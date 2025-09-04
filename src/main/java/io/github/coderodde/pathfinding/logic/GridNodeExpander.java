package io.github.coderodde.pathfinding.logic;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class GridNodeExpander {
    
    private final GridModel gridModel;
    private final PathfindingSettings pathfindingSettings;
    
    public GridNodeExpander(GridModel gridModel, 
                            PathfindingSettings pathfindingSettings) {
        this.gridModel = 
                Objects.requireNonNull(
                        gridModel, 
                        "The input grid model is null");
        
        this.pathfindingSettings =
                Objects.requireNonNull(
                        pathfindingSettings, 
                        "The input pathfinding settings is null");
    }
    
    public List<Cell> expand(Cell cell) {
        // 8: the maximum number of neighbours of a cell:
        List<Cell> neighbours = new ArrayList<>(8);
        int x = cell.getx();
        int y = cell.gety();
        
        // BEGIN: neighbors in west, north, east, sourth directions:
        if (isValidNeighbourCell(x - 1, y)) {
            neighbours.add(gridModel.getCell(x - 1, y));
        }
        
        if (isValidNeighbourCell(x, y - 1)) {
            neighbours.add(gridModel.getCell(x, y - 1));
        }
        
        if (isValidNeighbourCell(x + 1, y)) {
            neighbours.add(gridModel.getCell(x + 1, y));
        }
        
        if (isValidNeighbourCell(x, y + 1)) {
            neighbours.add(gridModel.getCell(x, y + 1));
        }
        
        // END: neighbors in west, north, east, south directions:
        
        if (!pathfindingSettings.allowDiagonals()) {
            // No diagonals allowed. We are done here:
            return neighbours;
        }
        
        // BEGIN: diagonal directions.
        
        if (pathfindingSettings.dontCrossCorners()) {
            
            if (canCrossNorthWest(x, y) && isValidNeighbourCell(x - 1, y - 1)) {
                neighbours.add(gridModel.getCell(x - 1,
                                                 y - 1));
            }
            
            if (canCrossNorthEast(x, y) && isValidNeighbourCell(x + 1, y - 1)) {
                neighbours.add(gridModel.getCell(x + 1,
                                                 y - 1));
            }
            
            if (canCrossSouthWest(x, y) && isValidNeighbourCell(x - 1, y + 1)) {
                neighbours.add(gridModel.getCell(x - 1,
                                                 y + 1));
            }
            
            if (canCrossSouthEast(x, y) && isValidNeighbourCell(x + 1, y + 1)) {
                neighbours.add(gridModel.getCell(x + 1,
                                                 y + 1));
            }
        } else {
            if (isValidNeighbourCell(x - 1, y - 1)) {
                neighbours.add(gridModel.getCell(x - 1, 
                                             y - 1));
            }
            
            if (isValidNeighbourCell(x + 1, y - 1)) {
                neighbours.add(gridModel.getCell(x + 1, 
                                             y - 1));
            }
            
            if (isValidNeighbourCell(x - 1, y + 1)) {
                neighbours.add(gridModel.getCell(x - 1,
                                             y + 1));
            }
            
            if (isValidNeighbourCell(x + 1, y + 1)) {
                neighbours.add(gridModel.getCell(x + 1, 
                                             y + 1));
            }
        }
        
        return neighbours;
    }
    
    private boolean isValidNeighbourCell(int x, int y) {
        if (x < 0 || y < 0 || x >= gridModel.getWidth() || y >= gridModel.getHeight()) {
            return false;
        }
        
        return !gridModel.getCell(x, y).getCellType().equals(CellType.WALL);
    }
    
    public boolean canCrossNorthWest(int x, int y) {
        if (x < 1 || y < 1) {
            return false;
        }
        
        Cell cell = gridModel.getCell(x - 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = gridModel.getCell(x, y - 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canCrossNorthEast(int x, int y) {
        if (x >= gridModel.getWidth() - 1 || y < 1) {
            return false;
        }
        
        Cell cell = gridModel.getCell(x + 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = gridModel.getCell(x, y - 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canCrossSouthWest(int x, int y) {
        if (x < 1 || y >= gridModel.getHeight() - 1) {
            return false;
        }
        
        Cell cell = gridModel.getCell(x - 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = gridModel.getCell(x, y + 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canCrossSouthEast(int x, int y) {
        if (x >= gridModel.getWidth() - 1 || y >= gridModel.getHeight() - 1) {
            return false;
        }
        
        Cell cell = gridModel.getCell(x + 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = gridModel.getCell(x, y + 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
}
