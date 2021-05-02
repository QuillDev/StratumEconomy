package tech.quilldev.stratumeconomy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import tech.quilldev.stratumeconomy.Commands.AddMerchantItem;
import tech.quilldev.stratumeconomy.Commands.SpawnMerchant;
import tech.quilldev.stratumeconomy.Events.MerchantInteractEvent;
import tech.quilldev.stratumeconomy.Events.MerchantShop;

import java.util.Objects;
import java.util.logging.Logger;

public final class StratumEconomy extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private static Economy economy = null;

    @Override
    public void onEnable() {
        MerchantAttributes.init(this);

        final var pluginManager = getServer().getPluginManager();
        // Plugin startup logic
        if (!setupEconomy()) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependancy found!", getDescription().getName()));
            pluginManager.disablePlugin(this);
            return;
        }

        pluginManager.registerEvents(new MerchantInteractEvent(), this);
        pluginManager.registerEvents(new MerchantShop(economy), this);
        Objects.requireNonNull(getCommand("spawnmerchant")).setExecutor(new SpawnMerchant());
        Objects.requireNonNull(getCommand("additem")).setExecutor(new AddMerchantItem());
    }

    @Override
    public void onDisable() {
        logger.info(String.format("[%s] Disabled version %s", getDescription().getName(), getDescription().getVersion()));
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        var rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }
}