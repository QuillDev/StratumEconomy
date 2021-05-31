package tech.quilldev.stratumeconomy.Commands.MarketCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.Market.EconomyManager;

public class ReloadMarketConfig implements CommandExecutor {

    private EconomyManager economyManager;

    public ReloadMarketConfig(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        economyManager.reload();
        sender.sendMessage("Reloaded market values!");
        return true;
    }
}
