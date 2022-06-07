package me.machinemaker.commands.api.argument;

import com.mojang.brigadier.context.CommandContext;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentArgument implements MinecraftArgument<Enchantment> {

    public static Enchantment getEnchantment(CommandContext<?> context, String name) {
        return context.getArgument(name, Enchantment.class);
    }
}
