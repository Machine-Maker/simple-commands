package me.machinemaker.commands.api.argument;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public interface CoordinateArgument extends MinecraftArgument<CoordinateArgument.Result> {

    class BlockPos implements CoordinateArgument {
    }

    class ColumnPos implements CoordinateArgument {
    }

    record Vec3(boolean centerCorrect) implements CoordinateArgument {
    }

    record Vec2(boolean centerCorrect) implements CoordinateArgument {
    }

    interface Result {
        Vector getPosition(BukkitBrigadierCommandSource commandSource);

        float getPitch(BukkitBrigadierCommandSource commandSource);

        float getYaw(BukkitBrigadierCommandSource commandSource);

        default Location getLocation(World world, BukkitBrigadierCommandSource commandSource) {
            return this.getPosition(commandSource).toLocation(world);
        }

        boolean isXRelative();

        boolean isYRelative();

        boolean isZRelative();
    }
}
