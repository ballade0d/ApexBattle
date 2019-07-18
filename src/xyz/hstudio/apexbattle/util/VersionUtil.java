package xyz.hstudio.apexbattle.util;

import org.bukkit.Bukkit;

import java.util.Arrays;

public class VersionUtil {

    private static final Version VERSION;

    static {
        // 获取版本
        String rawVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        VERSION = Arrays.stream(Version.values()).filter(v -> v.name().equals(rawVersion)).findFirst().orElse(Version.UNKNOWN);
    }

    public static Version getVersion() {
        return VERSION;
    }

    public enum Version {
        v1_9_R2,
        v1_10_R1,
        v1_11_R1,
        v1_12_R1,
        v1_13_R2,
        v1_14_R1,
        UNKNOWN
    }
}