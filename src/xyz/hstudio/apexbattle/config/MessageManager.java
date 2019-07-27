package xyz.hstudio.apexbattle.config;

import xyz.hstudio.apexbattle.ApexBattle;
import xyz.hstudio.apexbattle.annotation.LoadFromConfig;

import java.util.List;

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
    @LoadFromConfig(path = "shop_add_select")
    public String shop_add_select;
    @LoadFromConfig(path = "shop_add_successfully")
    public String shop_add_successfully;
    @LoadFromConfig(path = "lobby_set_successfully")
    public String lobby_set_successfully;
    @LoadFromConfig(path = "spectate_set_successfully")
    public String spectate_set_successfully;
    @LoadFromConfig(path = "already_in_game")
    public String already_in_game;
    @LoadFromConfig(path = "not_in_game")
    public String not_in_game;

    @LoadFromConfig(path = "no_enough_player_title")
    public String no_enough_player_title;
    @LoadFromConfig(path = "no_enough_player_sub_title")
    public String no_enough_player_sub_title;
    @LoadFromConfig(path = "player_death_title")
    public String player_death_title;
    @LoadFromConfig(path = "player_death_sub_title")
    public String player_death_sub_title;
    @LoadFromConfig(path = "player_get_item_title")
    public String player_get_item_title;
    @LoadFromConfig(path = "player_get_item_sub_title")
    public String player_get_item_sub_title;
    @LoadFromConfig(path = "notice_get_item_title")
    public String notice_get_item_title;
    @LoadFromConfig(path = "notice_get_item_sub_title")
    public String notice_get_item_sub_title;
    @LoadFromConfig(path = "lose_title")
    public String lose_title;
    @LoadFromConfig(path = "lose_sub_title")
    public String lose_sub_title;
    @LoadFromConfig(path = "win_title")
    public String win_title;
    @LoadFromConfig(path = "win_sub_title")
    public String win_sub_title;
    @LoadFromConfig(path = "weak_title")
    public String weak_title;
    @LoadFromConfig(path = "weak_sub_title")
    public String weak_sub_title;
    @LoadFromConfig(path = "god_title")
    public String god_title;
    @LoadFromConfig(path = "god_sub_title")
    public String god_sub_title;
    @LoadFromConfig(path = "god_end_title")
    public String god_end_title;
    @LoadFromConfig(path = "god_end_sub_title")
    public String god_end_sub_title;

    @LoadFromConfig(path = "create_sign_successfully")
    public String create_sign_successfully;
    @LoadFromConfig(path = "create_sign_failed")
    public String create_sign_failed;

    @LoadFromConfig(path = "cannot_join")
    public String cannot_join;

    @LoadFromConfig(path = "shop_tag")
    public String shop_tag;

    @LoadFromConfig(path = "end_message")
    public List<String> end_message;

    public MessageManager() {
        ConfigLoader.load(this, ApexBattle.getInstance().getMessage());
    }
}