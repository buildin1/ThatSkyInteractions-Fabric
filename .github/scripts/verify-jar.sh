#!/usr/bin/env bash
# 发布产物的自检。build.yml 与 release.yml 共用，避免两边各写一份而漂移。
#
# 这里查的两件事都有同一个特点：出问题时 `gradlew build` 照样成功、dev 里
# `runClient` 也完全正常，只有把 jar 装进真实客户端才炸。所以必须对产物本身查。
set -euo pipefail

JAR=$(ls build/libs/*.jar | grep -v -- '-sources' | head -1)
echo "Inspecting $JAR"

fail=0

# --- 1. Lombok ---------------------------------------------------------------
# Lombok 与其它注解处理器冲突时会“静默失效”：不生成任何 @Getter/@Slf4j，
# 但编译不报错。只能从产物里反查生成的方法是否存在。
unzip -p "$JAR" net/quepierts/thatskyinteractions/feature/animation/PlayerAnimationController.class \
  > /tmp/PlayerAnimationController.class
javap -p /tmp/PlayerAnimationController.class > /tmp/pac.txt
if ! grep -q "isPlaying()" /tmp/pac.txt; then
  echo "::error::Lombok 未生成 isPlaying()"
  fail=1
else
  echo "Lombok OK"
fi

# --- 2. mixin refmap ---------------------------------------------------------
# Loom 1.6.12 走 legacy mixin AP。MixinExtras 的注解
# (@ModifyExpressionValue / @WrapOperation / @ModifyReturnValue) 需要它自己的
# AP 扩展才会写进 refmap；一旦被排出 annotationProcessor 路径，这些注入点会
# 整批丢失，生产环境（intermediary 名）直接崩在 Mixin apply。
if unzip -l "$JAR" | grep -q "thatskyinteractions-refmap.json"; then
  unzip -p "$JAR" thatskyinteractions-refmap.json > /tmp/refmap.json

  for M in EntitySizeMixin CameraEventMixin GameRendererFovMixin ModelManagerMixin \
           CameraMixin MouseHandlerMixin GameRendererMixin; do
    if ! grep -q "/$M\"" /tmp/refmap.json; then
      echo "::error::refmap 缺少 $M —— MixinExtras 注解处理器可能被排除了"
      fail=1
    fi
  done

  for SEL in refreshDimensions "setPosition(DDD)V" turnPlayer "pick(F)V"; do
    if ! grep -qF "$SEL" /tmp/refmap.json; then
      echo "::error::refmap 缺少选择器 $SEL"
      fail=1
    fi
  done

  [ "$fail" -eq 0 ] && \
    echo "refmap OK ($(grep -o "\"net/quepierts/thatskyinteractions/feature/mixin/[A-Za-z/]*\"" /tmp/refmap.json | sort -u | wc -l) 个 mixin)"
else
  # Loom 1.18（26.1.2 线）在 remap 阶段直接改写注解，不产生 refmap，属正常。
  echo "no refmap in jar (Loom 新版直接 remap 注解) — 跳过 refmap 校验"
fi

exit "$fail"
