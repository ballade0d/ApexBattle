package xyz.hstudio.apexbattle.config;

import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;

public class MessageManager {

    @LoadFromConfig(path = "prefix")
    public String prefix;
    @LoadFromConfig(path = "no_command_found")
    public String no_command_found;
    @LoadFromConfig(path = "no_permission")
    public String no_permission;
    @LoadFromConfig(path = "command_wrong")
    public String command_wrong;
    @LoadFromConfig(path = "command_only_player")
    public String command_only_player;

    @LoadFromConfig(path = "game_already_exist")
    public String game_already_exist;
    @LoadFromConfig(path = "game_does_not_exist")
    public String game_does_not_exist;
    @LoadFromConfig(path = "create_successfully")
    public String create_successfully;
    @LoadFromConfig(path = "create_fail")
    public String create_fail;
    @LoadFromConfig(path = "set_region_successfully")
    public String set_region_successfully;
    @LoadFromConfig(path = "save_successfully")
    public String save_successfully;
    @LoadFromConfig(path = "save_fail")
    public String save_fail;
    @LoadFromConfig(path = "resource_add_successfully")
    public String resource_add_successfully;
    @LoadFromConfig(path = "team_add_successfully")
    public String team_add_successfully;
    @LoadFromConfig(path = "lobby_set_successfully")
    public String lobby_set_successfully;
    @LoadFromConfig(path = "already_in_game")
    public String already_in_game;
    @LoadFromConfig(path = "not_in_game")
    public String not_in_game;

    public MessageManager() {
        ConfigLoader.load(this, ApexBattle.getInstance().getMessage());
    }
}