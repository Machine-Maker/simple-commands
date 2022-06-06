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
import me.machinemaker.commands.api.argument.BlockArgument;
import me.machinemaker.commands.api.argument.ComponentArgument;
import me.machinemaker.commands.api.argument.EnchantmentArgument;
import me.machinemaker.commands.api.argument.ItemStackArgument;
import me.machinemaker.commands.api.argument.ObjectiveArgument;
import me.machinemaker.commands.api.argument.PlayerProfileArgument;
import me.machinemaker.commands.api.argument.MinecraftArgument;
import me.machinemaker.commands.api.argument.ColorArgument;
import me.machinemaker.commands.api.argument.CoordinateArgument;
import me.machinemaker.commands.api.brigadier.BrigadierConverter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ItemEnchantmentArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_18_R2.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class BrigadierConverterImpl implements BrigadierConverter {

    private final Multimap<Class<? extends MinecraftArgument<?>>, Mapper<?, ?, ?>> map = Multimaps.synchronizedMultimap(LinkedHashMultimap.create());

    public BrigadierConverterImpl() {
        // entity
        this.registerMapper(new Mapper<>(PlayerProfileArgument.class, GameProfileArgument.gameProfile(), result -> {
            return sender -> result.getNames((CommandSourceStack) sender).stream().map(profile -> (PlayerProfile) new CraftPlayerProfile(profile)).toList();
        }));
        this.registerMapper(new Mapper<>(CoordinateArgument.BlockPos.class, BlockPosArgument.blockPos(), CoordinatesImpl::new));
        this.registerMapper(new Mapper<>(CoordinateArgument.ColumnPos.class, ColumnPosArgument.columnPos(), CoordinatesImpl::new));
        this.registerMapper(new Mapper<>(CoordinateArgument.Vec3.class, Vec3Argument.vec3(), CoordinateArgument.Vec3::centerCorrect,  CoordinatesImpl::new));
        this.registerMapper(new Mapper<>(CoordinateArgument.Vec3.class, Vec3Argument.vec3(false), arg -> !arg.centerCorrect(),  CoordinatesImpl::new));
        this.registerMapper(new Mapper<>(CoordinateArgument.Vec2.class, Vec2Argument.vec2(), CoordinateArgument.Vec2::centerCorrect,  CoordinatesImpl::new));
        this.registerMapper(new Mapper<>(CoordinateArgument.Vec2.class, Vec2Argument.vec2(false), arg -> !arg.centerCorrect(),  CoordinatesImpl::new));
        this.registerMapper(new Mapper<>(BlockArgument.class, BlockStateArgument.block(), input -> {
            return CraftBlockStates.getBlockState(input.getState(), /* input.tag TODO expose nbt tag here */null);
        }));
        // block predicate
        this.registerMapper(new Mapper<>(ItemStackArgument.class, ItemArgument.item(), input -> {
            return amount -> CraftItemStack.asBukkitCopy(input.createItemStack(amount, true));
        }));
        // item predicate
        this.registerMapper(new Mapper<>(ColorArgument.class, net.minecraft.commands.arguments.ColorArgument.color(), PaperAdventure::asAdventure));
        this.registerMapper(new Mapper<>(ComponentArgument.class, net.minecraft.commands.arguments.ComponentArgument.textComponent(), PaperAdventure::asAdventure));
        // message
        this.registerMapper(new Mapper<>(ObjectiveArgument.class, net.minecraft.commands.arguments.ObjectiveArgument.objective(), Function.identity()));
        // objective criteria TODO wait on stat and criteria API


        this.registerMapper(new Mapper<>(EnchantmentArgument.class, ItemEnchantmentArgument.enchantment(), byKey(Enchantment::getByKey, Registry.ENCHANTMENT)));


    }

    private static <T, M> Function<M, T> byKey(Function<NamespacedKey, T> fromKey, Registry<M> registry) {
        return nms -> Objects.requireNonNull(fromKey.apply(CraftNamespacedKey.fromMinecraft(Objects.requireNonNull(registry.getKey(nms)))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CommandNode<BukkitBrigadierCommandSource> convert(CommandNode<BukkitBrigadierCommandSource> node, BukkitBrigadierCommand<BukkitBrigadierCommandSource> executor) {
        if (node instanceof LiteralCommandNode<BukkitBrigadierCommandSource> literal) {
            final LiteralCommandNode<BukkitBrigadierCommandSource> newNode = LiteralArgumentBuilder.<BukkitBrigadierCommandSource>literal(literal.getLiteral())
                    .requires(bukkitBrigadierCommandSource -> node.getRequirement().test(bukkitBrigadierCommandSource))
                    .executes(executor).build();
            for (CommandNode<BukkitBrigadierCommandSource> child : literal.getChildren()) {
                newNode.addChild(convert(child, executor));
            }
            return newNode;
        } else if (node instanceof ArgumentCommandNode<BukkitBrigadierCommandSource, ?> argument) {
            @Nullable ArgumentType<?> argumentType = null;
            @Nullable SuggestionProvider<BukkitBrigadierCommandSource> suggestionProvider = executor;
            if (argument.getType() instanceof MinecraftArgument<?> minecraftArgument) {
                argumentType = this.getNativeArgumentType(minecraftArgument);
                if (minecraftArgument.delegateSuggestionsToNativeType()) {
                    suggestionProvider = null;
                }
            }
            final ArgumentCommandNode<BukkitBrigadierCommandSource, ?> newNode = RequiredArgumentBuilder.<BukkitBrigadierCommandSource, Object>argument(argument.getName(), (ArgumentType<Object>) Objects.requireNonNullElseGet(argumentType, StringArgumentType::word))
                    .requires(bukkitBrigadierCommandSource -> node.getRequirement().test(bukkitBrigadierCommandSource))
                    .suggests(suggestionProvider)
                    .executes(executor).build();
            for (CommandNode<BukkitBrigadierCommandSource> child : argument.getChildren()) {
                newNode.addChild(convert(child, executor));
            }
            return newNode;
        } else {
            throw new IllegalStateException(node + " is not a LiteralCommandNode or ArgumentCommandNode");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends MinecraftArgument<V>, V> V parse(StringReader reader, T argumentType) throws CommandSyntaxException {
        for (Mapper<T, V, ?> mapper : this.getMappers((Class<T>) argumentType.getClass())) {
            if (mapper.test(argumentType)) {
                return mapper.parse(reader);
            }
        }
        throw new IllegalArgumentException("Could not find mapper for " + argumentType);
    }

    @Override
    public void registerMapper(Mapper<?, ?, ?> mapper) {
        this.map.put(mapper.apiType(), mapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends MinecraftArgument<V>, V> ArgumentType<?> getNativeArgumentType(T argumentType) {
        for (Mapper<T, V, ?> mapper : this.getMappers((Class<T>) argumentType.getClass())) {
            if (mapper.test(argumentType)) {
                return mapper.nativeType();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends MinecraftArgument<V>, V> Collection<? extends Mapper<T, V, ?>> getMappers(Class<? extends T> argumentTypeClass) {
        return this.map.get(argumentTypeClass).stream().map(m -> (Mapper<T, V, ?>) m).toList();
    }

    @Override
    public BukkitBrigadierCommandSource convertCommandSender(CommandSender sender) {
        return VanillaCommandWrapper.getListener(sender);
    }
}
