package tech.quilldev.stratumeconomy;

import com.google.inject.Singleton;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

@Singleton
public class EconomyKeys {

    public NamespacedKey vendorKey;
    public NamespacedKey vendorInventory;
    public NamespacedKey isDynamic;
    public NamespacedKey buyPriceKey;
    public NamespacedKey sellPriceKey;
    public NamespacedKey baseItemKey;

    public EconomyKeys(Plugin plugin) {
        this.vendorKey = new NamespacedKey(plugin, "npc_vendor");
        this.vendorInventory = new NamespacedKey(plugin, "npc_vendor_inventory");
        this.isDynamic = new NamespacedKey(plugin, "market_is_dynamic");
        this.buyPriceKey = new NamespacedKey(plugin, "market_buy_price_key");
        this.sellPriceKey = new NamespacedKey(plugin, "sell_price_key");
        this.baseItemKey = new NamespacedKey(plugin, "market_base_item_key");
    }

}
