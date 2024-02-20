package com.gmail.theminiluca.addon.luca.effects.team;

import io.github.theminiluca.api.utils.Colour;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class TeamInfo {
    
    @NotNull
    private final String prefix;
    private final String suffix;
    
    private final String name;

    public TeamInfo(@NotNull String prefix, String suffix, String name) {
        assert prefix.isEmpty();
        this.prefix = prefix;
        this.suffix = suffix;
        this.name = name;
    }

    public @NotNull String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getName() {
        return name;
    }
    
    public ChatColor getColor() {
        return Colour.getLastColor(prefix.replace("§", "&"));
    }
    
}
