package dev.anvilcraft.lib.v2.config;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * AnvilLib API 兼容层：极简 TOML 配置（本 mod 仅一个布尔项）。
 * SERVER 配置文件位置与 NeoForge 一致：<world>/serverconfig/<name>-server.toml
 */
public final class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("anvillib-config-compat");

    private record ConfigRecord(Object instance, Class<?> clazz, String fileName) {}

    private static final Map<String, ConfigRecord> CONFIGS = new HashMap<>();

    private ConfigManager() {}

    public static <T> T register(String modId, java.util.function.Supplier<T> factory) {
        T instance = factory.get();
        Class<?> clazz = instance.getClass();
        Config annotation = clazz.getAnnotation(Config.class);
        if (annotation == null) {
            throw new IllegalArgumentException(clazz.getName() + " is missing @Config");
        }
        String fileName = annotation.name() + "-" + annotation.type().name().toLowerCase() + ".toml";
        CONFIGS.put(annotation.name(), new ConfigRecord(instance, clazz, fileName));
        if (annotation.type() == ModConfig.Type.SERVER) {
            // 服务器启动时从存档目录加载（NeoForge 同样延迟到服务器启动）
            ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                Path dir = server.getServerDirectory().resolve("serverconfig");
                loadConfig(new ConfigRecord(instance, clazz, fileName), dir);
            });
        } else {
            loadConfig(new ConfigRecord(instance, clazz, fileName), Path.of("config"));
        }
        return instance;
    }

    private static void loadConfig(ConfigRecord record, java.nio.file.Path dir) {
        try {
            Files.createDirectories(dir);
            Path file = dir.resolve(record.fileName());
            Map<String, String> values = new HashMap<>();
            if (Files.exists(file)) {
                for (String line : Files.readAllLines(file)) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("[")) continue;
                    int eq = line.indexOf('=');
                    if (eq <= 0) continue;
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    values.put(key, value);
                }
            }
            StringBuilder out = new StringBuilder();
            for (Field field : record.clazz().getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                Comment comment = field.getAnnotation(Comment.class);
                if (comment != null) {
                    out.append("# ").append(comment.value().replace("\n", "\n# ")).append('\n');
                }
                String stored = values.get(field.getName());
                Object current = field.get(record.instance());
                if (stored != null) {
                    try {
                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            field.set(record.instance(), Boolean.parseBoolean(stored));
                        } else if (field.getType() == int.class || field.getType() == Integer.class) {
                            field.set(record.instance(), Integer.parseInt(stored));
                        } else if (field.getType() == double.class || field.getType() == Double.class) {
                            field.set(record.instance(), Double.parseDouble(stored));
                        } else if (field.getType() == String.class) {
                            field.set(record.instance(), stored);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to load config value {}: {}", field.getName(), stored, e);
                    }
                }
                Object value = field.get(record.instance());
                if (value instanceof String s) {
                    out.append(field.getName()).append(" = \"").append(s).append("\"\n");
                } else {
                    out.append(field.getName()).append(" = ").append(value).append('\n');
                }
            }
            Files.writeString(file, out.toString());
        } catch (IOException | IllegalAccessException e) {
            LOGGER.error("Failed to load config {}", record.fileName(), e);
        }
    }
}
