package com.gmail.theminiluca.addon.luca.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import jline.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.gmail.theminiluca.addon.luca.LucaSK.chests;

public class ChestOpenEffect extends Effect {


    static {
        Skript.registerEffect(ChestOpenEffect.class, "[LucaSK] open static chest [with %number% rows] to %player% from [id] %-string%");
    }




    private Expression<Player> player;
    private Expression<String> id;
    private Expression<Number> rows;

    @Override
    protected void execute(Event event) {
        @NotNull final Player player = Objects.requireNonNull(this.player.getSingle(event));
        @NotNull final String id = Objects.requireNonNull(this.id.getSingle(event));
        int rows = (this.rows == null ? 6 : Objects.requireNonNull(this.rows.getSingle(event)).intValue());
        if (rows < 1 || rows > 6) throw new IllegalArgumentException("슬롯은 1..6 그 사이만 가능합니다.");
        rows *= 9;
        if (!chests.containsKey(id))
            chests.put(id, Bukkit.createInventory(null, rows, id));
        Inventory inventory = chests.get(id);

        player.openInventory(inventory);

    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "open static chest [with %-number% rows] to %player% from [id] %-string% [named %-string%]";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        if (parseResult.expr.contains("with") && parseResult.expr.contains("rows"))
            this.rows = (Expression<Number>) expressions[0];
        else
            this.rows = null;
        this.player = (Expression<Player>) expressions[1];
        this.id = (Expression<String>) expressions[2];

        return true;
    }
}
