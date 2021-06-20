package tech.quilldev.stratumeconomy.DI;

import com.google.inject.AbstractModule;
import moe.quill.StratumCommonApi.Database.IDatabaseService;
import moe.quill.StratumCommonApi.KeyManager.IKeyManager;
import moe.quill.StratumCommonApi.Serialization.ISerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.Market.EconomyManager;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.StratumEconomy;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

public class PluginModule extends AbstractModule {

    private final Plugin plugin;
    private final IDatabaseService databaseService;
    private final IKeyManager keyManager;
    private final ISerializer serializer;
    private final MarketDataRetriever marketDataRetriever;
    private final EconomyManager economyManager;
    private final Economy economy;
    private final VendorHelper vendorHelper;
    private final EconomyKeys economyKeys;

    public PluginModule(StratumEconomy plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
        this.keyManager = plugin.getKeyManager();
        this.serializer = plugin.getSerializer();
        this.databaseService = plugin.getDatabaseService();
        this.economyKeys = new EconomyKeys(plugin);
        this.marketDataRetriever = new MarketDataRetriever(economyKeys);
        this.economyManager = new EconomyManager(economy, marketDataRetriever, databaseService, serializer, economyKeys);
        this.vendorHelper = new VendorHelper(economyManager, serializer, economyKeys);

    }


    @Override
    protected void configure() {
        bind(Plugin.class).toInstance(plugin);
        bind(IDatabaseService.class).toInstance(databaseService);
        bind(IKeyManager.class).toInstance(keyManager);
        bind(ISerializer.class).toInstance(serializer);
        bind(MarketDataRetriever.class).toInstance(marketDataRetriever);
        bind(EconomyManager.class).toInstance(economyManager);
        bind(Economy.class).toInstance(economy);
        bind(VendorHelper.class).toInstance(vendorHelper);
        bind(EconomyKeys.class).toInstance(economyKeys);
    }
}
