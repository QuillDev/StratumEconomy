package tech.quilldev.stratumeconomy.Commands.VendorCommands.RemoveVendorItem;

import com.google.inject.Inject;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.Commands.MarketCommands.MarketCommand;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

public class RemoveVendorItem extends MarketCommand {

    private final VendorHelper vendorHelper;

    @Inject
    public RemoveVendorItem(MarketDataRetriever marketDataRetriever, VendorHelper vendorHelper) {
        super(marketDataRetriever);
        this.vendorHelper = vendorHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) return true;
        final var vendor = vendorHelper.getVendor(args[0]);
        if (vendor == null) {
            sender.sendMessage("That vendor does not exist!");
            return true;
        }

        try {
            final var material = Material.valueOf(args[1]);
            final var success = vendorHelper.removeDynamicItem(vendor, material);
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
