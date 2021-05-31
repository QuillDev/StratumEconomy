package tech.quilldev.stratumeconomy.Market;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MarketItem {

    private final Material material;
    private double buyPrice;
    private double sellPrice;
    private boolean isStratum;
    private double baseBuyPrice;
    private double baseSellPrice;
    private ItemStack marketItem;

    public MarketItem(Material material, double buyPrice, double sellPrice, boolean isStratum) {
        this.material = material;
        this.buyPrice = buyPrice;
        this.baseBuyPrice = buyPrice;
        this.baseSellPrice = sellPrice;
        this.sellPrice = sellPrice;
        this.isStratum = isStratum;
        this.marketItem = new ItemStack(material);
    }

    public double getBaseBuyPrice() {
        return baseBuyPrice;
    }

    public double getBaseSellPrice() {
        return baseSellPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public float getBuyPrice() {
        return (float) buyPrice;
    }

    public float getSellPrice() {
        return (float) sellPrice;
    }

    public ItemStack getMarketItem() {
        return marketItem;
    }

    public Material getMaterial() {
        return material;
    }
}
