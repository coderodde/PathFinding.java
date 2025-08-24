package io.github.coderodde.pathfinding.utils;

import static io.github.coderodde.pathfinding.Configuration.BORDER_THICKNESS;
import javafx.geometry.Rectangle2D;

/**
 * This class holds the grid bounds.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class GridBounds {

    public final int horizontalCells;
    public final int verticalCells;

    /**
     * Constructs this {@code GridBounds} object.
     * 
     * @param rect            the rectangle representing the view port.
     * @param cellWidthHeight the width and height of the cells.
     */
    public GridBounds(Rectangle2D rect,
                      int cellWidthHeight) {
        int w = (int) rect.getWidth();
        int h = (int) rect.getHeight();

        w -= BORDER_THICKNESS; // Don't count the border on the right.
        h -= BORDER_THICKNESS; // Don't count the border at the top.

        this.horizontalCells = w / (cellWidthHeight + BORDER_THICKNESS);
        this.verticalCells   = h / (cellWidthHeight + BORDER_THICKNESS);
    }
    
    @Override
    public String toString() {
        return "GridBounds[horizontalCells = "
                + horizontalCells 
                + ", verticalCells = " 
                + verticalCells 
                + "]";
    }
}