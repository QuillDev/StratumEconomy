package tech.quilldev.stratumeconomy;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class EconomyKeys {

    public static NamespacedKey vendorKey;
    public static NamespacedKey vendorInventory;
    public static NamespacedKey isDynamic;
    public static NamespacedKey buyPriceKey;
    public static NamespacedKey sellPriceKey;
    public static NamespacedKey baseItemKey;

    public static void init(Plugin plugin) {
        vendorKey = new NamespacedKey(plugin, "npc_vendor");
        vendorInventory = new NamespacedKey(plugin, "npc_vendor_inventory");
        isDynamic = new NamespacedKey(plugin, "market_is_dynamic");
        buyPriceKey = new NamespacedKey(plugin, "market_buy_price_key");
        sellPriceKey = new NamespacedKey(plugin, "sell_price_key");
        baseItemKey = new NamespacedKey(plugin, "market_base_item_key");
    }
}
