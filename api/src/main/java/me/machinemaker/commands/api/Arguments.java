package me.machinemaker.commands.api;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.machinemaker.commands.api.arguments.EnchantmentArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

public final class Arguments {

    private Arguments() {
    }

    public static LiteralArgumentBuilder<CommandSender> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSender, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static RequiredArgumentBuilder<CommandSender, Enchantment> enchantment(String name) {
        return argument(name, new EnchantmentArgument());
    }
}
