package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * This class implements the Jump Point Search.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Oct 19, 2025)
 * @since 1.0.0 (Oct 19, 2025)
 */
public final class JumpPointSearchFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        NeighbourFinder neighbourFinder = 
                getNeighbourFinder(pathfindingSettings);
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        HeapNode startHeapNode = new HeapNode(source, 0.0);
        
        Queue<HeapNode> open = new PriorityQueue<>();
        Map<Cell, Cell> parentsMap = new HashMap<>();
        
        while (!open.isEmpty()) {
            Cell current = open.remove().cell;
            
            if (model.getCellType(current) != CellType.SOURCE &&
                model.getCellType(current) != CellType.TARGET) {
                model.setCellType(current, CellType.VISITED);
            }
            
            if (current.equals(target)) {
                return tracebackPath(target, 
                                     parentsMap);
            }
            
            identifySuccessors(current,
                               parentsMap,
                               model,
                               pathfindingSettings,
                               neighbourFinder);
        }
        
        return List.of();
    }
    
    private static NeighbourFinder 
        getNeighbourFinder(PathfindingSettings pathfindingSettings) {
        
        if (pathfindingSettings.allowDiagonals()) {
            if (pathfindingSettings.dontCrossCorners()) {
                return new DiagonalNoCrossingNeighbourFinder();
            } else {
                return new DiagonalCrossingNeighbourFinder();
            }
        } else {
            return new NoDiagonalNeighbourFinder();
        }
    }
           
    private static void identifySuccessors(Cell current,
                                           Map<Cell, Cell> parentsMap,
                                           GridModel model,
                                           PathfindingSettings ps,
                                           NeighbourFinder neighbourFinder) {
        
        List<Cell> neighbors = 
                neighbourFinder.findNeighbours(current,
                                               parentsMap,
                                               model,
                                               ps);
    }
    
    private static final class DiagonalNoCrossingNeighbourFinder 
            implements NeighbourFinder {

        @Override
        public List<Cell> findNeighbours(Cell current,
                                         Map<Cell, Cell> parentsMap,
                                         GridModel model, 
                                         PathfindingSettings ps) {
        
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
    
    private static final class DiagonalCrossingNeighbourFinder 
            implements NeighbourFinder {

        @Override
        public List<Cell> findNeighbours(Cell current,
                                         Map<Cell, Cell> parentsMap,
                                         GridModel model, 
                                         PathfindingSettings ps) {
            List<Cell> neighbours = new ArrayList<>();
            Cell parent = parentsMap.get(current);
            
            int x = current.getx();
            int y = current.gety();
            int px;
            int py;
            int dx;
            int dy;

            if (parent != null) {
                px = parent.getx();
                py = parent.gety();

                dx = (x - px) / Math.max(Math.abs(x - px), 1);
                dy = (y - py) / Math.max(Math.abs(y - py), 1);

                // Diagonal search:
                if (dx != 0 && dy != 0) {
                    if (model.isWalkable(x, y + dy)) {
                        neighbours.add(model.getCell(x, y + dy));
                    }

                    if (model.isWalkable(x + dx, y)) {
                        neighbours.add(model.getCell(x + dx, y));
                    }

                    if (model.isWalkable(x + dx, y + dy)) {
                        neighbours.add(model.getCell(x + dx, y + dy));
                    }

                    if (model.isWalkable(x - dx, y)) {
                        neighbours.add(model.getCell(x - dx, y + dy));
                    }

                    if (model.isWalkable(x, y - dy)) {
                        neighbours.add(model.getCell(x + dx, y - dy));
                    }
                } else {
                    // Once here, search horizontally and vertically:
                    if (dx == 0) {
                        if (model.isWalkable(x, y + dy)) {
                            neighbours.add(model.getCell(x, y + dy));
                        }

                        if (model.isWalkable(x + 1, y)) {
                            neighbours.add(model.getCell(x + 1, y + dy));
                        }

                        if (model.isWalkable(x - 1, y)) {
                            neighbours.add(model.getCell(x - 1, y + dy));
                        }
                    } else {
                        if (model.isWalkable(x + dx, y)) {
                            neighbours.add(model.getCell(x + dx, y));
                        }

                        if (model.isWalkable(x, y + 1)) {
                            neighbours.add(model.getCell(x + dx, y + 1));
                        }

                        if (model.isWalkable(x, y - 1)) {
                            neighbours.add(model.getCell(x + dx, y - 1));
                        }
                    }
                }
            } else {
                // Once here, return all neighbours:
                neighbours.addAll(
                        new GridNodeExpander(model, 
                                             ps).expand(current));
            }

            return neighbours;
        }
    }
    
    private static final class NoDiagonalNeighbourFinder 
            implements NeighbourFinder {

        @Override
        public List<Cell> findNeighbours(Cell current,
                                         Map<Cell, Cell> parentsMap,
                                         GridModel model,
                                         PathfindingSettings ps) {
            
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
    
    /**
     * This interface defines the API for computing neighbour cells of a given 
     * cell.
     */
    public interface NeighbourFinder {
        
        /**
         * Finds the neighbour cells of the cell {@code current}.
         * 
         * @param current    the cell whose neighbours to find.
         * @param parentsMap the map which maps cells to their respective parent
         *                   cells.
         * @param model      the grid model.
         * @param ps         the pathfinding settings.
         * @return 
         */
        List<Cell> findNeighbours(Cell current,
                                  Map<Cell, Cell> parentsMap,
                                  GridModel model,
                                  PathfindingSettings ps);
    }
    
    private static Cell jump(Cell current,
                             Cell target,
                             int px,
                             int py,
                             GridModel model) {
        int x = current.getx();
        int y = current.gety();
        int dx = x - px;
        int dy = y - py;
        
        if (!model.isWalkable(x, y)) {
            return null;
        }
        
        if (model.getCellType(current) != CellType.SOURCE &&
            model.getCellType(current) != CellType.TARGET) {
            model.setCellType(current, CellType.TRACED);
        }
        
        if (current.equals(target)) {
            return target;
        }
        
        if (dx != 0 && dy != 0) {
            
        }
    }
}
