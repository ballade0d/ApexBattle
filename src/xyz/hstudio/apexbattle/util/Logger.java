package xyz.hstudio.apexbattle.util;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Logger {

    private static final ConsoleCommandSender sender = Bukkit.getConsoleSender();

    public static void info(final String msg) {
        sender.sendMessage("[APEX|INFO] " + msg);
    }
}