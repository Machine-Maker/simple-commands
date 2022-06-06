package me.machinemaker.commands.nms;

import me.machinemaker.commands.api.brigadier.BrigadierConverter;

public class BrigadierConverterProviderImpl implements BrigadierConverter.Provider {

    private final BrigadierConverter INSTANCE = new BrigadierConverterImpl();

    @Override
    public BrigadierConverter get() {
        return INSTANCE;
    }
}
