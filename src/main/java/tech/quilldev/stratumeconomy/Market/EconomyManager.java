package tech.quilldev.stratumeconomy.Market;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import moe.quill.StratumCommonApi.Database.DataTypes.MarketData;
import moe.quill.StratumCommonApi.Database.IDatabaseService;
import moe.quill.StratumCommonApi.Serialization.ISerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.EconomyKeys;

import java.util.HashMap;

@Singleton
public class EconomyManager {

    private final MarketDataRetriever marketDataRetriever;
    private final EconomyKeys economyKeys;
    private final IDatabaseService databaseService;
    private final ISerializer serializer;
    private final Economy economy;

    private final HashMap<Material, MarketData> marketDataCache = new HashMap<>();
    private final HashMap<Material, MarketItem> marketItemCache = new HashMap<>();

    @Inject
    public EconomyManager(Economy economy,
                          MarketDataRetriever marketDataRetriever,
                          IDatabaseService databaseService,
                          ISerializer serializer,
                          EconomyKeys economyKeys) {
        this.databaseService = databaseService;
        this.marketDataRetriever = marketDataRetriever;
        this.economy = economy;
        this.serializer = serializer;
        this.economyKeys = economyKeys;
        this.updateMarketCache();
    }

    /**
     * Reload the economy manager and update prices
     */
    public void reload() {
        this.marketDataRetriever.reloadMarketValues();
        updateMarketCache();
    }

    /**
     * Update the market cache with new values from the sheet
     */
    public void updateMarketCache() {
        final var marketData = new HashMap<Material, MarketData>();

        for (final var key : marketDataRetriever.getMarketMap().keySet()) {
            final var value = marketDataRetriever.getMarketMap().get(key);

            //Data existing in the db
            final var dbMarketData = databaseService.getMarketData(key);

            //If the existing data does not exist create new data
            if (dbMarketData == null) {
                final var newMarketData = new MarketData(key, 1, 1);
                marketData.put(key, newMarketData);
                databaseService.saveMarketData(newMarketData);
                continue;
            }
            // just use the existing data otherwise
            marketData.put(key, dbMarketData);
        }

        marketDataCache.putAll(marketData);
        updatePrices();
    }

    /**
     * Update market prices to reflect the current trade volumes
     */
    public void updatePrices() {
        marketDataRetriever.getMarketMap().forEach(this::updateMaterialPrice);
    }

    public void updateMaterialPrice(Material mat, MarketItem itemData) {
        final var cacheEntry = marketDataCache.get(mat);

        //Get Buy and Sell prices
        final var baseBuyPrice = itemData.getBaseBuyPrice();
        final var baseSellPrice = itemData.getBaseSellPrice();

        //Get buy and sell volumes
        final var buyAmount = cacheEntry.getBuyAmount();
        final var sellAmount = cacheEntry.getSellAmount();

        //Get the buy / sell ratio
        final var purchaseRatio = (float) buyAmount / (float) sellAmount;

        //Get the new prices of the item
        final var buyPrice = Math.max(0.01f, purchaseRatio * (float) baseBuyPrice);
        final var sellPrice = Math.max(0.01f, purchaseRatio * (float) baseSellPrice);

        MarketItem cachedItem;
        if (!marketItemCache.containsKey(mat)) {
            cachedItem = new MarketItem(mat, buyPrice, sellPrice, false);
            marketItemCache.put(mat, cachedItem);
        } else {
            cachedItem = marketItemCache.get(mat);
            cachedItem.setBuyPrice(buyPrice);
            cachedItem.setSellPrice(sellPrice);
        }


        //Update the price keys on the item
        final var marketItem = cachedItem.getMarketItem();
        final var marketItemMeta = marketItem.getItemMeta();
        final var marketItemDataContainer = marketItemMeta.getPersistentDataContainer();
        marketItemDataContainer.set(economyKeys.buyPriceKey, PersistentDataType.BYTE_ARRAY, serializer.serializeFloat(buyPrice));
        marketItemDataContainer.set(economyKeys.sellPriceKey, PersistentDataType.BYTE_ARRAY, serializer.serializeFloat(sellPrice));
        marketItem.setItemMeta(marketItemMeta);
    }

    public void updateMaterialPrice(Material mat) {
        final var cachedMarketData = marketItemCache.get(mat);
        updateMaterialPrice(mat, cachedMarketData);
    }

    /**
     * Get market data for the given material from the cache
     *
     * @param material to get data for
     * @return the data for that material
     */
    public MarketData getMarketDataFromCache(Material material) {
        return marketDataCache.getOrDefault(material, null);
    }

    /**
     * Increment the buy data for the given material
     *
     * @param material to increment data for
     * @param amount   to add to the buy amount
     */
    public void incrementBuy(Material material, int amount) {
        final var data = getMarketDataFromCache(material);
        if (data == null) return;
        data.setBuyAmount(data.getBuyAmount() + amount);
    }

    /**
     * Increment the sell data for the given material
     *
     * @param material to increment data for
     * @param amount   to add to the sell amount
     */
    public void incrementSell(Material material, int amount) {
        final var data = getMarketDataFromCache(material);
        if (data == null) return;
        data.setSellAmount(data.getSellAmount() + amount);
    }

    /**
     * Get the market item for the given material
     *
     * @param material to check for
     * @return data for that item
     */
    public MarketItem getMarketItem(Material material) {
        return marketItemCache.getOrDefault(material, null);
    }

    /**
     * Save all of the current cached market data to the database
     */
    public void saveMarketPrices() {
        databaseService.saveMarketData(marketDataCache.values());
    }

    public Economy getEconomy() {
        return economy;
    }

    public MarketDataRetriever getMarketDataRetriever() {
        return marketDataRetriever;
    }
}
