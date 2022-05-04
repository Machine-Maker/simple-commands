package me.machinemaker.commands.example;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class EnumArgumentType<E extends Enum<E>> implements ArgumentType<E> {

    private final Class<E> enumClass;
    private final E[] values;

    public EnumArgumentType(Class<E> enumClass) {
        this.enumClass = enumClass;
        this.values = enumClass.getEnumConstants();
    }

    @Override
    public E parse(StringReader reader) throws CommandSyntaxException {
        return Enum.valueOf(this.enumClass, reader.readString().toUpperCase(Locale.ROOT));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        final String input = builder.getRemaining();
        for (E value : this.values) {
            if (value.name().startsWith(input)) {
                builder.suggest(value.name());
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }
}
