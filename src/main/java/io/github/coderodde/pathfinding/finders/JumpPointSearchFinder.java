package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.finders.jps.DiagonalCrossingNeighbourFinder;
import io.github.coderodde.pathfinding.finders.jps.DiagonalNoCrossingNeighbourFinder;
import io.github.coderodde.pathfinding.finders.jps.NoDiagonalNeighbourFinder;
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
    
    /**
     * This interface defines the API for jumping algorithms.
     */
    public interface Jumper {
        
        /**
         * Jumps to the next jump point cell.
         * 
         * @param x          the {@code x} coordinate of the current cell.
         * @param y          the {@code y} coordinate of the current cell.
         * @param px         the {@code x} coordinate of the parent cell.
         * @param py         the {@code y} coordinate of the parent cell.
         * @param model      the grid model.
         * 
         * @return the next jump point cell or {@code null} if there is no such.
         */
        Cell jump(int x,
                  int y,
                  int px,
                  int py,
                  GridModel model);
    }
    
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
}
