package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 5, 2025)
 * @since 1.0.0 (Sep 5, 2025)
 */
public final class BeamSearchFinder implements Finder {

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
            
            if (queue.size() > pathfindingSettings.getBeamWidth()) {
                List<Cell> layer = new ArrayList<>(queue);
                
                HeuristicFunction H = 
                        pathfindingSettings.getHeuristicFunction();
                
                layer.sort((a, b) -> {
                    return Double.compare(H.estimate(a, target),
                                          H.estimate(b, target));
                });
                
                searchStatistics.addToOpened(-layer.size());
                
                queue.clear();
                queue.addAll(
                        layer.subList(0,
                                      pathfindingSettings.getBeamWidth()));

                searchStatistics.addToOpened(queue.size());
            }
            
            Cell current = queue.removeFirst();
            
            searchStatistics.incrementVisited();
            searchStatistics.decrementOpened();
            
            if (current.equals(target)) {
                return tracebackPath(target, parents);
            }
            
            if (!current.equals(source)) {
                model.setCellType(current, CellType.VISITED);
            }
            
            neighbourIterable.setStartingCell(current);
            
            for (Cell neighbour : neighbourIterable) {
                if (searchState.haltRequested()) {
                    return List.of();
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                }
                
                searchSleep(pathfindingSettings);
                
                if (parents.containsKey(neighbour)) {
                    continue;
                }
                
                if (!neighbour.equals(target)) {
                    model.setCellType(neighbour, CellType.OPENED);
                }
                    
                searchStatistics.incrementOpened();
                parents.put(neighbour, current);
                queue.addLast(neighbour);
            }
        }
        
        return List.of();
    }
}
