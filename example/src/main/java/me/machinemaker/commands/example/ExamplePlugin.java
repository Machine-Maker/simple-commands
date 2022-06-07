package me.machinemaker.commands.example;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.Command;
import me.machinemaker.commands.api.PaperCommandDispatcher;
import me.machinemaker.commands.api.argument.EnchantmentArgument;
import me.machinemaker.commands.api.argument.PlayerProfileArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import static me.machinemaker.commands.api.argument.Arguments.argument;
import static me.machinemaker.commands.api.argument.Arguments.literal;

public class ExamplePlugin extends JavaPlugin {

    final PaperCommandDispatcher dispatcher = new PaperCommandDispatcher(this);
    static final EnumArgumentType<ExampleArg> EXAMPLE_ARG_ENUM_ARGUMENT_TYPE = new EnumArgumentType<>(ExampleArg.class);

    @Override
    public void onEnable() {
        dispatcher.register(literal("test_cmd")
                        .then(literal("profile").then(argument("gp", new PlayerProfileArgument()).executes(sendProfileMessage())))
                        .then(literal("ench").then(argument("ench", new EnchantmentArgument()).executes(sendEnchantmentMessage())))
        );
        dispatcher.apply();
    }

    enum ExampleArg {
        VALUE_1,
        VALUE_2,
        THIRD;
    }

    private static Command<BukkitBrigadierCommandSource> sendProfileMessage() {
        return context -> {
            context.getSource().getBukkitSender().sendMessage(Component.text(PlayerProfileArgument.getProfileResult(context, "gp").getPlayerProfiles(context.getSource()).toString()));
            return 1;
        };
    }

    private static Command<BukkitBrigadierCommandSource> sendEnchantmentMessage() {
        return context -> {
            context.getSource().getBukkitSender().sendMessage(Component.translatable(EnchantmentArgument.getEnchantment(context, "ench")));
            return 1;
        };
    }

    private static Command<BukkitBrigadierCommandSource> sendMessage(Component msg) {
        return context -> {
            context.getSource().getBukkitSender().sendMessage(msg);
            return 1;
        };
    }
}
