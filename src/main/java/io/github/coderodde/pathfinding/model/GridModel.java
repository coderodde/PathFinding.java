package io.github.coderodde.pathfinding.model;

import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import static io.github.coderodde.pathfinding.utils.CellType.FREE;
import static io.github.coderodde.pathfinding.utils.CellType.OPENED;
import static io.github.coderodde.pathfinding.utils.CellType.SOURCE;
import static io.github.coderodde.pathfinding.utils.CellType.TARGET;
import static io.github.coderodde.pathfinding.utils.CellType.TRACED;
import static io.github.coderodde.pathfinding.utils.CellType.VISITED;
import static io.github.coderodde.pathfinding.utils.CellType.WALL;
import io.github.coderodde.pathfinding.view.GridView;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

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
    
    public boolean isWalkable(int x, int y) {
        if (!isValidCellLocation(x, y)) {
            return false;
        }
        
        Cell cell = getCell(x, y);
        
        return !cell.getCellType().equals(CellType.WALL);
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
    
    public GridModel copyModel() {
        return null;
    }
    
    public void drawRandomMaze() {
        Random rnd = new Random();
        drawAllAsWalls();
        drawViaDFS(rnd);
        setSourceTargetCells(rnd);
    }
    
    private void drawAllAsWalls() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {   
                setCellType(x, y, WALL);
            }
        }
    }
    
    private void setSourceTargetCells(Random random) {
        int sourceX = random.nextInt(width);
        int sourceY = random.nextInt(height);
        
        moveSource(sourceX, sourceY);
        
        while (true) {
            int targetX = random.nextInt(width);
            int targetY = random.nextInt(height);
            
            if (targetX != sourceX || targetY != sourceY) {
                moveTarget(targetX, targetY);
                return;
            }
        }
    }
    
    private void drawViaDFS(Random rnd) {
        int rows = height;
        int cols = width;
        
        if (height % 2 == 0) {
            rows--;
        }
        
        if (width % 2 == 0) {
            cols--;
        }
        
        int roomCols = (cols - 1) / 2; // Number of “rooms” horizontally.
        int roomRows = (rows - 1) / 2; // Number of “rooms” vertically.

        boolean[][] visited = new boolean[roomRows]
                                         [roomCols];

        // Pick a random room (mapped to grid coords: rr -> 2 * rr + 1):
        int rr = rnd.nextInt(roomRows); 
        int cc = rnd.nextInt(roomCols);
        
        // Carve the inital room from its wall:
        carveCell(cc, rr);

        // Initialize DFS stack:
        Deque<int[]> stack = new ArrayDeque<>();
        visited[rr][cc] = true;
        stack.push(new int[]{ cc, rr });

        // 4-neighbor moves in room space
        int[] deltaRowOffsets = { -1, 0, 1, 0 };
        int[] deltaColOffsets = { 0, 1, 0, -1 };
        int[] directionIndices = { 0, 1, 2, 3 };

        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            int c0 = cur[0];
            int r0 = cur[1];
            
            // Fisher–Yates shuffle:
            for (int i = 3; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int t = directionIndices[i];
                directionIndices[i] = directionIndices[j];
                directionIndices[j] = t;
            }

            boolean moved = false;
            
            for (int directionIndex : directionIndices) {
                int r1 = r0 + deltaRowOffsets[directionIndex]; 
                int c1 = c0 + deltaColOffsets[directionIndex];
                
                if (0 <= r1 
                        && r1 < roomRows 
                        && 0 <= c1 
                        && c1 < roomCols 
                        && !visited[r1][c1]) {
                    
                    // Carve passage between rooms (r0, c0) and (r1, c1):
                    carveBetween(c0, 
                                 r0,
                                 c1,
                                 r1);
                    
                    carveCell(c1, r1);
                    
                    visited[r1][c1] = true;
                    stack.push(new int[]{ c1, r1 });
                    moved = true;
                    break;
                }
            }
            
            if (!moved) {
                stack.pop();
            }
        }
    }
    
    private void carveCell(int roomX, int roomY) {
        int x = 2 * roomX + 1;
        int y = 2 * roomY + 1;
        
        setCellType(x, y, CellType.FREE);
    }
    
    private void carveBetween(int roomX0,
                              int roomY0,
                              int roomX1,
                              int roomY1) {
        
        int cellX0 = 2 * roomX0 + 1;
        int cellY0 = 2 * roomY0 + 1;
        int cellX1 = 2 * roomX1 + 1;
        int cellY1 = 2 * roomY1 + 1;
        
        int x = (cellX0 + cellX1) / 2;
        int y = (cellY0 + cellY1) / 2;
        
        setCellType(x, y, CellType.FREE);
    }
}
