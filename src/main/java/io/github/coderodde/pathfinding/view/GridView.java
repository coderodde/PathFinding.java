package io.github.coderodde.pathfinding.view;

import static io.github.coderodde.pathfinding.Configuration.BORDER_PAINT;
import static io.github.coderodde.pathfinding.Configuration.BORDER_THICKNESS;
import static io.github.coderodde.pathfinding.Configuration.MINIMUM_CELL_WIDTH_HEIGHT;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.GridBounds;
import io.github.coderodde.pathfinding.utils.Cell;
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
    
    private int topMargin;
    
    private int leftMargin;
    
    private int horizontalCells;
    
    private int verticalCells;
    
    private int contentWidth;
    
    private int contentHeight;
    
    public GridView() {
        Rectangle2D screenRect = Screen.getPrimary().getBounds();
        setWidth(screenRect.getWidth());
        setHeight(screenRect.getHeight());
    }
    
    public void initializeState() {
        GridBounds gridBounds = 
                new GridBounds(
                        Screen.getPrimary()
                              .getBounds(), 
                        cellWidthHeight);
        System.out.println("cds " + cellWidthHeight);
        horizontalCells = gridBounds.horizontalCells;
        verticalCells   = gridBounds.verticalCells;
        
        contentWidth  = (int)(horizontalCells 
                      * (cellWidthHeight + BORDER_THICKNESS) 
                      + BORDER_THICKNESS);
        
        contentHeight = (int)(verticalCells 
                      * (cellWidthHeight + BORDER_THICKNESS) 
                      + BORDER_THICKNESS);
        
        topMargin  = (int)((getHeight() - contentHeight) / 2.0);
        leftMargin = (int)((getWidth()  - contentWidth)  / 2.0);
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
    
    public void drawAllCels() {
        for (int y = 0; y < verticalCells; ++y) {
            for (int x = 0; x < horizontalCells; ++x) {
                drawCell(model.getCell(x, y));
            }
        }
    }
    
    public void drawCell(Cell cell) {
        Color color = cell.getCellType().getColor();
        GraphicsContext gc = getGraphicsContext2D();
        
        gc.setFill(color);
        gc.fillRect(
                leftMargin +
                        cell.getx() * (cellWidthHeight + BORDER_THICKNESS) 
                        + BORDER_THICKNESS,

                topMargin +
                        cell.gety() * (cellWidthHeight + BORDER_THICKNESS) 
                        + BORDER_THICKNESS,

                cellWidthHeight,
                cellWidthHeight);
    }
    
    public void drawBorders() {
        GraphicsContext gc = getGraphicsContext2D();
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
    }

    public void setGridModel(GridModel model) {
        this.model = model;
    }
}   