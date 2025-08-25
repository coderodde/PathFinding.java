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
        UNDRAW_WALL
    }
    
    private DrawMode drawMode = DrawMode.NONE;
    
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
        Cell cell = accessCellViaEvent(event);
        
        if (cell == null) {
            // We are pointing at a border or magin:
            return;
        }
        
        int x = cell.getx();
        int y = cell.gety();
        
        switch (drawMode) {
            case DRAW_WALL -> {
                if (cell.getCellType().equals(CellType.SOURCE)) {
                    // Don't draw the wall on top of cell occupied by source.
                    // Just draw the wall below it:
                    model.setSourceCellCoversWallCell(true);
                } else if (cell.getCellType().equals(CellType.TARGET)) {
                    // Don't draw the wall on top of cell occupied by target.
                    // Just draw the wall below it:
                    model.setTargetCellCoversWallCell(true);
                } else {
                    model.setCellType(x, y, CellType.WALL);
                }
            }
            
            case UNDRAW_WALL -> {
                if (cell.getCellType().equals(CellType.SOURCE)) {
                    model.setSourceCellCoversWallCell(false);
                } else if (cell.getCellType().equals(CellType.TARGET)) {
                    model.setTargetCellCoversWallCell(false);
                } else {
                    model.setCellType(x, y, CellType.FREE);
                }
            }
            
            case MOVE_SOURCE -> model.moveSource(x, y);
            case MOVE_TARGET -> model.moveTarget(x, y);
                
//            default -> // "Handle" the case NONE:
//                throw new IllegalStateException("Should not get here");
        }
        
        view.drawAllCels();
    }
    
    private Cell accessCellViaEvent(MouseEvent event) {
        int cursorX = (int) event.getSceneX();
        int cursorY = (int) event.getSceneY();
        return view.getCellAtCursor(cursorX,
                                    cursorY);
    }
    
    private void onMouseReleased(MouseEvent event) {
        drawMode = DrawMode.NONE;
    }
    
    private void onMousePressed(MouseEvent event) {
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
                drawMode = DrawMode.UNDRAW_WALL;
                
                model.setCellType(
                        cell.getx(),
                        cell.gety(),
                        CellType.FREE);
            }
                
            case SOURCE -> drawMode = DrawMode.MOVE_SOURCE;
            case TARGET -> drawMode = DrawMode.MOVE_TARGET;
                
            default -> // "Handle" OPENED, VISITED, TRACED:
                throw new IllegalStateException("Should not get here");
        }
    }
}
