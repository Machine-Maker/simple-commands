package me.machinemaker.commands.example;

import com.mojang.brigadier.Command;
import me.machinemaker.commands.api.PaperCommandDispatcher;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import static me.machinemaker.commands.api.Builders.argument;
import static me.machinemaker.commands.api.Builders.literal;

public class ExamplePlugin extends JavaPlugin {

    final PaperCommandDispatcher dispatcher = new PaperCommandDispatcher(this);
    static final EnumArgumentType<ExampleArg> EXAMPLE_ARG_ENUM_ARGUMENT_TYPE = new EnumArgumentType<>(ExampleArg.class);

    @Override
    public void onEnable() {
        dispatcher.register(literal("test_cmd")
                .executes(sendMessage(Component.text("ROOT")))
                .then(argument("arg", EXAMPLE_ARG_ENUM_ARGUMENT_TYPE)
                        .executes(context -> {
                            return sendMessage(Component.text("arg: " + context.getArgument("arg", ExampleArg.class).name())).run(context);
                        })
                )
        );
        dispatcher.apply();
    }

    enum ExampleArg {
        VALUE_1,
        VALUE_2,
        THIRD;
    }

    private static Command<CommandSender> sendMessage(Component msg) {
        return context -> {
            context.getSource().sendMessage(msg);
            return 1;
        };
    }
}
