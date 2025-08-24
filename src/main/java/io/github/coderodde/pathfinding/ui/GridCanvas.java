package io.github.coderodde.pathfinding.ui;

import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class GridCanvas extends Canvas {
    
    private static final int MINIMUM_CELL_WIDTH_HEIGHT = 20;
    private static final int BORDER_THICKNESS = 1;
    private static final Paint BORDER_PAINT = Color.web("#555555");
    private static final float LEFT_SOURCE_SHIFT = 0.25f;
    
    private final class GridBounds {
        final int horizontalCells;
        final int verticalCells;
        
        GridBounds(Rectangle2D rect) {

            int w = (int) rect.getWidth();
            int h = (int) rect.getHeight();

            w -= BORDER_THICKNESS; // Don't count the border on the right.
            h -= BORDER_THICKNESS; // Don't count the border at the top.

            this.horizontalCells = w
                                 / (getCellWidthHeight() + BORDER_THICKNESS);
            
            this.verticalCells = h
                               / (getCellWidthHeight() + BORDER_THICKNESS);
        }
    }
    
    /**
     * The inner width and height of a cell.
     */
    private int cellWidthHeight;
    
    /**
     * The actual grid data structure that is a matrix of cells.
     */
    private Cell[][] cellGrid;
    
    private GridBounds getGridBounds(Rectangle2D screenBounds) {
        return new GridBounds(screenBounds);
    }
    
    public GridCanvas(int cellWidthHeight) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        this.cellWidthHeight = Math.max(MINIMUM_CELL_WIDTH_HEIGHT, 
                                        cellWidthHeight);
        
        setWidth(screenBounds.getWidth());
        setHeight(screenBounds.getHeight());
        GridBounds gridBounds = getGridBounds(screenBounds);
        cellGrid = new Cell[gridBounds.verticalCells][];
        
        for (int y = 0; y < gridBounds.verticalCells; ++y) {
            cellGrid[y] = new Cell[gridBounds.horizontalCells];
            
            for (int x = 0; x < gridBounds.horizontalCells; ++x) {
                cellGrid[y][x] = new Cell(CellType.FREE, 
                                          x,
                                          y);
            }
        }
        
        int shift = (int)(LEFT_SOURCE_SHIFT * gridBounds.horizontalCells);
        
        cellGrid[gridBounds.verticalCells / 2][shift] = 
                new Cell(CellType.SOURCE,
                         shift,
                         gridBounds.verticalCells / 2);
        
        cellGrid[gridBounds.verticalCells / 2][3 * shift] = 
                new Cell(CellType.TARGET,
                         3 * shift,
                         gridBounds.verticalCells / 2);
    }
    
    @Override
    public boolean isResizable() {
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
    
    public void draw() {
        GraphicsContext gc = this.getGraphicsContext2D();
        GridBounds gridBounds = getGridBounds(Screen.getPrimary().getBounds());
        
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
                Color color = cellGrid[y][x].getCellType().getColor();
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
        
//        System.out.println("width: " + getWidth());
//        System.out.println("height: " + getHeight());
//        System.out.println("contentWidth: " + contentWidth);
//        System.out.println("contentHeight: " + contentHeight);
//        System.out.println("leftMargin: " + leftMargin);
//        System.out.println("topMargin: " + topMargin);
//        System.out.println("verticalCells: " + verticalCells);
//        System.out.println("horizontalCells: " + horizontalCells);
        
//        gc.fillRect(leftMargin,
//                    topMargin,
//                    contentWidth,
//                    contentHeight);
    }
}   