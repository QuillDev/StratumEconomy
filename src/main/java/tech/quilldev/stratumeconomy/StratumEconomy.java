package tech.quilldev.stratumeconomy;


import com.google.inject.Guice;
import com.google.inject.Inject;
import moe.quill.StratumCommon.Commands.StratumCommand;
import moe.quill.StratumCommon.Database.IDatabaseService;
import moe.quill.StratumCommon.KeyManager.IKeyManager;
import moe.quill.StratumCommon.Plugin.StratumConfig;
import moe.quill.StratumCommon.Plugin.StratumConfigBuilder;
import moe.quill.StratumCommon.Plugin.StratumPlugin;
import moe.quill.StratumCommon.Serialization.ISerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.quilldev.stratumeconomy.Commands.MarketCommands.ReloadMarketConfig;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItem.AddVendorItemCommand;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.AddVendorItem.AddVendorItemTabs;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.RemoveVendorItem.RemoveVendorItem;
import tech.quilldev.stratumeconomy.Commands.VendorCommands.RemoveVendorItem.RemoveVendorItemTabs;
import tech.quilldev.stratumeconomy.DI.PluginModule;
import tech.quilldev.stratumeconomy.Events.VendorWindowListener;

public final class StratumEconomy extends StratumPlugin {

    private static final Logger logger = LoggerFactory.getLogger(StratumEconomy.class.getSimpleName());

    //Inject listeners
    @Inject
    VendorWindowListener vendorWindowListener;

    //Inject commands
    @Inject
    ReloadMarketConfig reloadMarketConfig;
    @Inject
    AddVendorItemCommand addVendorItemCommand;
    @Inject
    AddVendorItemTabs addVendorItemTabs;
    @Inject
    RemoveVendorItem removeVendorItem;
    @Inject
    RemoveVendorItemTabs removeVendorItemTabs;

    public StratumEconomy() {
        super(new StratumConfigBuilder()
                .setUseDatabase(true)
                .setUseKeyManager(true)
                .setUseSerialization(true)
                .build()
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        final var economyService = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyService == null) return;
        final var economy = economyService.getProvider();

        final var injector = Guice.createInjector(new PluginModule(this, economy));
        injector.injectMembers(this);

        registerEvent(vendorWindowListener);

        registerCommand(
                new StratumCommand("reloadmarket", reloadMarketConfig, null),
                new StratumCommand("addvendoritem", addVendorItemCommand, addVendorItemTabs),
                new StratumCommand("removevendoritem", removeVendorItem, removeVendorItemTabs)
        );

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
