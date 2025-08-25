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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    
    private int screenWidth;
    
    private int screenHeight;
    
    public GridView() {
        Rectangle2D screenRect = Screen.getPrimary().getBounds();
        setWidth(screenRect.getWidth());
        setHeight(screenRect.getHeight());
    }
    
    public void initializeState() {
        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        
        screenWidth  = (int)(screenRectangle.getWidth());
        screenHeight = (int)(screenRectangle.getHeight()); 
        
        GridBounds gridBounds = 
                new GridBounds(screenRectangle, cellWidthHeight);
        
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
    
    /**
     * Returns {@code true} if and only if the cursor position {@code (x, y)} 
     * points to a border.
     * 
     * @param x the {@code X}-coordinate of the cursor in pixels.
     * @param y the {@code Y}-coordinate of the cursor in pixels.
     * @return {@code true} if the cursor points to a border.
     */
    public boolean isBorder(int x, int y) {
        if (x <= leftMargin || y <= topMargin) {
            return true;
        }
        
        if (x >= screenWidth  - leftMargin || 
            y >= screenHeight - topMargin) {
            return true;
        }
        
        x -= leftMargin;
        
        if (x % (cellWidthHeight + BORDER_THICKNESS) == 1) {
            return true;
        }
        
        y -= topMargin;
        
        if (y % (cellWidthHeight + BORDER_THICKNESS) == 1) {
            
        }
        
        return false;
    }
    
    public Cell getCellAtCursor(int x, int y) {
        if (isBorder(x, y)) {
            return null;
        }
        
        int cellX = (x - leftMargin) / (cellWidthHeight + BORDER_THICKNESS);
        int cellY = (y - topMargin)  / (cellWidthHeight + BORDER_THICKNESS);
        
        return model.getCell(cellX, cellY);
    }
    
    public void drawDebug(String s) {
        drawBorders();
        drawAllCels();
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.RED);
        
        Font monoFont = Font.font("Monospaced", FontWeight.BOLD, 26);
        gc.setFont(monoFont);
        gc.fillText(s, 100, 100);
    }
}   