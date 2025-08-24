package io.github.coderodde.pathfinding.view;

import static io.github.coderodde.pathfinding.Configuration.BORDER_PAINT;
import static io.github.coderodde.pathfinding.Configuration.BORDER_THICKNESS;
import static io.github.coderodde.pathfinding.Configuration.MINIMUM_CELL_WIDTH_HEIGHT;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.GridBounds;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

/**
 * This class implements the grid view.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class GridView extends Canvas {
    
    /**
     * The inner width and height of a cell.
     */
    private int cellWidthHeight;
    
    /**
     * The actual grid data structure that is a matrix of cells.
     */
    private GridModel model;
    
    public GridView() {
        Rectangle2D screenRect = Screen.getPrimary().getBounds();
        setWidth(screenRect.getWidth());
        setHeight(screenRect.getHeight());
    }
    
    @Override
    public boolean isResizable() {
        // Refuse to resize.
        return false;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
    
    public int getCellWidthHeight() {
        return cellWidthHeight;
    }
    
    public void setCellWidthHeight(int cellWidthHeight) {
        this.cellWidthHeight = Math.max(cellWidthHeight,
                                        MINIMUM_CELL_WIDTH_HEIGHT);
    }
    
    /**
     * The actual draw method.
     */
    public void draw() {
        GraphicsContext gc = this.getGraphicsContext2D();
        GridBounds gridBounds = 
                new GridBounds(
                        Screen.getPrimary()
                              .getBounds(), 
                        cellWidthHeight);
        
        int horizontalCells = gridBounds.horizontalCells;
        int verticalCells   = gridBounds.verticalCells;
        
        if (horizontalCells < 1 || verticalCells < 1) {
            throw new IllegalStateException("Should not get here");
        }
        
        int contentWidth  = (int)(horizontalCells 
                          * (cellWidthHeight + BORDER_THICKNESS) 
                          + BORDER_THICKNESS);
        
        int contentHeight = (int)(verticalCells 
                          * (cellWidthHeight + BORDER_THICKNESS) 
                          + BORDER_THICKNESS);
        
        int topMargin  = (int)((getHeight() - contentHeight) / 2.0);
        int leftMargin = (int)((getWidth()  - contentWidth)  / 2.0);
        
        gc.setStroke(BORDER_PAINT);
        gc.setLineWidth(BORDER_THICKNESS);
        
        // Draw verticla borders:
        for (int borderX = 0; borderX <= horizontalCells; ++borderX) {
            int x = leftMargin + borderX * (cellWidthHeight + BORDER_THICKNESS);
            
            gc.strokeLine(x,
                          topMargin, 
                          x,
                          topMargin + contentHeight);
        }
        
        // Draw horizontal borders:
        for (int borderY = 0; borderY <= verticalCells; ++borderY) {
            int y = topMargin + borderY * (cellWidthHeight + BORDER_THICKNESS);
            
            gc.strokeLine(leftMargin, 
                          y,
                          leftMargin + contentWidth, 
                          y);
        }
        
        // Paint the cells:
        for (int y = 0; y < verticalCells; ++y) {
            for (int x = 0; x < horizontalCells; ++x) {
                Cell cell = model.getCell(x, y);
                CellType cellType = cell.getCellType();
                Color color = cellType.getColor();
                gc.setFill(color);
                gc.fillRect(
                        leftMargin +
                                x * (cellWidthHeight + BORDER_THICKNESS) 
                                + BORDER_THICKNESS,
                        
                        topMargin +
                                y * (cellWidthHeight + BORDER_THICKNESS) 
                                + BORDER_THICKNESS,
                        
                        cellWidthHeight,
                        cellWidthHeight);
            }
        }
    }

    public void setGridModel(GridModel model) {
        this.model = model;
    }
}   