package tech.quilldev.stratumeconomy.Database;

import org.bukkit.Material;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Market.MarketItem;

import java.util.HashMap;
import java.util.Objects;

public class EconomyManager {

    private final MarketDataRetriever marketDataRetriever;
    private final MarketDatabaseManager marketDatabaseManager;

    private final HashMap<Material, MarketData> marketDataCache = new HashMap<>();
    private final HashMap<Material, MarketItem> marketItems = new HashMap<>();

    public EconomyManager(MarketDataRetriever marketDataRetriever, MarketDatabaseManager marketDatabaseManager) {
        this.marketDatabaseManager = marketDatabaseManager;
        this.marketDataRetriever = marketDataRetriever;
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
        this.marketDataRetriever.getMarketMap()
                .keySet()
                .stream()
                .map(marketDatabaseManager::getMarketData)
                .filter(Objects::nonNull)
                .forEach(entry -> marketDataCache.put(entry.getMaterial(), entry));
        updatePrices();
    }

    /**
     * Update market prices to reflect the current trade volumes
     */
    public void updatePrices() {
        marketDataRetriever.getMarketMap().forEach((mat, itemData) -> {
            final var cacheEntry = marketDataCache.get(mat);

            //Get Buy and Sell prices
            final var baseBuyPrice = itemData.getBuyPrice();
            final var baseSellPrice = itemData.getSellPrice();

            //Get buy and sell volumes
            final var buyAmount = cacheEntry.getBuyAmount();
            final var sellAmount = cacheEntry.getSellAmount();
            //Get the buy / sell ratio
            final var purchaseRatio = buyAmount / sellAmount;

            //Get the new prices of the item
            final var buyPrice = Math.max(0.01, purchaseRatio * baseBuyPrice);
            final var sellPrice = Math.max(0.01, purchaseRatio * baseSellPrice);

            marketItems.put(mat, new MarketItem(mat, buyPrice, sellPrice, false));
        });
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
     * Save all of the current cached market data to the database
     */
    public void saveMarketPrices() {
        marketDataCache.values().forEach(marketDatabaseManager::saveMarketData);
    }
}
