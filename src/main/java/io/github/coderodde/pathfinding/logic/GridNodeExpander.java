package io.github.coderodde.pathfinding.logic;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class GridNodeExpander {
    
    private GridModel model;
    private PathfindingSettings pathfindingSettings;
    
    public void setGridModel(GridModel model) {
        this.model = model;
    }
    
    public void setPathfindingSettings(
            PathfindingSettings pathfindingSettings) {
        this.pathfindingSettings = pathfindingSettings;
    }
    
    public List<Cell> expand(Cell cell) {
        // 8: the maximum number of neighbours of a cell:
        List<Cell> neighbours = new ArrayList<>(8);
        int x = cell.getx();
        int y = cell.gety();
        
        // BEGIN: neighbors in west, north, east, sourth directions:
        if (isValidNeighbourCell(x - 1, y)) {
            neighbours.add(model.getCell(x - 1, y));
        }
        
        if (isValidNeighbourCell(x, y - 1)) {
            neighbours.add(model.getCell(x, y - 1));
        }
        
        if (isValidNeighbourCell(x + 1, y)) {
            neighbours.add(model.getCell(x + 1, y));
        }
        
        if (isValidNeighbourCell(x, y + 1)) {
            neighbours.add(model.getCell(x, y + 1));
        }
        
        // END: neighbors in west, north, east, south directions:
        
        if (!pathfindingSettings.allowDiagonals()) {
            // No diagonals allowed. We are done here:
            return neighbours;
        }
        
        // BEGIN: diagonal directions.
        
        if (pathfindingSettings.dontCrossCorners()) {
            
            if (canCrossNorthWest(x, y)) {
                neighbours.add(model.getCell(x - 1, y - 1));
            }
            
            if (canCrossNorthEast(x, y)) {
                neighbours.add(model.getCell(x + 1, y - 1));
            }
            
            if (canCrossSouthWest(x, y)) {
                neighbours.add(model.getCell(x - 1, y + 1));
            }
            
            if (canCrossSouthEast(x, y)) {
                neighbours.add(model.getCell(x + 1, y + 1));
            }
        } else {
            if (isValidNeighbourCell(x - 1, y - 1)) {
                neighbours.add(model.getCell(x - 1, y - 1));
            }
            
            if (isValidNeighbourCell(x + 1, y - 1)) {
                neighbours.add(model.getCell(x + 1, y - 1));
            }
            
            if (isValidNeighbourCell(x - 1, y + 1)) {
                neighbours.add(model.getCell(x - 1, y + 1));
            }
            
            if (isValidNeighbourCell(x + 1, y + 1)) {
                neighbours.add(model.getCell(x + 1, y + 1));
            }
        }
        
        return neighbours;
    }
    
    private boolean isValidNeighbourCell(int x, int y) {
        if (x < 0 || y < 0 || x >= model.getWidth() || y >= model.getHeight()) {
            return false;
        }
        
        return !model.getCell(x, y).getCellType().equals(CellType.WALL);
    }
    
    public boolean canCrossNorthWest(int x, int y) {
        if (x < 1 || y < 1) {
            return false;
        }
        
        Cell cell = model.getCell(x - 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = model.getCell(x, y - 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canCrossNorthEast(int x, int y) {
        if (x >= model.getWidth() - 1 || y < 1) {
            return false;
        }
        
        Cell cell = model.getCell(x + 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = model.getCell(x, y + 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canCrossSouthWest(int x, int y) {
        if (x < 1 || y >= model.getHeight() - 1) {
            return false;
        }
        
        Cell cell = model.getCell(x - 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = model.getCell(x, y + 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
    
    public boolean canCrossSouthEast(int x, int y) {
        if (x >= model.getWidth() - 1 || y >= model.getHeight() - 1) {
            return false;
        }
        
        Cell cell = model.getCell(x + 1, y);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        cell = model.getCell(x, y + 1);
        
        if (cell.getCellType().equals(CellType.WALL)) {
            return false;
        }
        
        return true;
    }
}
