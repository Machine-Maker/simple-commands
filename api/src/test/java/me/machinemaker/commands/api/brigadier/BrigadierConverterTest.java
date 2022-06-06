package me.machinemaker.commands.api.brigadier;

import net.kyori.adventure.util.Services;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BrigadierConverterTest {

    @Test
    void testBrigadierConverterExists() {
        assertNotNull(Services.service(BrigadierConverter.Provider.class).orElseThrow().get());
    }
}
