package it.unipi.lsmd.spotify2.configs;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

// Configuration class for MongoDB connection
public class MongoDBConfig {

    // Your replica set name
    private static final String REPLICA_SET_NAME = "rs0";

    // MongoDB URI with replica set information
    private static final String MONGODB_URI = "mongodb://localhost:27016";/*7,localhost:27018,localhost:27019/?replicaSet="
            + REPLICA_SET_NAME;*/

    // Static instance of MongoClient
    private static MongoClient mongoClient;

    // Method to get the MongoDB client
    public static MongoClient getMongoClient() {
        if (mongoClient == null) {
            // Create a connection string with replica set information
            ConnectionString connectionString = new ConnectionString(MONGODB_URI);

            // Create MongoClientSettings
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .readConcern(ReadConcern.LOCAL)
                    .readPreference(ReadPreference.nearest())
                    .writeConcern(WriteConcern.W1)
                    .writeConcern(WriteConcern.JOURNALED)
                    .build();

            // Create a MongoClient using the MongoClientSettings
            mongoClient = MongoClients.create(mongoClientSettings);
        }
        return mongoClient;
    }

    // Method to close the MongoDB client
    public static void closeMongoClient() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
