package io.github.coderodde.pathfinding.model;

import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;

/**
 *
 * @author rodio
 */
public final class GridModel {
    
    private final Cell[][] cells;
    
    public GridModel(int width, int height) {
        cells = new Cell[height][width];
        
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                cells[y][x] = new Cell(CellType.FREE, 
                                       x,
                                       y);
            }
        }
        
        int sourceX = width / 4;
        int targetX = width - sourceX;
        int terminalY = height / 2;
        
        cells[terminalY][sourceX].setCellType(CellType.SOURCE);
        cells[terminalY][targetX].setCellType(CellType.TARGET);
    }
    
    public Cell getCell(int x, int y) {
        return cells[y][x];
    }
    
    public int getWidth() {
        return cells[0].length;
    }
    
    public int getHeight() {
        return cells.length;
    }
}
