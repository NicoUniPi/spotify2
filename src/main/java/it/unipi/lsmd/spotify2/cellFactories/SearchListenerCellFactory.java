package it.unipi.lsmd.spotify2.cellFactories;

import it.unipi.lsmd.spotify2.dtos.ListenerDTO;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

// Custom cell factory for ListenerDTO objects in a ListView
public class SearchListenerCellFactory implements Callback<ListView<ListenerDTO>, ListCell<ListenerDTO>> {

    // The call method is called to create a new ListCell for each item in the ListView
    @Override
    public ListCell<ListenerDTO> call(ListView<ListenerDTO> param) {
        return new ListCell<>() {

            // The updateItem method is called whenever the item in the list changes
            @Override
            protected void updateItem(ListenerDTO listener, boolean empty) {
                super.updateItem(listener, empty);

                // If the cell is empty or the listener is null, set the text to null
                if (empty || listener == null) {
                    setText(null);
                } else {
                    // Set the text of the cell to the username of the listener
                    setText(listener.getUsername());
                }
            }
        };
    }
}
