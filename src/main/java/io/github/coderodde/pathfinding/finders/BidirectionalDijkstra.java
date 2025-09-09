package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import static io.github.coderodde.pathfinding.finders.Finder.tracebackPathBiDijkstra;
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
 * @version 1.0.0 (Sep 8, 2025)
 * @since 1.0.0 (Sep 8, 2025)
 */
public final class BidirectionalDijkstra implements Finder {

    @Override
    public List<Cell> findPath(GridModel model,
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState,
                               SearchStatistics searchStatistics) {
    
        Queue<HeapNode> queuef = new PriorityQueue<>();
        Queue<HeapNode> queueb = new PriorityQueue<>();
        
        Map<Cell, Double> distancef = new HashMap<>();
        Map<Cell, Double> distanceb = new HashMap<>();
        
        Map<Cell, Cell> parentsf = new HashMap<>();
        Map<Cell, Cell> parentsb = new HashMap<>();
        
        Set<Cell> closedf = new HashSet<>();
        Set<Cell> closedb = new HashSet<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        queuef.add(new HeapNode(source, 0.0));
        queueb.add(new HeapNode(target, 0.0));
        
        distancef.put(source, 0.0);
        distanceb.put(target, 0.0);
        
        parentsf.put(source, null);
        parentsb.put(target, null);
        
        searchStatistics.incrementOpened();
        searchStatistics.incrementOpened();
        
        double mu = Double.POSITIVE_INFINITY;
        Cell touchf = null;
        Cell touchb = null;
        
        while (!queuef.isEmpty() && !queueb.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                continue;
            }
            
            Cell currentf = queuef.remove().cell;
            Cell currentb = queueb.remove().cell;
            
            closedf.add(currentf);
            closedb.add(currentb);
            
            searchStatistics.incrementVisited();
            searchStatistics.incrementVisited();
            
            if (!currentf.getCellType().equals(CellType.SOURCE)) {
                model.setCellType(currentf, CellType.VISITED);
            }
            
            if (!currentb.getCellType().equals(CellType.TARGET)) {
                model.setCellType(currentb, CellType.VISITED);
            }
            
            neighbourIterable.setStartingCell(currentf);
            
            for (Cell child : neighbourIterable) {
                if (closedf.contains(child)) {
                    continue;
                }
                
                double tentativeDistance = distancef.get(currentf) 
                                         + pathfindingSettings.getWeight(
                                                 currentf,
                                                 child);
                
                if (!distancef.containsKey(child) ||
                     distancef.get(child) > tentativeDistance) {
                    
                    distancef.put(child, tentativeDistance);
                    parentsf.put(child, currentf);
                    
                    queuef.add(new HeapNode(child, 
                                            tentativeDistance));
                    
                    if (!child.getCellType().equals(CellType.TARGET)) {
                        model.setCellType(child, CellType.OPENED);
                    }
                    
                    searchSleep(pathfindingSettings);
                    searchStatistics.incrementOpened();
                }
                
                if (closedb.contains(child)) {
                    double shortestPathUpperBound = 
                            distancef.get(currentf) +
                            pathfindingSettings.getWeight(currentf, child)+ 
                            distanceb.get(child);
                    
                    if (mu > shortestPathUpperBound) {
                        mu = shortestPathUpperBound;
                        touchf = currentf;
                        touchb = child;
                    }
                }
            }
            
            neighbourIterable.setStartingCell(currentb);
            
            for (Cell parent : neighbourIterable) {
                if (closedb.contains(parent)) {
                    continue;
                }
                
                double tentativeDistance 
                        = distanceb.get(currentb) 
                        + pathfindingSettings.getWeight(parent, currentb);
                
                if (!distanceb.containsKey(parent) ||
                     distanceb.get(parent) > tentativeDistance) {
                    
                    distanceb.put(parent, tentativeDistance);
                    parentsb.put(parent, currentb);
                    
                    queueb.add(new HeapNode(parent,
                                            tentativeDistance));
                    
                    if (!parent.getCellType().equals(CellType.SOURCE)) {
                        model.setCellType(parent, CellType.OPENED);
                    }
                    
                    searchSleep(pathfindingSettings);
                    searchStatistics.incrementOpened();
                }
                
                if (closedf.contains(parent)) {
                    double shortestPathUpperBound = 
                            distancef.get(parent) + 
                            pathfindingSettings.getWeight(parent, currentb) +
                            distanceb.get(currentb);
                    
                    if (mu > shortestPathUpperBound) {
                        mu = shortestPathUpperBound;
                        touchf = parent;
                        touchb = currentb;
                    }
                }
            }
            
            if (queuef.element().f + queueb.element().f >= mu) {
                return tracebackPathBiDijkstra(touchf,
                                               touchb,
                                               parentsf,
                                               parentsb);
            }
        }
        
        return List.of();
    }
}
