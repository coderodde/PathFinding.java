package io.github.coderodde.pathfinding;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * This class merely contains static constant used throughout the software 
 * package.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class Configuration {
    
    /**
     * The minimum cell width and height. Cells are squares, so the width and 
     * the height are equal.
     */
    public static final int MINIMUM_CELL_WIDTH_HEIGHT = 20;
    
    /**
     * The border thickness in pixels.
     */
    public static final int BORDER_THICKNESS = 1;
    
    /**
     * The border color.
     */
    public static final Paint BORDER_PAINT = Color.web("#555555");
    
    /**
     * Multiplied by the number of cells in horizontal direction, gives the 
     * {@code X]-coordinate of the source cell.
     */
    public static final float LEFT_SOURCE_SHIFT = 0.25f;
    
    /**
     * The default width and height of a cell.
     */
    public static final int DEFAULT_CELL_WIDTH_HEIGHT = 26;
    
    private Configuration() {
        
    }
}
