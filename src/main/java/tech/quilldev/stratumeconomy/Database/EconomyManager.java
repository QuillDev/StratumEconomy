package tech.quilldev.stratumeconomy.Database;


import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Market.MarketItem;
import tech.quilldev.stratumeconomy.StratumEconomy;

import java.util.HashMap;
import java.util.Objects;

public class EconomyManager {

    private final MarketDataRetriever marketDataRetriever;
    private final MarketDatabaseManager marketDatabaseManager;
    private final Economy economy;

    private final HashMap<Material, MarketData> marketDataCache = new HashMap<>();
    private final HashMap<Material, MarketItem> marketItemCache = new HashMap<>();

    public EconomyManager(Economy economy, MarketDataRetriever marketDataRetriever, MarketDatabaseManager marketDatabaseManager) {
        this.marketDatabaseManager = marketDatabaseManager;
        this.marketDataRetriever = marketDataRetriever;
        this.economy = economy;
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
        //TODO: Only update entries that don't exist or have been changed recently, this will
        //TODO: save a LOT of computation power when we add more items to the list
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
            marketItemDataContainer.set(EconomyKeys.buyPriceKey, PersistentDataType.BYTE_ARRAY, StratumEconomy.serializer.serializeFloat((float) buyPrice));
            marketItemDataContainer.set(EconomyKeys.sellPriceKey, PersistentDataType.BYTE_ARRAY, StratumEconomy.serializer.serializeFloat((float) sellPrice));
            marketItem.setItemMeta(marketItemMeta);
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
        marketDataCache.values().forEach(marketDatabaseManager::saveMarketData);
    }

    public Economy getEconomy() {
        return economy;
    }

    public MarketDataRetriever getMarketDataRetriever() {
        return marketDataRetriever;
    }
}
