package io.github.coderodde.pathfinding.controller;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.view.GridView;
import java.util.Objects;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Rodoin "rodde" Efremov
 * @version 1.0.0 (Aug 25, 2025)
 * @since 1.0.0 (Aug 25, 2025)
 */
public final class GridController {
    
    private GridView view;
    private GridModel model;
    
    public void setGridView(GridView view) {
        this.view = Objects.requireNonNull(view,  "The input view is null");
    }
    
    public void setGridModel(GridModel model) {
        this.model = Objects.requireNonNull(model, "The input model is null");
    }
    
    public void setEventHandlers() {
        view.setOnMouseMoved(eh -> {
            onMousePress(eh);   
        });
    }
    
    private void onMousePress(MouseEvent event) {
        int cursorX = (int) event.getSceneX();
        int cursorY = (int) event.getSceneY();
        Cell cell = view.getCellAtCursor(cursorX,
                                         cursorY);
        view.drawDebug(Objects.toString(cell));
        
        if (cell == null) {
            // Once here, the cursor points to a border or outside of the grid:
            return;
        }
        
        
    }
}
