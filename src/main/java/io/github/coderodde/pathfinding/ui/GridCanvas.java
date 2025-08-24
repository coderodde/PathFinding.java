package io.github.coderodde.pathfinding.ui;

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
    
    private static final int MINIMUM_CELL_WIDTH_HEIGHT = 5;
    private static final int BORDER_THICKNESS = 1;
    private static final Paint BORDER_PAINT = Color.web("#222222");
    
    /**
     * The inner width and height of a cell.
     */
    private int cellWidthHeight;
    
    public GridCanvas() {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        setWidth(screenBounds.getWidth());
        setHeight(screenBounds.getHeight());
        
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
        
        int w = (int) getWidth();
        int h = (int) getHeight();
        
        w -= BORDER_THICKNESS; // Don't count the border on the right.
        h -= BORDER_THICKNESS; // Don't count the border at the top.
               
        int horizontalCells = w / (getCellWidthHeight() + BORDER_THICKNESS); 
        int verticalCells   = h / (getCellWidthHeight() + BORDER_THICKNESS); 
        
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