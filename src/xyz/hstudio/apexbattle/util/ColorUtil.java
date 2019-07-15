package xyz.hstudio.apexbattle.util;

import org.bukkit.Color;

public class ColorUtil {

    public static Color getColor(final String color) {
        switch (color) {
            case "WHITE": {
                return Color.fromRGB(255, 255, 255);
            }
            case "SILVER": {
                return Color.fromRGB(192, 192, 192);
            }
            case "GRAY": {
                return Color.fromRGB(128, 128, 128);
            }
            case "BLACK": {
                return Color.fromRGB(0, 0, 0);
            }
            case "RED": {
                return Color.fromRGB(255, 0, 0);
            }
            case "MAROON": {
                return Color.fromRGB(128, 0, 0);
            }
            case "YELLOW": {
                return Color.fromRGB(255, 255, 0);
            }
            case "OLIVE": {
                return Color.fromRGB(128, 128, 0);
            }
            case "LIME": {
                return Color.fromRGB(0, 255, 0);
            }
            case "GREEN": {
                return Color.fromRGB(0, 128, 0);
            }
            case "AQUA": {
                return Color.fromRGB(0, 255, 255);
            }
            case "TEAL": {
                return Color.fromRGB(0, 128, 128);
            }
            case "BLUE": {
                return Color.fromRGB(0, 0, 255);
            }
            case "NAVY": {
                return Color.fromRGB(0, 0, 128);
            }
            case "FUCHSIA": {
                return Color.fromRGB(255, 0, 255);
            }
            case "PURPLE": {
                return Color.fromRGB(128, 0, 128);
            }
            case "ORANGE": {
                return Color.fromRGB(255, 165, 0);
            }
            default: {
                return null;
            }
        }
    }
}