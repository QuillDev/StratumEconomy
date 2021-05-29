package tech.quilldev.stratumeconomy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import tech.quilldev.stratumeconomy.Commands.MarketCommands.ReloadMarketConfig;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItemCommand;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItemTabs;
import tech.quilldev.stratumeconomy.Database.EconomyManager;
import tech.quilldev.stratumeconomy.Database.MarketDatabaseManager;
import tech.quilldev.stratumeconomy.Events.OpenVendorWindow;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;

public final class StratumEconomy extends JavaPlugin {

    @Override
    public void onEnable() {
        final var economyService = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyService == null) return;
        final var economy = economyService.getProvider();
        //Setup the economy keys
        EconomyKeys.init(this);

        final var marketDataRetriever = new MarketDataRetriever();
        final var marketDatabaseManager = new MarketDatabaseManager();
        final var economyManager = new EconomyManager(marketDataRetriever, marketDatabaseManager);

        /**
         * EVENT REGISTERING
         */
        final var registry = getServer().getPluginManager();
        registry.registerEvents(new OpenVendorWindow(marketDataRetriever, this), this);

        /**
         * Command Setup + configuration
         */
        final var reloadMarketCommand = getCommand("reloadmarket");
        if (reloadMarketCommand != null) {
            reloadMarketCommand.setExecutor(new ReloadMarketConfig(economyManager));
        }

        final var addVendorItemCommand = getCommand("addvendoritem");
        if (addVendorItemCommand != null) {
            addVendorItemCommand.setExecutor(new AddVendorItemCommand(marketDataRetriever));
            addVendorItemCommand.setTabCompleter(new AddVendorItemTabs(marketDataRetriever));
        }
    }

    @Override
    public void onDisable() {
    }
}
