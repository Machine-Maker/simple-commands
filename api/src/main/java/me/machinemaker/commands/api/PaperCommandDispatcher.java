package me.machinemaker.commands.api;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommand;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.google.common.base.Preconditions;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.brigadier.PaperBrigadier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PaperCommandDispatcher extends CommandDispatcher<CommandSender> {

    private final Plugin plugin;
    private boolean registered = false;

    public PaperCommandDispatcher(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LiteralCommandNode<CommandSender> register(LiteralArgumentBuilder<CommandSender> command) {
        Preconditions.checkState(!this.registered, "Cannot register commands after they've been registered to the command map");
        return super.register(command);
    }

    public void apply() {
        Preconditions.checkState(!this.registered, "Cannot register commands after they've been registered to the command map");
        List<Command> commands = new ArrayList<>();
        for (CommandNode<CommandSender> child : this.getRoot().getChildren()) {
            if (child instanceof LiteralCommandNode<CommandSender> literalChild) {
                commands.add(new PaperBrigadierCommand(this, literalChild));
            } else {
                throw new IllegalStateException(child + " is not a literal command node");
            }
        }
        this.plugin.getServer().getCommandMap().registerAll(this.plugin.getName().toLowerCase(Locale.ROOT), commands);
        this.plugin.getServer().getPluginManager().registerEvents(new EventListener(), this.plugin);
        this.registered = true;
    }

    public Plugin plugin() {
        return this.plugin;
    }

    private final class EventListener implements Listener {

        @EventHandler
        public void onTabComplete(AsyncTabCompleteEvent event) {
            final String strippedBuffer = event.getBuffer().startsWith("/") ? event.getBuffer().substring(1) : event.getBuffer();
            if (strippedBuffer.trim().isEmpty()) {
                return;
            }
            final ParseResults<CommandSender> parseResults = PaperCommandDispatcher.this.parse(strippedBuffer, event.getSender());
            final Suggestions suggestions = PaperCommandDispatcher.this.getCompletionSuggestions(parseResults).join();
            event.setHandled(true);
            event.completions(suggestions.getList().stream().map(suggestion -> AsyncTabCompleteEvent.Completion.completion(suggestion.getText(), PaperBrigadier.componentFromMessage(suggestion.getTooltip()))).toList());
        }

        @EventHandler
        public void onCommandRegister(CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
            if (event.getCommand() instanceof PaperBrigadierCommand command && command.getPlugin() == PaperCommandDispatcher.this.plugin) {
                event.setLiteral((LiteralCommandNode<BukkitBrigadierCommandSource>) convert(command.node(), event.getBrigadierCommand()));
            }
        }

        private static CommandNode<BukkitBrigadierCommandSource> convert(CommandNode<CommandSender> node, BukkitBrigadierCommand<BukkitBrigadierCommandSource> executor) {
            if (node instanceof LiteralCommandNode<CommandSender> literal) {
                final LiteralCommandNode<BukkitBrigadierCommandSource> newNode = LiteralArgumentBuilder.<BukkitBrigadierCommandSource>literal(literal.getLiteral())
                        .requires(bukkitBrigadierCommandSource -> node.getRequirement().test(bukkitBrigadierCommandSource.getBukkitSender()))
                        .executes(executor).build();
                for (CommandNode<CommandSender> child : literal.getChildren()) {
                    newNode.addChild(convert(child, executor));
                }
                return newNode;
            } else if (node instanceof ArgumentCommandNode<CommandSender, ?> argument) {
                final ArgumentCommandNode<BukkitBrigadierCommandSource, ?> newNode = RequiredArgumentBuilder.<BukkitBrigadierCommandSource, Object>argument(argument.getName(), (ArgumentType<Object>) argument.getType())
                        .requires(bukkitBrigadierCommandSource -> node.getRequirement().test(bukkitBrigadierCommandSource.getBukkitSender()))
                        .suggests(executor)
                        .executes(executor).build();
                for (CommandNode<CommandSender> child : argument.getChildren()) {
                    newNode.addChild(convert(child, executor));
                }
                return newNode;
            } else {
                throw new IllegalStateException(node + " is a root");
            }
        }
    }
}
