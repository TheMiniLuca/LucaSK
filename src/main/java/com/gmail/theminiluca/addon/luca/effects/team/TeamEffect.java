package com.gmail.theminiluca.addon.luca.effects.team;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.gmail.theminiluca.addon.luca.LucaSK;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.gmail.theminiluca.addon.luca.LucaSK.teamColor;

public class TeamEffect extends Effect {
    public static final String TEAM_EFFECT = "[LucaSK] packet %-player% to be show for %-player% with prefix %-string% [and suffix %-string%] [named %-string%]";

    static {
        Skript.registerEffect(TeamEffect.class, TEAM_EFFECT);
    }

    static ScoreboardManager manager;

    @Override
    protected void execute(Event event) {

        @NotNull final Player player = Objects.requireNonNull(this.player.getSingle(event));
        @NotNull final Player target = Objects.requireNonNull(this.target.getSingle(event));
        @NotNull final String prefix = Objects.requireNonNull(this.prefix.getSingle(event));
        final String suffix = (this.suffix == null ? null : this.suffix.getSingle(event));
        final String name = (this.name == null ? null : this.name.getSingle(event));
        if (!teamColor.containsKey(player.getUniqueId()))
            teamColor.put(player.getUniqueId(), new HashMap<>());
        Map<UUID, TeamInfo> map = teamColor.get(player.getUniqueId());
        TeamInfo teamInfo = new TeamInfo(prefix, suffix, name);
        map.put(target.getUniqueId(), teamInfo);
        update(player);

    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Horse horse) {
            AttributeInstance attribute = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (attribute == null) return;
            attribute.setBaseValue(attribute.getBaseValue() * 1.6d);
        }
    }


    static Map<UUID, Scoreboard> scoreboardBack = new HashMap<>();

    public static void update(final Player player) {
        Scoreboard scoreboard = manager.getNewScoreboard();
        if (teamColor.containsKey(player.getUniqueId())) {
            for (Map.Entry<UUID, TeamInfo> entry : teamColor.get(player.getUniqueId()).entrySet()) {
                Player target = Bukkit.getPlayer(entry.getKey());
                if (target == null) continue;
                TeamInfo info = entry.getValue();
                @NotNull Team team;
                String name = (info.getName() == null ? "" : info.getName()) + target.getName();
                if (scoreboard.getTeam(name) == null)
                    team = scoreboard.registerNewTeam(name);
                else
                    team = Objects.requireNonNull(scoreboard.getTeam(name));
                team.setColor(info.getColor());
                team.setPrefix(info.getPrefix());
                if (info.getSuffix() != null)
                    team.setSuffix(info.getSuffix());
                team.addEntry(target.getName());

            }
            if (!scoreboardBack.containsKey(player.getUniqueId()))
                scoreboardBack.put(player.getUniqueId(), scoreboard);
            if (!scoreboardBack.get(player.getUniqueId()).equals(scoreboard)) {
                scoreboardBack.put(player.getUniqueId(), scoreboard);
                player.setScoreboard(scoreboard);
            }
        }

    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return TEAM_EFFECT;
    }

    private Expression<Player> player;
    private Expression<String> prefix;
    private Expression<String> suffix;
    private Expression<String> name;
    private Expression<Player> target;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        this.player = (Expression<Player>) expressions[0];
        this.target = (Expression<Player>) expressions[1];
        this.prefix = (Expression<String>) expressions[2];
        this.suffix = (Expression<String>) expressions[3];
        this.name = (Expression<String>) expressions[4];
        return true;
    }
}
