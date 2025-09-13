package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public final class BFSFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model,
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState,
                               SearchStatistics searchStatistics) {
        
        
        Map<Cell, Cell> parents = new HashMap<>();
        Deque<Cell> queue = new ArrayDeque<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        parents.put(source, null);
        queue.addLast(source);
        searchStatistics.incrementOpened();
        
        while (!queue.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell current = queue.removeFirst();
            
            searchStatistics.incrementVisited();
            searchStatistics.decrementOpened();
            
            if (!current.equals(source)) {
                model.setCellType(current, CellType.VISITED);
            }
            
            if (current.equals(target)) {
                return tracebackPath(target, parents);
            }
            
            neighbourIterable.setStartingCell(current);
            
            for (Cell neighbour : neighbourIterable) {
                if (searchState.haltRequested()) {
                    return List.of();
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                    
                    if (searchState.haltRequested()) {
                        // Requested halt while on pause:
                        return List.of();
                    }
                }
                
                searchSleep(pathfindingSettings);
                
                if (parents.containsKey(neighbour)) {
                    continue;
                }
                
                if (!neighbour.equals(target)) {
                    model.setCellType(neighbour, CellType.OPENED);
                }
                    
                parents.put(neighbour, current);
                queue.addLast(neighbour);
                searchStatistics.incrementOpened();
            }
        }
        
        return List.of();
    }
}
