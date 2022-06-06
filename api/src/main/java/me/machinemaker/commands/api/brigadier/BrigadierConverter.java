package me.machinemaker.commands.api.brigadier;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommand;
import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.machinemaker.commands.api.arguments.MinecraftArgument;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.List;
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
    CommandNode<BukkitBrigadierCommandSource> convert(CommandNode<CommandSender> apiNode, BukkitBrigadierCommand<BukkitBrigadierCommandSource> command);

    <T extends MinecraftArgument<V>, V> V parse(StringReader reader, T argumentType) throws CommandSyntaxException;

    <T extends MinecraftArgument<V>, V> Collection<? extends Mapper<V, ?>> getMappers(Class<? extends T> argumentType);

    /**
     * Registers a new argument type mapper.
     *
     * @param mapper the mapper
     */
    void registerMapper(Mapper<?, ?> mapper);

    <T extends MinecraftArgument<V>, V> @Nullable ArgumentType<?> getNativeArgumentType(T argumentType);

    interface Provider {

        BrigadierConverter get();
    }

    record Mapper<T, M>(
            Class<? extends MinecraftArgument<T>> apiType,
            ArgumentType<M> nativeType,
            Predicate<MinecraftArgument<T>> validCheck,
            Function<M, T> convert
    ) implements Predicate<MinecraftArgument<T>> {

        public Mapper(Class<? extends MinecraftArgument<T>> apiType, ArgumentType<M> nativeType, Function<M, T> convert) {
            this(apiType, nativeType, ignored -> true, convert);
        }

        public T parse(StringReader reader) throws CommandSyntaxException {
            return this.convert.apply(this.nativeType.parse(reader));
        }

        @Override
        public boolean test(MinecraftArgument<T> argument) {
            return this.validCheck.test(argument);
        }
    }
}
