package it.unipi.lsmd.spotify2;

import it.unipi.lsmd.spotify2.configs.MongoDBConfig;
import it.unipi.lsmd.spotify2.configs.Neo4jDBConfig;
import it.unipi.lsmd.spotify2.pageManagement.PageManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.image.Image;

public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(PageManager.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Set the icon of the primary stage
        primaryStage.getIcons().add(new Image("img/logoSpotify2.png"));

        PageManager pageManager = new PageManager(primaryStage);

        // Register a shutdown hook to close the MongoDB client when the application is terminated
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Closing Mongo driver...");
            MongoDBConfig.closeMongoClient();

            logger.info("Closing Neo4j driver...");
            Neo4jDBConfig.closeNeoClient();
        }));

        pageManager.switchToSignIn(); // Start with the sign-in page
    }
}