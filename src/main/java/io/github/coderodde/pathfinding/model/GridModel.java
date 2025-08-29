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
    
    private final int width;
    private final int height;
    
    /**
     * The previous {@code X}-coordinate of the source cell.
     */
    private int previousSourceCellX;
    
    /**
     * The previous {@code Y}-coordinate of the source cell.
     */
    private int previousSourceCellY;
    
    /**
     * The previous {@code X}-coordinate of the target cell.
     */
    private int previousTargetCellX;
    
    /**
     * The previous {@code Y}-coordinate of the target cell.
     */
    private int previousTargetCellY;
    
    /**
     * Caches the cell representing the source cell.
     */
    private Cell sourceCell;
    
    /**
     * Caches the cell representing the target cell.
     */
    private Cell targetCell;
    
    /**
     * If set to {@code true}, the source cell covers a wall cell drawn before.
     */
    private boolean sourceCellCoversWallCell = false;
    
    /**
     * If set to {@code true}, the target cell covers a wall cell drawn before.
     */
    private boolean targetCellCoversWallCell = false;
    
    public void clearWalls() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (getCellType(x, y).equals(CellType.WALL)) {
                    setCellType(x, y, CellType.FREE);
                }
            }
        }
    }
        
    public void createCells() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                cells[y][x] = new Cell(CellType.FREE, x, y);
            }
        }
    }
    
    public void reinit() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                setCellType(x, y, CellType.FREE);
            }
        }
        
        int sourceX = width / 4;
        int targetX = width - sourceX;
        int terminalY = height / 2; // The y-coodinate for both the source and 
                                    // target.
        
        sourceCell = getCell(sourceX, terminalY);
        targetCell = getCell(targetX, terminalY);
        
        sourceCell.setx(sourceX);
        sourceCell.sety(terminalY);
        
        targetCell.setx(targetX);
        targetCell.sety(terminalY);
                                    
        setCellType(sourceCell, CellType.SOURCE);
        setCellType(targetCell, CellType.TARGET);
        
        previousSourceCellX = sourceX;
        previousSourceCellY = terminalY;
        
        previousTargetCellX = targetX;
        previousTargetCellY = terminalY;
    }
    
    /**
     * Constructs this grid model.
     * 
     * @param width  the number of cells in horizontal direction.
     * @param height the number of cells in vertical direction.
     */
    public GridModel(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new Cell[height][width];
        createCells();
        reinit();
    }
    
    public void setSourceCellCoversWallCell(boolean sourceCellCoversWallCell) {
        this.sourceCellCoversWallCell = sourceCellCoversWallCell;
    }
    
    public void setTargetCellCoversWallCell(boolean targetCellCoversWallCell) {
        this.targetCellCoversWallCell = targetCellCoversWallCell;
    }
    
    /**
     * Moves the source cell to the cell with cell coordinates {@code (x, y)}.
     * 
     * @param x the {@code X}-coordinates of the new source cell position.
     * @param y the {@code Y}-coordinates of the new source cell position.
     */
    public void moveSource(int x, int y) {
        if (x == targetCell.getx() && y == targetCell.gety()) {
            // Do not move the source on top of the target cell!
            return;
        }
        
        if (x == previousSourceCellX && y == previousSourceCellY) {
            // Source cell position did not change. Nothing to update!
            return;
        }
        
        if (sourceCellCoversWallCell) {
            setCellType(previousSourceCellX,
                        previousSourceCellY,
                        CellType.WALL);
        } else {
            setCellType(previousSourceCellX, 
                        previousSourceCellY,
                        CellType.FREE);
        }
        
        previousSourceCellX = x;
        previousSourceCellY = y;
        
        sourceCellCoversWallCell = 
                getCell(x, y)
                        .getCellType()
                        .equals(CellType.WALL);
        
        sourceCell.setx(x);
        sourceCell.sety(y);
        
        setCellType(x, 
                    y,
                    CellType.SOURCE);
    }
    
    public void moveTarget(int x, int y) {
        if (x == sourceCell.getx() && y == sourceCell.gety()) {
            // Do not move the target on top of the source cell!
            return;
        }
        
        if (x == previousTargetCellX && y == previousTargetCellY) {
            // Target cell position did not change. Nothing to update!
            return;
        }
        
        if (targetCellCoversWallCell) {
            setCellType(previousTargetCellX,
                        previousTargetCellY,
                        CellType.WALL);
        } else {
            setCellType(previousTargetCellX, 
                        previousTargetCellY,
                        CellType.FREE);
        }
        
        previousTargetCellX = x;
        previousTargetCellY = y;
        
        targetCellCoversWallCell = 
                getCell(x, y)
                        .getCellType()
                        .equals(CellType.WALL);
        
        targetCell.setx(x);
        targetCell.sety(y);
        
        setCellType(x, 
                    y,
                    CellType.TARGET);
    }
    
    public boolean isValidCellLocation(int x, int y) {
        if (x < 0) {
            return false;
        }
        
        if (y < 0) {
            return false;
        }
        
        if (x >= cells[0].length) {
            return false;
        }
        
        if (y >= cells.length) {
            return false;
        }
        
        return true;
    }
    
    public Cell getSourceGridCell() {
        return sourceCell;
    }
    
    public Cell getTargetGridCell() {
        return targetCell;
    }
    
    public Cell getCell(int x, int y) {
        if (!isValidCellLocation(x, y)) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "Invalid cell location: (x = %d, y = %d). " + 
                            "The width of the model is %d and " +
                            "the height is %d cells",
                            x,
                            y,
                            getWidth(), 
                            getHeight()));
        }
        
        return cells[y][x];
    }
    
    public CellType getCellType(int x, int y) {
        if (!isValidCellLocation(x, y)) {
            throw new IllegalArgumentException(
                    String.format(
                            "(%d, %d), not a valid cell coordinates",
                            x, 
                            y));
        }
        
        return cells[y][x].getCellType();
    }
    
    public CellType getCellType(Cell cell) {
        return getCellType(cell.getx(), cell.gety());
    }
    
    public void setCellType(int x, int y, CellType cellType) {
        Cell cell = getCell(x, y);
        cell.setCellType(cellType);
        
        if (view != null) {
            view.drawCell(cell);
        }
    }
    
    public void setCellType(Cell cell, CellType cellType) {
        setCellType(cell.getx(),
                    cell.gety(), 
                    cellType);
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
