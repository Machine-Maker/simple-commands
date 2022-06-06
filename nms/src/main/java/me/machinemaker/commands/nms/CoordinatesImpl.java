package me.machinemaker.commands.nms;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import me.machinemaker.commands.api.argument.CoordinateArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;

public class CoordinatesImpl implements CoordinateArgument.Result {

    private final Coordinates coordinates;

    public CoordinatesImpl(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public Vector getPosition(BukkitBrigadierCommandSource commandSource) {
        final Vec3 nms = this.coordinates.getPosition(getStack(commandSource));
        return new Vector(nms.x, nms.y, nms.z);
    }

    @Override
    public float getPitch(BukkitBrigadierCommandSource commandSource) {
        return this.coordinates.getRotation(getStack(commandSource)).y;
    }

    @Override
    public float getYaw(BukkitBrigadierCommandSource commandSource) {
        return this.coordinates.getRotation(getStack(commandSource)).x;
    }

    @Override
    public boolean isXRelative() {
        return this.coordinates.isXRelative();
    }

    @Override
    public boolean isYRelative() {
        return this.coordinates.isYRelative();
    }

    @Override
    public boolean isZRelative() {
        return this.coordinates.isZRelative();
    }

    private static CommandSourceStack getStack(BukkitBrigadierCommandSource commandSource) {
        if (commandSource instanceof CommandSourceStack stack) {
            return stack;
        }
        throw new IllegalArgumentException(commandSource + " is not an instance of " + CommandSourceStack.class.getName());
    }
}
