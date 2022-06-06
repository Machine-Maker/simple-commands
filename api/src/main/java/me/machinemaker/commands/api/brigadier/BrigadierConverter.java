package me.machinemaker.commands.api.brigadier;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommand;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.machinemaker.commands.api.argument.MinecraftArgument;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public interface BrigadierConverter {

    /**
     * Maps argument types to native brigadier/minecraft types
     *
     * @param apiNode node from a command created with this api
     * @param command brigadier command
     * @return the converted command node that can be sent to the player
     * @throws IllegalArgumentException if apiNode isn't a {@link LiteralCommandNode} or {@link com.mojang.brigadier.tree.ArgumentCommandNode}
     */
    CommandNode<BukkitBrigadierCommandSource> convert(CommandNode<BukkitBrigadierCommandSource> apiNode, BukkitBrigadierCommand<BukkitBrigadierCommandSource> command);

    <T extends MinecraftArgument<V>, V> V parse(StringReader reader, T argumentType) throws CommandSyntaxException;

    <T extends MinecraftArgument<V>, V> Collection<? extends Mapper<T, V, ?>> getMappers(Class<? extends T> argumentType);

    /**
     * Registers a new argument type mapper.
     *
     * @param mapper the mapper
     */
    void registerMapper(Mapper<?, ?, ?> mapper);

    <T extends MinecraftArgument<V>, V> @Nullable ArgumentType<?> getNativeArgumentType(T argumentType);

    BukkitBrigadierCommandSource convertCommandSender(CommandSender sender);

    interface Provider {

        BrigadierConverter get();
    }

    record Mapper<T extends MinecraftArgument<V>, V, M>(
            Class<T> apiType,
            ArgumentType<M> nativeType,
            Predicate<T> validCheck,
            Function<M, V> convert
    ) implements Predicate<T> {

        public Mapper(Class<T> apiType, ArgumentType<M> nativeType, Function<M, V> convert) {
            this(apiType, nativeType, ignored -> true, convert);
        }

        public V parse(StringReader reader) throws CommandSyntaxException {
            return this.convert.apply(this.nativeType.parse(reader));
        }

        @Override
        public boolean test(T argument) {
            return this.validCheck.test(argument);
        }
    }
}
