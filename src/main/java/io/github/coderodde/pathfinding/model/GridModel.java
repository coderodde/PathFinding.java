package io.github.coderodde.pathfinding.model;

import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import static io.github.coderodde.pathfinding.utils.CellType.FREE;
import static io.github.coderodde.pathfinding.utils.CellType.OPENED;
import static io.github.coderodde.pathfinding.utils.CellType.SOURCE;
import static io.github.coderodde.pathfinding.utils.CellType.TARGET;
import static io.github.coderodde.pathfinding.utils.CellType.TRACED;
import static io.github.coderodde.pathfinding.utils.CellType.VISITED;
import io.github.coderodde.pathfinding.view.GridView;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * Caches the current path.
     */
    private final List<Cell> path = new ArrayList<>();
    
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
    
    public void initTerminalCells() {
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
    
    public void initModel() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                setCellType(x, y, CellType.FREE);
            }
        }
        
        initTerminalCells();
    }
    
    /**
     * Resets all the cells of type {@link CellType#OPENED}, 
     * {@link CellType#VISITED} and {@link CellType#TRACED} to 
     * {@link CellType#FREE}.
     */
    public void clearStateCells() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                switch (getCell(x, y).getCellType()) {
                    case VISITED:
                    case OPENED:
                    case TRACED:
                        setCellType(x, y, FREE);
                        break;
                    case SOURCE:
                        setCellType(x, y, SOURCE); // Repaint source so that it has
                        // the path visual artifact!
                        break;
                    case TARGET:
                        setCellType(x, y, TARGET); // Repaint target!
                        break;
                }
            }
        }
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
        initModel();
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
        
        boolean oldSourceCellCoversWallCell = sourceCellCoversWallCell;
        
        sourceCellCoversWallCell = 
                getCell(x, y)
                        .getCellType()
                        .equals(CellType.WALL);
        
        setCellType(previousSourceCellX,
                    previousSourceCellY,
                    oldSourceCellCoversWallCell ? 
                    CellType.WALL :
                    CellType.FREE);
        
        previousSourceCellX = x;
        previousSourceCellY = y;
        
        setCellType(x, 
                    y,
                    CellType.SOURCE);
        
        sourceCell = getCell(x, y);
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
        
        boolean oldTargetCellCoversWallCell = targetCellCoversWallCell;
        
        targetCellCoversWallCell = 
                getCell(x, y)
                        .getCellType()
                        .equals(CellType.WALL);
        
        setCellType(previousTargetCellX,
                    previousTargetCellY, 
                    oldTargetCellCoversWallCell ? 
                    CellType.WALL :
                    CellType.FREE);
        
        previousTargetCellX = x;
        previousTargetCellY = y;
        
        setCellType(x, 
                    y,
                    CellType.TARGET);
        
        targetCell = getCell(x, y);
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
    
    public void setPath(List<Cell> path) {
        path.clear();
        path.addAll(path);
    }
    
    public List<Cell> getPath() {
        return new ArrayList<>(path);
    }
}
