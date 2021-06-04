package tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItem;

import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

import java.util.List;
import java.util.stream.Collectors;

public class AddVendorItemTabs implements TabCompleter {

    private final MarketDataRetriever marketDataRetriever;
    private final VendorHelper vendorHelper;

    @Inject
    public AddVendorItemTabs(MarketDataRetriever marketDataRetriever, VendorHelper vendorHelper) {
        this.marketDataRetriever = marketDataRetriever;
        this.vendorHelper = vendorHelper;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return vendorHelper.getVendorNames()
                    .stream()
                    .filter(name -> name.toLowerCase().contains(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return marketDataRetriever.getMarketMap().keySet().stream().map(Enum::name).collect(Collectors.toList());
        }
        return null;
    }
}
