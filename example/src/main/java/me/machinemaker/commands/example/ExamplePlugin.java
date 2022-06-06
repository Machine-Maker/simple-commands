package me.machinemaker.commands.example;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.Command;
import me.machinemaker.commands.api.PaperCommandDispatcher;
import me.machinemaker.commands.api.argument.PlayerProfileArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import static me.machinemaker.commands.api.Arguments.argument;
import static me.machinemaker.commands.api.Arguments.literal;

public class ExamplePlugin extends JavaPlugin {

    final PaperCommandDispatcher dispatcher = new PaperCommandDispatcher(this);
    static final EnumArgumentType<ExampleArg> EXAMPLE_ARG_ENUM_ARGUMENT_TYPE = new EnumArgumentType<>(ExampleArg.class);

    @Override
    public void onEnable() {
        dispatcher.register(literal("test_cmd")
                // .executes(sendMessage(Component.text("ROOT")))
                .then(argument("gp", new PlayerProfileArgument())
                        .executes(context -> {
                            return sendMessage(Component.text(context.getArgument("gp", PlayerProfileArgument.Result.class).getPlayerProfiles(context.getSource()).toString())).run(context);
                        })

                )
                // .then(argument("arg", EXAMPLE_ARG_ENUM_ARGUMENT_TYPE)
                //         .executes(context -> {
                //             return sendMessage(Component.text("arg: " + context.getArgument("arg", ExampleArg.class).name())).run(context);
                //         })
                //         .then(enchantment("ench")
                //                 .executes(context -> {
                //                     return sendMessage(Component.text("arg: " + context.getArgument("arg", ExampleArg.class).name() + " ench: " + context.getArgument("ench", Enchantment.class).translationKey())).run(context);
                //                 })
                //         )
                // )
        );
        dispatcher.apply();
    }

    enum ExampleArg {
        VALUE_1,
        VALUE_2,
        THIRD;
    }

    private static Command<BukkitBrigadierCommandSource> sendMessage(Component msg) {
        return context -> {
            context.getSource().getBukkitSender().sendMessage(msg);
            return 1;
        };
    }
}
