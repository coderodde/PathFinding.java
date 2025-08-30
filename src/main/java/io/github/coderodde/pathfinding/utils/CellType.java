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
    PATH    (Color.DARKBLUE),
    SOURCE  (Color.web("#22dd22")),
    TARGET  (Color.web("#dd2222")),
    VISITED (Color.web("#b3b2b1")),
    OPENED  (Color.web("#aaffaa")),
    TRACED  (Color.web("#ffff80"));
    
    private final Color color;
    
    private CellType(Color color) {
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }
}
