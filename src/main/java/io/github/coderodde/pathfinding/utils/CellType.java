package io.github.coderodde.pathfinding.utils;

import javafx.scene.paint.Color;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public enum CellType {
    
    FREE    (Color.WHITE),
    WALL    (Color.web("#444444")),
    SOURCE  (Color.web("#22dd22")),
    TARGET  (Color.web("#dd2222")),
    VISITED (Color.web("#448844")),
    OPENED  (Color.web("#666666")),
    TRACED  (Color.web("#888888"));
    
    private final Color color;
    
    private CellType(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
}
