package me.machinemaker.commands.api;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.brigadier.PaperBrigadier;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PaperBrigadierCommand extends Command implements PluginIdentifiableCommand {

    private final PaperCommandDispatcher dispatcher;
    private final LiteralCommandNode<CommandSender> root;

    public PaperBrigadierCommand(PaperCommandDispatcher dispatcher, LiteralCommandNode<CommandSender> root) {
        super(root.getLiteral());
        this.setDescription(String.join("\n", root.getExamples()));
        this.setUsage(root.getUsageText());
        this.dispatcher = dispatcher;
        if (root.getRequirement() instanceof PermissionRequirement permissionRequirement) {
            this.setPermission(permissionRequirement.permission());
        }
        this.root = root;
    }

    private static void sendFailure(@NotNull CommandSender sender, Component message) {
        sender.sendMessage(Identity.nil(), Component.text().color(NamedTextColor.RED).append(message));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        try {
            this.dispatcher.execute(constructCommand(commandLabel, args), sender);
        } catch (CommandSyntaxException syntaxException) { // Copied from nms Commands.java
            sendFailure(sender, PaperBrigadier.componentFromMessage(syntaxException.getRawMessage()));
            if (syntaxException.getInput() != null && syntaxException.getCursor() >= 0) {
                int j = Math.min(syntaxException.getInput().length(), syntaxException.getCursor());
                TextComponent.Builder builder = Component.text()
                        .color(NamedTextColor.GRAY)
                        .clickEvent(ClickEvent.runCommand(commandLabel));

                if (j > 10) {
                    builder.append(Component.text("..."));
                }

                builder.append(Component.text(syntaxException.getInput().substring(Math.max(0, j - 10), j)));
                if (j < syntaxException.getInput().length()) {
                    builder.append(Component.text()
                            .content(syntaxException.getInput().substring(j))
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.UNDERLINED)
                    );
                }
                builder.append(Component.translatable("command.context.here")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.ITALIC)
                );
                sendFailure(sender, builder.build());
            }
        } catch (Exception exception) {
            TextComponent.Builder chatcomponenttext = Component.text().content(exception.getMessage() == null ? exception.getClass().getName() : exception.getMessage());

            if (this.getPlugin().getSLF4JLogger().isDebugEnabled()) {
                this.getPlugin().getSLF4JLogger().error("Command exception: {}", "/" + commandLabel, exception);
                StackTraceElement[] astacktraceelement = exception.getStackTrace();

                for (int k = 0; k < Math.min(astacktraceelement.length, 3); ++k) {
                    chatcomponenttext
                            .append(Component.text("\n\n"))
                            .append(Component.text(astacktraceelement[k].getMethodName()))
                            .append(Component.text("\n "))
                            .append(Component.text(Objects.requireNonNullElse(astacktraceelement[k].getFileName(), "unknown file")))
                            .append(Component.text(":"))
                            .append(Component.text(String.valueOf(astacktraceelement[k].getLineNumber())));
                }
            }
            sendFailure(sender, Component.translatable()
                    .key("command.failed")
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, chatcomponenttext.build()))
                    .build()
            );
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        ParseResults<CommandSender> parseResults = this.dispatcher.parse(constructCommand(alias, args), sender);
        Suggestions suggestions = this.dispatcher.getCompletionSuggestions(parseResults).join();
        return suggestions.getList().stream().map(Suggestion::getText).toList();
    }

    private static String constructCommand(String label, String[] args) {
        final StringBuilder sb = new StringBuilder(label);
        for (String arg : args) {
            sb.append(" ").append(arg);
        }
        return sb.toString();
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this.dispatcher.plugin();
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return this.root.getRequirement().test(target);
    }

    public LiteralCommandNode<CommandSender> node() {
        return this.root;
    }
}
