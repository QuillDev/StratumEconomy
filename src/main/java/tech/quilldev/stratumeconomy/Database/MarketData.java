package tech.quilldev.stratumeconomy.Database;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Material;

import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class MarketData {

    private final Material material;
    private int buyAmount;
    private int sellAmount;
    private int volume = 0;


    public MarketData(Material material, int buyAmount, int sellAmount) {
        this.material = material;
        this.buyAmount = buyAmount;
        this.sellAmount = sellAmount;
    }

    public MarketData(Document marketDataDocument) {
        this.material = Material.valueOf(marketDataDocument.getString(MarketDataKey.MATERIAL.name()).toUpperCase());
        this.buyAmount = marketDataDocument.getInteger(MarketDataKey.BUY_AMOUNT.name());
        this.sellAmount = marketDataDocument.getInteger(MarketDataKey.SELL_AMOUNT.name());
        this.volume = marketDataDocument.getInteger(MarketDataKey.VOLUME.name());
    }

    public Bson getUpdateBson() {
        return combine(
                set(MarketDataKey.BUY_AMOUNT.name(), buyAmount),
                set(MarketDataKey.SELL_AMOUNT.name(), sellAmount),
                set(MarketDataKey.VOLUME.name(), buyAmount + sellAmount)
        );
    }


    /**
     * Getters and setters
     */
    public Material getMaterial() {
        return material;
    }

    public int getVolume() {
        return volume;
    }

    public int getBuyAmount() {
        return buyAmount;
    }

    public int getSellAmount() {
        return sellAmount;
    }

    public void setBuyAmount(int buyAmount) {
        this.buyAmount = buyAmount;
    }

    public void setSellAmount(int sellAmount) {
        this.sellAmount = sellAmount;
    }
}
