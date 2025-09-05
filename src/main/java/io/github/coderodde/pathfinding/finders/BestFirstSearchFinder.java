package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 4, 2025)
 * @since 1.0.0 (Sep 4, 2025)
 */
public final class BestFirstSearchFinder implements Finder {
    
    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState) {
        
        Queue<HeapNode> open    = new PriorityQueue<>();
        Map<Cell, Cell> parents = new HashMap<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        open.add(new HeapNode(source, 0.0));
        parents.put(source, null);
        
        while (!open.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell current = open.remove().cell;
            
            if (!current.equals(source) &&
                !current.equals(target)) {
                model.setCellType(current, CellType.VISITED);
            }
            
            if (current.equals(target)) {
                return tracebackPath(target, parents);
            }
            
            neighbourIterable.setStartingCell(current);
            
            for (Cell child : neighbourIterable) {
                if (searchState.haltRequested()) {
                    return List.of();
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                }
                
                searchSleep(pathfindingSettings);
                
                if (!parents.containsKey(child)) {
                    parents.put(child, current);
                    
                    if (child.equals(target)) {
                        return tracebackPath(child, parents);
                    }
                    
                    model.setCellType(child, CellType.OPENED);
                    parents.put(child, current);
                    open.add(
                            new HeapNode(
                                    child,
                                    pathfindingSettings.getHeuristicFunction()
                                                       .estimate(child, 
                                                                 target)));
                }
            }
        }
        
        return List.of();
    }
    
    private static final class HeapNode implements Comparable<HeapNode> {

        Cell cell;
        double f;
        
        public HeapNode(Cell cell, double f) {
            this.cell = cell;
            this.f = f;
        }
        
        @Override
        public int compareTo(HeapNode o) {
            return Double.compare(f, o.f);
        }
    }
}
