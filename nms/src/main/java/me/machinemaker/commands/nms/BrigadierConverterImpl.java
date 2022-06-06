package me.machinemaker.commands.nms;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommand;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.adventure.PaperAdventure;
import me.machinemaker.commands.api.arguments.EnchantmentArgument;
import me.machinemaker.commands.api.arguments.PlayerProfileArgument;
import me.machinemaker.commands.api.arguments.MinecraftArgument;
import me.machinemaker.commands.api.arguments.ColorArgument;
import me.machinemaker.commands.api.brigadier.BrigadierConverter;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ItemEnchantmentArgument;
import net.minecraft.core.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class BrigadierConverterImpl implements BrigadierConverter {

    private final Multimap<Class<? extends MinecraftArgument<?>>, Mapper<?, ?>> map = Multimaps.synchronizedMultimap(LinkedHashMultimap.create());

    public BrigadierConverterImpl() {
        this.registerMapper(new Mapper<>(PlayerProfileArgument.class, GameProfileArgument.gameProfile(), result -> {
            return sender -> result.getNames(VanillaCommandWrapper.getListener(sender)).stream().map(profile -> (PlayerProfile) new CraftPlayerProfile(profile)).toList();
        }));

        this.registerMapper(new Mapper<>(ColorArgument.class, net.minecraft.commands.arguments.ColorArgument.color(), PaperAdventure::asAdventure));
        this.registerMapper(new Mapper<>(EnchantmentArgument.class, ItemEnchantmentArgument.enchantment(), byKey(Enchantment::getByKey, Registry.ENCHANTMENT)));


    }

    private static <T, M> Function<M, T> byKey(Function<NamespacedKey, T> fromKey, Registry<M> registry) {
        return nms -> Objects.requireNonNull(fromKey.apply(CraftNamespacedKey.fromMinecraft(Objects.requireNonNull(registry.getKey(nms)))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CommandNode<BukkitBrigadierCommandSource> convert(CommandNode<CommandSender> node, BukkitBrigadierCommand<BukkitBrigadierCommandSource> executor) {
        if (node instanceof LiteralCommandNode<CommandSender> literal) {
            final LiteralCommandNode<BukkitBrigadierCommandSource> newNode = LiteralArgumentBuilder.<BukkitBrigadierCommandSource>literal(literal.getLiteral())
                    .requires(bukkitBrigadierCommandSource -> node.getRequirement().test(bukkitBrigadierCommandSource.getBukkitSender()))
                    .executes(executor).build();
            for (CommandNode<CommandSender> child : literal.getChildren()) {
                newNode.addChild(convert(child, executor));
            }
            return newNode;
        } else if (node instanceof ArgumentCommandNode<CommandSender, ?> argument) {
            @Nullable ArgumentType<?> argumentType = null;
            @Nullable SuggestionProvider<BukkitBrigadierCommandSource> suggestionProvider = executor;
            if (argument.getType() instanceof MinecraftArgument<?> minecraftArgument) {
                argumentType = this.getNativeArgumentType(minecraftArgument);
                if (minecraftArgument.delegateSuggestionsToNativeType()) {
                    suggestionProvider = null;
                }
            }
            final ArgumentCommandNode<BukkitBrigadierCommandSource, ?> newNode = RequiredArgumentBuilder.<BukkitBrigadierCommandSource, Object>argument(argument.getName(), (ArgumentType<Object>) Objects.requireNonNullElseGet(argumentType, StringArgumentType::word))
                    .requires(bukkitBrigadierCommandSource -> node.getRequirement().test(bukkitBrigadierCommandSource.getBukkitSender()))
                    .suggests(suggestionProvider)
                    .executes(executor).build();
            for (CommandNode<CommandSender> child : argument.getChildren()) {
                newNode.addChild(convert(child, executor));
            }
            return newNode;
        } else {
            throw new IllegalStateException(node + " is not a LiteralCommandNode or ArgumentCommandNode");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends MinecraftArgument<V>, V> V parse(StringReader reader, T argumentType) throws CommandSyntaxException {
        for (Mapper<V, ?> mapper : this.getMappers((Class<T>) argumentType.getClass())) {
            if (mapper.test(argumentType)) {
                return mapper.parse(reader);
            }
        }
        throw new IllegalArgumentException("Could not find mapper for " + argumentType);
    }

    @Override
    public void registerMapper(Mapper<?, ?> mapper) {
        this.map.put(mapper.apiType(), mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends MinecraftArgument<V>, V> ArgumentType<?> getNativeArgumentType(T argumentType) {
        for (Mapper<V, ?> mapper : this.getMappers((Class<T>) argumentType.getClass())) {
            if (mapper.test(argumentType)) {
                return mapper.nativeType();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends MinecraftArgument<V>, V> Collection<? extends Mapper<V, ?>> getMappers(Class<? extends T> argumentTypeClass) {
        return this.map.get(argumentTypeClass).stream().map(m -> (Mapper<V, ?>) m).toList();
    }
}
