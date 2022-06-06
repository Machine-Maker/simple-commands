package me.machinemaker.commands.api;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.machinemaker.commands.api.argument.EnchantmentArgument;
import org.bukkit.enchantments.Enchantment;

public final class Arguments {

    private Arguments() {
    }

    public static LiteralArgumentBuilder<BukkitBrigadierCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<BukkitBrigadierCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static RequiredArgumentBuilder<BukkitBrigadierCommandSource, Enchantment> enchantment(String name) {
        return argument(name, new EnchantmentArgument());
    }
}
