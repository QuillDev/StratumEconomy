package tech.quilldev.stratumeconomy;


import moe.quill.StratumCommon.KeyManager.IKeyManager;
import moe.quill.StratumCommon.Serialization.ISerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.quilldev.stratumeconomy.Commands.MarketCommands.ReloadMarketConfig;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItemCommand;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItemTabs;
import tech.quilldev.stratumeconomy.Market.EconomyManager;
import tech.quilldev.stratumeconomy.Database.MarketDatabaseManager;
import tech.quilldev.stratumeconomy.Events.VendorWindowListener;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

public final class StratumEconomy extends JavaPlugin {

    public static ISerializer serializer;
    private static final Logger logger = LoggerFactory.getLogger(StratumEconomy.class.getSimpleName());

    @Override
    public void onEnable() {
        //Start Stratum serialization
        final var serviceManager = getServer().getServicesManager();
        final var serializeService = serviceManager.getRegistration(ISerializer.class);
        final var keyManagerService = serviceManager.getRegistration(IKeyManager.class);
        if (serializeService == null) {
            return;
        }
        if (keyManagerService == null) {
            return;
        }
        StratumEconomy.serializer = serializeService.getProvider();
        final var keyManager = keyManagerService.getProvider();
        keyManager.getKeyMap().forEach(logger::info);

        final var economyService = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyService == null) return;
        final var economy = economyService.getProvider();
        //Setup the economy keys
        EconomyKeys.init(this);

        final var marketDataRetriever = new MarketDataRetriever();
        final var marketDatabaseManager = new MarketDatabaseManager();
        final var economyManager = new EconomyManager(economy, marketDataRetriever, marketDatabaseManager);
        VendorHelper.init(economyManager);

        /**
         * EVENT REGISTERING
         */
        final var registry = getServer().getPluginManager();
        registry.registerEvents(new VendorWindowListener(economyManager, this), this);

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
