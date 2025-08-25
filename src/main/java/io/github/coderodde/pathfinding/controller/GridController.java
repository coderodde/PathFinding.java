package io.github.coderodde.pathfinding.controller;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.view.GridView;
import java.util.Objects;

/**
 *
 * @author rodio
 */
public final class GridController {
    
    private final GridView view;
    private final GridModel model;
    
    public GridController(GridView view, GridModel model) {
        this.view  = Objects.requireNonNull(view,  "The input view is null");
        this.model = Objects.requireNonNull(model, "The input model is null");
        
        view.setOnMouseMoved(eh -> {
            int cursorX = (int) eh.getSceneX();
            int cursorY = (int) eh.getSceneY();
            Cell cell = view.getCellAtCursor(cursorX,
                                             cursorY);
            
            view.drawDebug(Objects.toString(cell));
        });
    }
}
