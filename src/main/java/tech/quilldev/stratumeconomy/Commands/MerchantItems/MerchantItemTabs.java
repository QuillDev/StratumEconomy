package tech.quilldev.stratumeconomy.Commands.MerchantItems;

import org.bukkit.Nameable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.quilldev.stratumeconomy.MerchantAttributes;

import java.util.List;
import java.util.stream.Collectors;

public class MerchantItemTabs implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return MerchantAttributes
                .merchants
                .stream()
                .map(Nameable::getCustomName)
                .collect(Collectors.toList());
    }
}
