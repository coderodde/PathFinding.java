package io.github.coderodde.pathfinding.controller;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
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
    
    private enum DrawMode {
        NONE,
        MOVE_SOURCE,
        MOVE_TARGET,
        DRAW_WALL,
        ERASE_WALL
    }
    
    /**
     * The drawing mode of this controller.
     */
    private DrawMode drawMode = DrawMode.NONE;
    
    /**
     * The flag marking whether the user interaction such as drawing/erasing the
     * walls and moving the terminal cells is enabled or not.
     */
    private boolean userInteractionEnabled = true;
    
    public boolean isUserInteractionEnabled() {
        return userInteractionEnabled;
    }
    
    public void enableUserInteraction() {
        userInteractionEnabled = true;
    }
    
    public void disableUserInteraction() {
        drawMode = DrawMode.NONE;
        userInteractionEnabled = false;
    }
    
    public void setGridView(GridView view) {
        this.view = Objects.requireNonNull(view,  "The input view is null");
    }
    
    public void setGridModel(GridModel model) {
        this.model = Objects.requireNonNull(model, "The input model is null");
    }
    
    public void setEventHandlers() {
        view.setOnMousePressed(eh -> {
            onMousePressed(eh);
        });
        
        view.setOnMouseReleased(eh -> {
            onMouseReleased(eh);   
        });
        
        view.setOnMouseDragged(eh -> {
            onMouseDrag(eh);
        });
    }
    
    private void onMouseDrag(MouseEvent event) {
        if (!userInteractionEnabled) {
            // User interaction not allowed. Return with NO-OP.
            return;
        }
        
        Cell cell = accessCellViaEvent(event);
        
        if (cell == null) {
            // We are pointing at a border or magin:
            return;
        }
        
        switch (drawMode) {
            case DRAW_WALL   -> drawWall(cell);
            case ERASE_WALL  -> eraseWall(cell);
            case MOVE_SOURCE -> moveSource(cell);
            case MOVE_TARGET -> moveTarget(cell);
        }
    }
    
    private void drawWall(Cell cell) {
        int x = cell.getx();
        int y = cell.gety();
                
        switch (cell.getCellType()) {
            case SOURCE:
                // Don't draw the wall on top of cell occupied by source.
                // Just draw the wall below it:
                model.setSourceCellCoversWallCell(true);
                break;
                
            case TARGET:
                // Don't draw the wall on top of cell occupied by target.
                // Just draw the wall below it:
                model.setTargetCellCoversWallCell(true);
                break;
                
            default:
                model.setCellType(x, y, CellType.WALL);
                break;
        }
    }
    
    private void moveSource(Cell cell) {
        model.moveSource(cell.getx(), 
                         cell.gety());
    }
    
    private void moveTarget(Cell cell) {
        model.moveTarget(cell.getx(), 
                         cell.gety());
    }
    
    private void eraseWall(Cell cell) {
        int x = cell.getx();
        int y = cell.gety();
                
        switch (cell.getCellType()) {
            case SOURCE:
                model.setSourceCellCoversWallCell(false);
                break;
                
            case TARGET:
                model.setTargetCellCoversWallCell(false);
                break;
                
            default:
                model.setCellType(x, y, CellType.FREE);
                break;
        }
    }
    
    private Cell accessCellViaEvent(MouseEvent event) {
        int cursorX = (int) event.getSceneX();
        int cursorY = (int) event.getSceneY();
        return view.getCellAtCursor(cursorX,
                                    cursorY);
    }
    
    private void onMouseReleased(MouseEvent event) {
        if (userInteractionEnabled) {
            drawMode = DrawMode.NONE;
        }
    }
    
    private void onMousePressed(MouseEvent event) {
        if (!userInteractionEnabled) {
            // No user interaction allowed. Return with NO-OP.
            return;
        }
        
        Cell cell = accessCellViaEvent(event);
        
        if (cell == null) {
            // Pressed at the border or margin:
            return;
        }
        
        switch (cell.getCellType()) {
            case FREE -> {
                drawMode = DrawMode.DRAW_WALL;
                
                model.setCellType(
                        cell.getx(),
                        cell.gety(),
                        CellType.WALL);
            }
                
            case WALL -> {
                drawMode = DrawMode.ERASE_WALL;
                
                model.setCellType(
                        cell.getx(),
                        cell.gety(),
                        CellType.FREE);
            }
                
            case SOURCE -> drawMode = DrawMode.MOVE_SOURCE;
            case TARGET -> drawMode = DrawMode.MOVE_TARGET;
            
            case VISITED, OPENED, TRACED -> model.setCellType(cell, 
                                                              CellType.FREE);
        }   
    }
}
