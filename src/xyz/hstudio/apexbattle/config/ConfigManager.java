package xyz.hstudio.apexbattle.config;

import xyz.hstudio.apexbattle.annotation.LoadFromConfig;

public class ConfigManager {

    @LoadFromConfig(path = "wait_time")
    public int wait_time;

}