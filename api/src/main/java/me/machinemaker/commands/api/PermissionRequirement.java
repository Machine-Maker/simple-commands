package me.machinemaker.commands.api;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public record PermissionRequirement(String permission, BiPredicate<CommandSender, String> permissionCheck) implements Predicate<CommandSender> {

    @Override
    public boolean test(CommandSender s) {
        return this.permissionCheck.test(s, this.permission);
    }

    public static <S extends Permissible> PermissionRequirement of(Permission permission) {
        return of(permission.getName());
    }

    public static <S extends Permissible> PermissionRequirement of(String permission) {
        return new PermissionRequirement(permission, CommandSender::hasPermission);
    }
}
