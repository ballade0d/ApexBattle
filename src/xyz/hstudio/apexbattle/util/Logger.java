package xyz.hstudio.apexbattle.util;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

public class Logger {

    private static final ConsoleCommandSender SENDER = Bukkit.getConsoleSender();

    public static void log(final String msg) {
        Logger.SENDER.sendMessage("(APEX/INFO) " + msg);
    }
}