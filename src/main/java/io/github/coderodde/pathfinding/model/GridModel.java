package io.github.coderodde.pathfinding.model;

import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import io.github.coderodde.pathfinding.view.GridView;

/**
 * This class implements the grid model representing the cell configurations.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class GridModel {
    
    /**
     * The actual grid.
     */
    private final Cell[][] cells;
    
    /**
     * The view object.
     */
    private GridView view;
    
    private Cell sourceCell;
    
    private Cell targetCell;
    
    private boolean sourceCellCoversWallCell = false;
    private boolean targetCellCoversWallCell = false;
    
    /**
     * Constructs this grid model.
     * 
     * @param width  the number of cells in horizontal direction.
     * @param height the number of cells in vertical direction.
     */
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
        
        sourceCell = new Cell(CellType.SOURCE, 
                              sourceX, 
                              terminalY);
        
        targetCell = new Cell(CellType.TARGET,
                              targetX,
                              terminalY);
        
        cells[terminalY][sourceX] = sourceCell;
        cells[terminalY][targetX] = targetCell;
    }
    
    public void moveSource(int x, int y) {
        int oldSourceX = sourceCell.getx();
        int oldSourceY = sourceCell.gety();
        
        if (sourceCellCoversWallCell) {
            setCellType(oldSourceX,
                        oldSourceY,
                        CellType.WALL);
        } else {
            setCellType(oldSourceX, 
                        oldSourceY,
                        CellType.FREE);
        }
        
        sourceCellCoversWallCell = 
                getCell(x, y)
                        .getCellType()
                        .equals(CellType.WALL);
        
        setCellType(x, 
                    y,
                    CellType.SOURCE);
    }
    
    public void moveTarget(int x, int y) {
        int oldTargetX = targetCell.getx();
        int oldTargetY = targetCell.gety();
        
        if (sourceCellCoversWallCell) {
            setCellType(oldTargetX,
                        oldTargetY,
                        CellType.WALL);
        } else {
            setCellType(oldTargetX, 
                        oldTargetY,
                        CellType.FREE);
        }
        
        targetCellCoversWallCell = 
                getCell(x, y)
                        .getCellType()
                        .equals(CellType.WALL);
        
        setCellType(x, 
                    y,
                    CellType.TARGET);
        
        view.drawDebug(targetCell.toString());
    }
    
    public Cell getCell(int x, int y) {
        return cells[y][x];
    }
    
    public void setCellType(int x, int y, CellType cellType) {
        Cell cell = getCell(x, y);
        cell.setCellType(cellType);
        
        if (view != null) {
            view.drawCell(cell);
        }
    }
    
    public int getWidth() {
        return cells[0].length;
    }
    
    public int getHeight() {
        return cells.length;
    }
    
    public void setGridView(GridView view) {
        this.view = view;
    }
}
