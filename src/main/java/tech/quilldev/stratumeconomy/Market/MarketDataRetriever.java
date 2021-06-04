package tech.quilldev.stratumeconomy.Market;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.EconomyKeys;

import java.util.HashMap;

@Singleton
public class MarketDataRetriever {

    private final static GoogleSheetReader sheetReader = new GoogleSheetReader();
    private final HashMap<Material, MarketItem> marketMap = new HashMap<>();

    private final EconomyKeys economyKeys;

    @Inject
    public MarketDataRetriever(EconomyKeys economyKeys) {
        this.economyKeys = economyKeys;
        reloadMarketValues();
    }

    public void reloadMarketValues() {
        marketMap.putAll(sheetReader.loadMarketDataFromUrl("1zoDrgF_glGS3vYCzkBJL_a50UUX3m8TcnC0Pjt9Yh6U", "Prices"));
    }


    /**
     * Get the given market item
     *
     * @param material to get the market data for
     * @return the market item with the given data
     */
    public MarketItem getMarketItem(Material material) {
        return marketMap.getOrDefault(material, null);
    }

    /**
     * Get the market item from the current item stack
     *
     * @param item to get market item from
     * @return the market item
     */
    public MarketItem getMarketItem(ItemStack item) {
        if (item == null) return null;
        final var itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return null;
        }
        final var itemData = itemMeta.getPersistentDataContainer();
        if (!itemData.has(economyKeys.isDynamic, PersistentDataType.BYTE_ARRAY)) {
            return null;
        }
        //If the data is good, get the market data
        return getMarketItem(item.getType());
    }

    /**
     * Get a map of all market items
     *
     * @return a map of market items
     */
    public HashMap<Material, MarketItem> getMarketMap() {
        return marketMap;
    }
}
