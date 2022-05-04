package me.machinemaker.commands.api;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        try {
            this.dispatcher.execute(constructCommand(commandLabel, args), sender);
        } catch (CommandSyntaxException e) {
            throw new IllegalArgumentException(e);
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
