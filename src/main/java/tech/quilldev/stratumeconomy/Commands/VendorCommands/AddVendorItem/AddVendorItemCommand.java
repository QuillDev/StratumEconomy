package tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItem;

import com.google.inject.Inject;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.Commands.MarketCommands.MarketCommand;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

public class AddVendorItemCommand extends MarketCommand {

    private final VendorHelper vendorHelper;

    @Inject
    public AddVendorItemCommand(MarketDataRetriever marketDataRetriever, VendorHelper vendorHelper) {
        super(marketDataRetriever);
        this.vendorHelper = vendorHelper;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) return false;
        final var vendor = vendorHelper.getVendor(args[0]);

        //If the vendor is null, tell the user
        if (vendor == null) {
            sender.sendMessage("There is no vendor with that name!");
            return true;
        }

        //Check if the material given is okay
        if (!EnumUtils.isValidEnum(Material.class, args[1].toUpperCase())) {
            sender.sendMessage("Invalid material!");
            return true;
        }

        final var material = Material.valueOf(args[1]);
        final var marketItem = marketDataRetriever.getMarketItem(material);
        if (marketItem == null) {
            sender.sendMessage("There is no data for that market item. (Check the sheet?)");
            return true;
        }

        final var success = vendorHelper.addDynamicItem(vendor, marketItem);
        sender.sendMessage(String.format("%s in adding %s to vendor %s.",
                (success) ? "Succeeded" : "Failed",
                material.name(),
                args[0]
        ));
        return true;
    }
}
