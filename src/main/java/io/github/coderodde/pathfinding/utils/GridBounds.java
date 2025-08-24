package io.github.coderodde.pathfinding.utils;

import static io.github.coderodde.pathfinding.Configuration.BORDER_THICKNESS;
import javafx.geometry.Rectangle2D;

public final class GridBounds {

    public final int horizontalCells;
    public final int verticalCells;

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