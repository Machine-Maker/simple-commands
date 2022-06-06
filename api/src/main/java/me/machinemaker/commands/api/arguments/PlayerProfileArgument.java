package me.machinemaker.commands.api.arguments;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class PlayerProfileArgument implements MinecraftArgument<PlayerProfileArgument.Result> {

    @FunctionalInterface
    public interface Result {
        Collection<PlayerProfile> getPlayerProfiles(CommandSender sender) throws CommandSyntaxException;
    }
}
