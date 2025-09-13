package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

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
                               SearchState searchState,
                               SearchStatistics searchStatistics) {
        
        Queue<HeapNode> open    = new PriorityQueue<>();
        Set<Cell> closed        = new HashSet<>();
        Map<Cell, Cell> parents = new HashMap<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        open.add(new HeapNode(source, 0.0));
        parents.put(source, null);
        searchStatistics.incrementOpened();
        
        while (!open.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell current = open.remove().cell;
            
            if (current.equals(target)) {
                return tracebackPath(target, parents);
            }
            
            if (!current.equals(source) &&
                !current.equals(target)) {
                model.setCellType(current, CellType.VISITED);
            }
           
            closed.add(current);
            neighbourIterable.setStartingCell(current);
            searchStatistics.decrementOpened();
            searchStatistics.incrementVisited();
            
            for (Cell child : neighbourIterable) {
                if (searchState.haltRequested()) {
                    return List.of();
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                }
                
                if (closed.contains(child)) {
                    continue;
                }
                
                if (!parents.containsKey(child)) {
                    searchSleep(pathfindingSettings);
                    
                    model.setCellType(child, CellType.OPENED);
                    parents.put(child, current);
                    open.add(
                            new HeapNode(
                                    child,
                                    pathfindingSettings.getHeuristicFunction()
                                                       .estimate(child, 
                                                                 target)));
                    
                    searchStatistics.incrementOpened();
                }
            }
        }
        
        return List.of();
    }
}
