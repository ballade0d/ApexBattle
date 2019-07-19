package xyz.hstudio.apexbattle.util;

import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.hstudio.apexbattle.ApexBattle;

import java.io.File;
import java.io.IOException;

public class WorldUtil {

    /**
     * 保存世界区块
     *
     * @param world 世界
     * @param game  游戏名
     * @return 是否成功
     */
    public static boolean saveWorld(final World world, final String game) {
        String name = world.getName();
        File regionDir = new File(Bukkit.getWorldContainer(), name + "/region");
        if (!regionDir.exists()) {
            return false;
        }
        File[] regions = regionDir.listFiles();
        if (regions == null) {
            return false;
        }
        try {
            File newFile;
            for (File region : regions) {
                newFile = new File(ApexBattle.getInstance().getDataFolder(), "map/" + game + "/" + region.getName());
                if (!newFile.getParentFile().exists() && !newFile.getParentFile().mkdirs()) {
                    return false;
                }
                Files.copy(region, newFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 重置世界
     *
     * @param world 世界
     * @param game  游戏名
     * @return 是否成功
     */
    public static boolean resetWorld(final World world, final String game) {
        String name = world.getName();
        File regionDir = new File(ApexBattle.getInstance().getDataFolder(), "map/" + game);
        if (!regionDir.exists()) {
            return false;
        }
        File[] regions = regionDir.listFiles();
        if (regions == null) {
            return false;
        }
        try {
            File newFile = new File(Bukkit.getWorldContainer(), name + "/region");
            if (newFile.exists() && newFile.isDirectory()) {
                File[] oldRegions = newFile.listFiles();
                if (oldRegions != null) {
                    for (File oldRegion : oldRegions) {
                        if (oldRegion.delete()) {
                            return false;
                        }
                    }
                }
                if (!newFile.delete()) {
                    return false;
                }
                if (!newFile.createNewFile()) {
                    return false;
                }
            }
            for (File region : regions) {
                newFile = new File(Bukkit.getWorldContainer(), name + "/region" + "/" + region.getName());
                Files.copy(region, newFile);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}