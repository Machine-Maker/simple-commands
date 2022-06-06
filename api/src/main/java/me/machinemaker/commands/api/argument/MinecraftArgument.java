package me.machinemaker.commands.api.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.machinemaker.commands.api.brigadier.BrigadierConverter;
import net.kyori.adventure.util.Services;

public interface MinecraftArgument<T> extends ArgumentType<T> {

    BrigadierConverter CONVERTER = Services.service(BrigadierConverter.Provider.class).orElseThrow().get();

    @Override
    default T parse(StringReader reader) throws CommandSyntaxException {
        return CONVERTER.parse(reader, this);
    }

    default boolean delegateSuggestionsToNativeType() {
        return true;
    }
}
