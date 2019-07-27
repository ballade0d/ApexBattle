package xyz.hstudio.apexbattle.util;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public class ColorUtil {

    public static Color getColor(final ChatColor color) {
        switch (color) {
            case RED:
                return Color.RED;
            case AQUA:
                return Color.AQUA;
            case BLUE:
                return Color.BLUE;
            case GRAY:
                return Color.GRAY;
            case BLACK:
                return Color.BLACK;
            case GREEN:
                return Color.GREEN;
            case WHITE:
                return Color.WHITE;
            case YELLOW:
                return Color.YELLOW;
            default:
                return null;
        }
    }
}