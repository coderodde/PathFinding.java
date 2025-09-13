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
 * @version 1.0.0 (Sep 10, 2025)
 * @since 1.0.0 (Sep 10, 2025)
 */
public final class BidirectionalBeamSearchFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        Deque<Cell> queuef = new ArrayDeque<>();
        Deque<Cell> queueb = new ArrayDeque<>();
        Map<Cell, Cell> parentsf = new HashMap<>();
        Map<Cell, Cell> parentsb = new HashMap<>();
        Map<Cell, Integer> distancef = new HashMap<>();
        Map<Cell, Integer> distanceb = new HashMap<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        queuef.addLast(source);
        queueb.addLast(target);
        
        parentsf.put(source, null);
        parentsb.put(target, null);
        
        distancef.put(source, 0);
        distanceb.put(target, 0);
        
        int bestCost = Integer.MAX_VALUE;
        Cell touchCell = null;
        
        searchStatistics.incrementOpened(); // Inc for forward search.
        searchStatistics.incrementOpened(); // Inc for backward search.
        
        while (!queuef.isEmpty() && !queueb.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            int distf = distancef.get(queuef.getFirst());
            int distb = distanceb.get(queueb.getFirst());
            
            if (touchCell != null && bestCost < distf + distb) {
                return tracebackPath(touchCell, 
                                     parentsf,
                                     parentsb);
            }
            
            if (distf <= distb) {
                if (queuef.size() > pathfindingSettings.getBeamWidth()) {
                    pruneQueueForward(queuef,
                                      target,
                                      pathfindingSettings,
                                      searchStatistics);
                }
                
                Cell current = queuef.removeFirst();
                
                searchStatistics.incrementVisited();
                searchStatistics.decrementOpened();
                
                if (!current.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(current, CellType.VISITED);
                }
                
                if (distanceb.containsKey(current) &&
                        bestCost > distf + distb) {
                    
                    bestCost  = distf + distb;
                    touchCell = current;
                    searchStatistics.incrementVisited();
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
                    
                    if (parentsf.containsKey(neighbour)) {
                        continue;
                    }
                    
                    searchSleep(pathfindingSettings);
                    
                    if (!neighbour.equals(source)) {
                        model.setCellType(neighbour, 
                                          CellType.OPENED);
                    }
                    
                    distancef.put(neighbour,
                                  distancef.get(current) + 1);
                    
                    parentsf.put(neighbour, 
                                 current);
                    
                    queuef.addLast(neighbour);
                    
                    searchStatistics.incrementOpened();
                }
            } else {
                if (queueb.size() > pathfindingSettings.getBeamWidth()) {
                    pruneQueueBackward(queueb,
                                       source,
                                       pathfindingSettings,
                                       searchStatistics);
                }
                
                Cell current = queueb.removeFirst();
                
                searchStatistics.decrementOpened();
                searchStatistics.incrementVisited();
                
                if (!current.getCellType().equals(CellType.TARGET)) {
                    model.setCellType(current, CellType.VISITED);
                }
                
                if (distancef.containsKey(current)
                        && bestCost > distf + distb) {
                    bestCost = distf + distb;
                    touchCell = current;
                    searchStatistics.incrementVisited();
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

                    if (parentsb.containsKey(neighbour)) {
                        continue;
                    }

                    searchSleep(pathfindingSettings);
                    
                    if (!neighbour.equals(target)) {
                        model.setCellType(neighbour, CellType.OPENED);
                    }
                    
                    distanceb.put(neighbour, distanceb.get(current) + 1);
                    parentsb.put(neighbour, current);
                    queueb.addLast(neighbour);
                    
                    searchStatistics.incrementOpened();
                }
            }
        }
        
        return List.of();
    }
    
    private static void pruneQueueForward(Deque<Cell> queue,
                                          Cell target,
                                          PathfindingSettings ps,
                                          SearchStatistics searchStatistics) {
        
        List<Cell> layer = new ArrayList<>(queue);
        searchStatistics.addToOpened(-layer.size());
        
        HeuristicFunction h = ps.getHeuristicFunction();
        
        layer.sort((a, b) -> {
            return Double.compare(h.estimate(a, target),
                                  h.estimate(b, target));
        });
        
        queue.clear();
        queue.addAll(
                layer.subList(0, ps.getBeamWidth()));
        
        searchStatistics.addToOpened(queue.size());
    }
    
    private static void pruneQueueBackward(
            Deque<Cell> queue,
            Cell source,
            PathfindingSettings pathfindingSettings,
            SearchStatistics searchStatistics) {
        
        List<Cell> layer = new ArrayList<>(queue);
        searchStatistics.addToOpened(-layer.size());
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        
        layer.sort((a, b) -> {
            return Double.compare(h.estimate(a, source),
                                  h.estimate(b, source));
        });
        
        queue.clear();
        queue.addAll(
                layer.subList(0, pathfindingSettings.getBeamWidth()));
        
        searchStatistics.addToOpened(queue.size());
    }
}
