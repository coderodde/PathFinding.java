package io.github.coderodde.pathfinding.utils;

import java.util.Objects;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class Cell {
    
    private final CellType cellType;
    
    private final int x;
    private final int y;
    
    public Cell(CellType cellType, int x, int y) {
        this.cellType = 
                Objects.requireNonNull(cellType, "The cell type is null");
        
        this.x = x;
        this.y = y;
    }
    
    public CellType getCellType() {
        return cellType;
    }
    
    public int getx() {
        return x;
    }
    
    public int gety() {
        return y;
    }
    
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        
        if (o == null) {
            return false;
        }
        
        if (!getClass().equals(o.getClass())) {
            return false;
        }
        
        Cell other = (Cell) o;
        return x == other.x && y == other.y;
    }
}
