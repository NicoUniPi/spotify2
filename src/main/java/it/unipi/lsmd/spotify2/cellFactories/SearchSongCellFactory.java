package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.dtos.SongDTO;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

// Custom cell factory for SongDTO objects in a ListView
public class SearchSongCellFactory implements Callback<ListView<SongDTO>, ListCell<SongDTO>> {

    // The call method is called to create a new ListCell for each item in the ListView
    @Override
    public ListCell<SongDTO> call(ListView<SongDTO> param) {
        return new ListCell<>() {

            // The updateItem method is called whenever the item in the list changes
            @Override
            protected void updateItem(SongDTO song, boolean empty) {
                super.updateItem(song, empty);

                // If the cell is empty or the song is null, set the text to null
                if (empty || song == null) {
                    setText(null);
                } else {
                    // Set the text of the cell to the title of the song
                    setText(song.getTitle());
                }
            }
        };
    }
}
