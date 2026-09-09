# ThatSkyInteractions — Fabric

[ThatSkyInteractions](https://github.com/LouisQuepierts/ThatSkyInteractions) 的 Fabric 移植版，经原作者授权。移植自 NeoForge 版 2.1.0-rc2。

## 环境要求

| | |
|---|---|
| Minecraft | 26.1.2 |
| Fabric Loader | 0.19.5+ |
| Fabric API | 0.155.3+26.1.2 |
| Java | 25 |

## 安装

把 Release 里的 jar 放进 `mods/`，并安装 [Fabric API](https://modrinth.com/mod/fabric-api)。

## 已移植

好友树、交互邀请与接受、牵手、背起、表情、语音、动画同步、SDF 界面渲染。单人与多人联机均可用。

另外新增了 4 项原版没有的功能：靠近玩家时头顶显示交互图标、互动粒子与音效反馈、互动镜头 FOV 脉冲、牵手与背起时头部对视。

## 尚不完善

- 背起、牵手、下马的姿态未逐帧核对，与原版可能存在细微差异
- 仅在局域网环境验证过，独立服务器未回归
- 三人及以上场景未验证
- 滚轮调整互动镜头距离未实机验证

## 构建

```bash
./gradlew build
```

需要 JDK 25。

## 致谢

动画后端使用 [veynir](https://github.com/LouisQuepierts/veynir) 1.2.1（MIT），已合并进主 jar；SDF 渲染部分来自 AnvilLib（MPL-2.0）。

感谢 Louis_Quepierts 编写 ThatSkyInteractions 与 veynir。

## 许可

MIT
