package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.utils.GenreDistribution;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

// Custom cell factory for GenreDistribution objects in a ListView
public class PlaylistStatsCellFactory implements Callback<ListView<GenreDistribution>, ListCell<GenreDistribution>> {

    // The call method is called to create a new ListCell for each item in the ListView
    @Override
    public ListCell<GenreDistribution> call(ListView<GenreDistribution> param) {
        return new ListCell<>() {

            // The updateItem method is called whenever the item in the list changes
            @Override
            protected void updateItem(GenreDistribution genre, boolean empty) {
                super.updateItem(genre, empty);

                // If the cell is empty or the genre is null, set the text to null
                if (empty || genre == null) {
                    setText(null);
                } else {
                    // Set the text of the cell to the genre and its distribution
                    setText(genre.getGenre() + " | " + genre.getDistribution());
                }
            }
        };
    }
}
