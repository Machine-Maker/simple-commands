package me.machinemaker.commands.api.argument;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import java.util.Collection;

public class PlayerProfileArgument implements MinecraftArgument<PlayerProfileArgument.Result> {

    @FunctionalInterface
    public interface Result {
        Collection<PlayerProfile> getPlayerProfiles(BukkitBrigadierCommandSource sender) throws CommandSyntaxException;
    }

    public static Result getProfileResult(CommandContext<?> context, String name) {
        return context.getArgument(name, Result.class);
    }
}
