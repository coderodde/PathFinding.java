package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
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
public final class NBAStarFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model,
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState) {
        
        Queue<HeapNode> opena = new PriorityQueue<>();
        Queue<HeapNode> openb = new PriorityQueue<>();
        
        Map<Cell, Double> distancea = new HashMap<>();
        Map<Cell, Double> distanceb = new HashMap<>();
        
        Map<Cell, Cell> parentsa = new HashMap<>();
        Map<Cell, Cell> parentsb = new HashMap<>();
        
        Set<Cell> closed = new HashSet<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        double totalDistance = 
                pathfindingSettings
                        .getHeuristicFunction()
                        .estimate(source, target);
        
        double fa = totalDistance;
        double fb = totalDistance;
        TouchCell touchCell = new TouchCell();
        BestPathCost bestPathCost = new BestPathCost(Double.POSITIVE_INFINITY);
    
        opena.add(new HeapNode(source, fa));
        openb.add(new HeapNode(target, fb));
        
        distancea.put(source, 0.0);
        distanceb.put(target, 0.0);
        
        parentsa.put(source, null);
        parentsb.put(target, null);
        
        while (!opena.isEmpty() && !openb.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                continue;
            }
            
            searchSleep(pathfindingSettings);
            
            if (opena.size() <= openb.size()) {
                expandInForwardDirection(opena, 
                                         closed,
                                         source, 
                                         target,
                                         fb,
                                         distancea,
                                         distanceb,
                                         parentsa,
                                         bestPathCost, 
                                         touchCell, 
                                         model,
                                         neighbourIterable, 
                                         pathfindingSettings,
                                         searchState);
            } else {
                expandInBackwardDirection(openb,
                                          closed,
                                          source,
                                          target, 
                                          fa, 
                                          distancea,
                                          distanceb, 
                                          parentsb, 
                                          bestPathCost, 
                                          touchCell, 
                                          model,
                                          neighbourIterable, 
                                          pathfindingSettings,
                                          searchState);
            }
        }
        
        if (touchCell.value == null) {
            return List.of();
        }
        
        return tracebackPath(touchCell.value, 
                             parentsa, parentsb);
    }
    
    private static void expandInForwardDirection(
            Queue<HeapNode> open,
            Set<Cell> closed,
            Cell source,
            Cell target,
            double fb,
            Map<Cell, Double> distancea,
            Map<Cell, Double> distanceb,
            Map<Cell, Cell> parents,
            BestPathCost bestPathCost,
            TouchCell touchCell,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings pathfindingSettings,
            SearchState searchState) {
        
        Cell current = open.remove().cell;
        
        if (closed.contains(current)) {
            return;
        }
        
        closed.add(current);
        
        if (!current.getCellType().equals(CellType.SOURCE)) {
            model.setCellType(current, CellType.VISITED);
        }
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        
        if (distancea.get(current) + h.estimate(current, target)
                                   >= bestPathCost.value ||
            fb - h.estimate(current, source) >= bestPathCost.value) {
            // Reject current.
        } else {
            iterable.setStartingCell(current);
            
            for (Cell child : iterable) {
                if (searchState.haltRequested()) {
                    return;
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                }
                
                if (closed.contains(child)) {
                    continue;
                }
                
                double tentativeDistance = distancea.get(current) 
                                         + pathfindingSettings
                                                 .getWeight(current, child);
                
                if (!distancea.containsKey(child) || 
                    distancea.get(child) > tentativeDistance) {
                    
                    searchSleep(pathfindingSettings);
                    
                    model.setCellType(child, CellType.OPENED);
                    
                    distancea.put(child, tentativeDistance);
                    parents.put(child, current);
                    
                    HeapNode hn = 
                            new HeapNode(
                                    child, 
                                    tentativeDistance + h.estimate(child, 
                                                                   target));
                    
                    open.add(hn);
                    
                    if (distanceb.containsKey(child)) {
                        double pathCost = tentativeDistance 
                                        + distanceb.get(child);
                        
                        if (bestPathCost.value > pathCost) {
                            bestPathCost.value = pathCost;
                            touchCell.value = child;
                        }
                    }
                }
            }               
        }
    }
    private static void expandInBackwardDirection(
            Queue<HeapNode> open,
            Set<Cell> closed,
            Cell source,
            Cell target,
            double fa,
            Map<Cell, Double> distancea,
            Map<Cell, Double> distanceb,
            Map<Cell, Cell> parents,
            BestPathCost bestPathCost,
            TouchCell touchCell,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings pathfindingSettings,
            SearchState searchState) {
        
        Cell current = open.remove().cell;
        
        if (closed.contains(current)) {
            return;
        }
        
        closed.add(current);
        
        if (!current.getCellType().equals(CellType.TARGET)) {
            model.setCellType(current, CellType.VISITED);
        }
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        
        if (distanceb.get(current) + h.estimate(current, source)
                                   >= bestPathCost.value ||
            fa - h.estimate(current, target) >= bestPathCost.value) {
            // Reject current.
        } else {
            iterable.setStartingCell(current);
            
            for (Cell parent : iterable) {
                if (searchState.haltRequested()) {
                    return;
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                }
                
                if (closed.contains(parent)) {
                    continue;
                }
                
                double tentativeDistance = distanceb.get(current) 
                                         + pathfindingSettings
                                                 .getWeight(current, parent);
                
                if (!distanceb.containsKey(parent) || 
                    distanceb.get(parent) > tentativeDistance) {
                    
                    searchSleep(pathfindingSettings);
                    
                    model.setCellType(parent, CellType.OPENED);
                    
                    distanceb.put(parent, tentativeDistance);
                    parents.put(parent, current);
                    
                    HeapNode hn = 
                            new HeapNode(
                                    parent, 
                                    tentativeDistance + h.estimate(parent, 
                                                                   source));
                    
                    open.add(hn);
                    
                    if (distancea.containsKey(parent)) {
                        double pathCost = tentativeDistance 
                                        + distancea.get(parent);
                        
                        if (bestPathCost.value > pathCost) {
                            bestPathCost.value = pathCost;
                            touchCell.value = parent;
                        }
                    }
                }
            }               
        }
    }
    
    static final class BestPathCost {
        double value;
        
        BestPathCost(double value) {
            this.value = value;
        }
    }
    
    static final class TouchCell {
        Cell value;
    }
}
