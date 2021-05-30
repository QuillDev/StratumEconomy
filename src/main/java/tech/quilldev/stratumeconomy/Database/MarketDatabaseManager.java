package tech.quilldev.stratumeconomy.Database;


import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bukkit.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketDatabaseManager {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> marketCollection;
    private static final Logger logger = LoggerFactory.getLogger(MarketDatabaseManager.class.getSimpleName());

    public MarketDatabaseManager() {

        this.mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .build());

        this.database = mongoClient.getDatabase("StratumMarket");
        this.marketCollection = this.database.getCollection("marketData");
    }

    /**
     * Get market data for the given material
     *
     * @param material to get market data for
     * @return the data for the given material
     */
    public MarketData getMarketData(Material material) {
        final var marketDataDocument = findMarketData(material);

        //If there is no entry for this item ad one
        if (marketDataDocument == null) {
            final var newMarketDataDocument = createNewDataEntry(material);

            if (!newMarketDataDocument.isEmpty()) {
                return new MarketData(newMarketDataDocument);
            }

            return null;
        }

        return new MarketData(marketDataDocument);
    }

    public void saveMarketData(MarketData marketData) {
        //Try to update the player
        marketCollection.findOneAndUpdate(
                Filters.eq("material", marketData.getMaterial().name()),
                marketData.getUpdateBson()
        );
    }

    /**
     * Create a new data entry for the given material
     *
     * @param material to create an entry for
     * @return the entry for that material
     */
    public Document createNewDataEntry(Material material) {

        final var existingMarketData = findMarketData(material);
        if (existingMarketData != null) {
            logger.error(String.format("Tried to add material that already exists! Name: %s", material.name()));
            return new Document();
        }

        // Create the marketData document here
        final var marketData = new Document()
                .append(MarketDataKey.MATERIAL.name(), material.name())
                .append(MarketDataKey.BUY_AMOUNT.name(), 1)
                .append(MarketDataKey.SELL_AMOUNT.name(), 1)
                .append(MarketDataKey.VOLUME.name(), 0);

        //try to enter the marketData into the collection
        final var result = marketCollection.insertOne(marketData);

        //If it was acknowledged return the marketData document we inserted
        if (result.wasAcknowledged()) {
            logger.info(String.format("Material: %s was added to the database.", material.name()));
            return marketData;
        }

        logger.error(String.format("Failed to add material %s to the database!", material.name()));
        return new Document();
    }

    /**
     * Get market data for the given material
     *
     * @param material to get market data for
     * @return the document for that item
     */
    public Document findMarketData(Material material) {
        return marketCollection.find(Filters.eq(MarketDataKey.MATERIAL.name(), material.name())).first();
    }
}
