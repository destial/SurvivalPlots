package xyz.destiall.survivalplots.plot;

import java.util.Arrays;
import java.util.List;

public enum PlotFlags {
    MEMBER_BUILD("member_build", "Allow members to build"),
    MEMBER_OPEN_INVENTORY("member_open_inventory", "Allow members to open chests"),
    MEMBER_INTERACT_BLOCK("member_use_block", "Allow members to use or interact with blocks (e.g gates, doors)"),
    MEMBER_INTERACT_ENTITY("member_use_entity", "Allow members to use or interact with entities (e.g. armor stands, horse)"),
    MEMBER_EDIT_DESCRIPTION("member_edit_description", "Allow members to edit the plot's description"),
    MEMBER_EDIT_FLAGS("member_edit_flags", "Allow members to edit this plot's flags"),
    MEMBER_TRUST_OTHER("member_trust_other", "Allow members to trust other players in this plot"),
    MEMBER_BAN_OTHER("member_ban_other", "Allow members to ban other players from this plot"),

    GUEST_BUILD("guest_build", "Allow visitors to build"),
    GUEST_OPEN_INVENTORY("guest_open_inventory", "Allow visitors to open chests"),
    GUEST_INTERACT_BLOCK("guest_use_block", "Allow visitors to use or interact with blocks (e.g gates, doors)"),
    GUEST_INTERACT_ENTITY("guest_use_entity", "Allow visitors to use or interact with entities (e.g. armor stands, horse)"),

    ANIMALS_INVINCIBLE("animals_invincible", "Make animals invincible to damage"),
    PVP_ON("pvp", "Allow player combat"),
    EXPLOSIONS_ON("explosions", "Allow explosions"),
    SHOW_DESCRIPTION_ENTER("show_description_on_enter", "Show your plot's description when entering"),
    ALLOW_VEHICLES("allow_vehicles", "Allow boats or minecarts to be placed"),

    PARTY_TRUST("party_trust", "Allow party members to have the same permission as trusted members")

    ;

    private final String desc;
    private final String name;
    PlotFlags(String name, String description) {
        this.name = name;
        this.desc = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return desc;
    }

    public static List<PlotFlags> def() {
        return Arrays.asList(MEMBER_BUILD, MEMBER_OPEN_INVENTORY, MEMBER_INTERACT_BLOCK, MEMBER_INTERACT_ENTITY);
    }

    public static PlotFlags getFlag(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (Exception e) {
            return Arrays.stream(values()).filter(f -> f.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
    }
}
