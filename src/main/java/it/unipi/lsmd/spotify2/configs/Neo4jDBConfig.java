package it.unipi.lsmd.spotify2.configs;

import it.unipi.lsmd.spotify2.daos.exceptions.DaoException;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

// Configuration class for Neo4j database connection
public class Neo4jDBConfig {

    // Neo4jDB server URL
    private static final String NEO4JDB_URI = "bolt://localhost:7687";

    // Singleton instance of Neo4jDBConfig
    private static Neo4jDBConfig neo = null;

    // Neo4j Driver for database interaction
    private final Driver driver;

    // Private constructor to initialize the Neo4j Driver
    private Neo4jDBConfig() {
        // Create a Neo4j Driver using the provided URI, username, and password
        driver = GraphDatabase.driver(NEO4JDB_URI, AuthTokens.basic("neo4j", "password"));
    }

    // Method to obtain the singleton instance of Neo4jDBConfig
    public static Neo4jDBConfig getInstance() {
        if(neo == null)
            neo = new Neo4jDBConfig();
        return neo;
    }

    // Method to get the Neo4j Driver for database interaction
    public Driver getDriver() {
        if(neo == null)
            throw new DaoException("Neo4j Connection doesn't exist.");
        else
            return neo.driver;
    }

    // Method to close the Neo4j Driver
    public static void closeNeoClient() {
        if(neo != null)
            neo.driver.close();
    }
}
