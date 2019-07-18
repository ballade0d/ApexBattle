package xyz.hstudio.apexbattle.config;

import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;

import java.lang.reflect.Field;
import java.util.List;

public class ConfigLoader {

    public static boolean load(final Object object, final FileConfiguration config) {
        LoadFromConfig annotation;
        for (Field field : object.getClass().getDeclaredFields()) {
            // 获取变量的注解
            annotation = field.getAnnotation(LoadFromConfig.class);
            if (annotation == null) {
                continue;
            }
            // 使变量可以被访问
            field.setAccessible(true);
            // 获取配置路径
            String path = annotation.path();
            // 获取变量类型
            Class type = field.getType();

            // 赋值
            try {
                if (type == String.class) {
                    field.set(object, config.getString(path));
                } else if (type == Integer.class || type == int.class) {
                    field.set(object, config.getInt(path));
                } else if (type == Boolean.class || type == boolean.class) {
                    field.set(object, config.getBoolean(path));
                } else if (type == Double.class || type == double.class) {
                    field.set(object, config.getDouble(path));
                } else if (type == Long.class || type == long.class) {
                    field.set(object, config.getLong(path));
                } else if (type == Vector.class) {
                    field.set(object, config.getVector(path));
                } else if (type == OfflinePlayer.class) {
                    field.set(object, config.getOfflinePlayer(path));
                } else if (type == ItemStack.class) {
                    field.set(object, config.getItemStack(path));
                } else if (type == Color.class) {
                    field.set(object, config.getColor(path));
                } else if (type == ConfigurationSection.class) {
                    field.set(object, config.getConfigurationSection(path));
                } else if (type == List.class) {
                    if (annotation.listType() == String.class) {
                        field.set(object, config.getStringList(path));
                    } else if (annotation.listType() == Integer.class) {
                        field.set(object, config.getIntegerList(path));
                    } else if (annotation.listType() == Boolean.class) {
                        field.set(object, config.getBooleanList(path));
                    } else if (annotation.listType() == Double.class) {
                        field.set(object, config.getDoubleList(path));
                    } else if (annotation.listType() == Float.class) {
                        field.set(object, config.getFloatList(path));
                    } else if (annotation.listType() == Long.class) {
                        field.set(object, config.getLongList(path));
                    } else if (annotation.listType() == Byte.class) {
                        field.set(object, config.getByteList(path));
                    } else if (annotation.listType() == Character.class) {
                        field.set(object, config.getCharacterList(path));
                    } else if (annotation.listType() == Short.class) {
                        field.set(object, config.getShortList(path));
                    } else {
                        field.set(object, config.getList(path));
                    }
                }
            } catch (IllegalAccessException ignore) {
                return false;
            }
        }
        return true;
    }
}