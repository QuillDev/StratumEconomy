package tech.quilldev.stratumeconomy.Commands.VendorCommands.RemoveVendorItem;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.Commands.MarketCommands.MarketCommand;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

public class RemoveVendorItem extends MarketCommand {
    public RemoveVendorItem(MarketDataRetriever marketDataRetriever) {
        super(marketDataRetriever);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) return true;
        final var vendor = VendorHelper.getVendor(args[0]);
        if (vendor == null) {
            sender.sendMessage("That vendor does not exist!");
            return true;
        }

        try {
            final var material = Material.valueOf(args[1]);
            final var success = VendorHelper.removeDynamicItem(vendor, material);
            sender.sendMessage(String.format("%s in removing %s from vendor %s.",
                    (success) ? "Succeeded" : "Failed",
                    material.name(),
                    args[0]
            ));
        } catch (Exception ignored) {
            sender.sendMessage("That material does not exist");
            return true;
        }
        return false;
    }
}
