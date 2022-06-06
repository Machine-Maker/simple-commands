package me.machinemaker.commands.api.argument;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.inventory.ItemStack;

public class ItemStackArgument implements MinecraftArgument<ItemStackArgument.Result> {

    @FunctionalInterface
    public interface Result {

        ItemStack getStack(int amount) throws CommandSyntaxException;
    }
}
