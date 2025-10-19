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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * This class implements the finder using PEA* - Partial Expansion A* discussed
 * in <a href="https://cdn.aaai.org/AAAI/2000/AAAI00-142.pdf">this paper</a>.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 17, 2025)
 * @since 1.0.0 (Sep 17, 2025)
 */
public final class PEAStarFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings ps,
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
    
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        double C = ps.getCutoff();
        
        Map<Cell, Double> g  = new HashMap<>();
        Map<Cell, Double> F  = new HashMap<>();
        Map<Cell, Cell> p    = new HashMap<>();
        Set<Cell> closed     = new HashSet<>();
        Set<Cell> openSet    = new HashSet<>();
        Queue<HeapNode> open = new PriorityQueue<>();
        HeuristicFunction h  = ps.getHeuristicFunction();
        
        g.put(source, 0.0);
        F.put(source, h.estimate(source, target));
        p.put(source, null);
        openSet.add(source);
        open.add(new HeapNode(source, F.get(source)));
        
        while (!open.isEmpty()) {
            HeapNode heapNode = open.remove();
            Cell cell = heapNode.cell;
            openSet.remove(cell);
            searchStatistics.decrementOpened();
            searchStatistics.incrementVisited();
            
            if (!cell.getCellType().equals(CellType.SOURCE) &&
                !cell.getCellType().equals(CellType.TARGET)) {
                model.setCellType(cell, CellType.VISITED);
            }
            
            if (cell.equals(target)) {
                return tracebackPath(target, p);
            }
            
            List<Cell> belowSet = new ArrayList<>();
            List<Cell> aboveSet = new ArrayList<>();
            neighbourIterable.setStartingCell(cell);
            
            for (Cell child : neighbourIterable) {
                double tentativeDistance = (g.containsKey(child)) 
                        ? g.get(child)
                        : g.get(cell) + ps.getWeight(cell, child);
                
                double f = tentativeDistance + h.estimate(child, target);

                if (f <= F.get(cell) + C) {
                    belowSet.add(child);
                } else {
                    aboveSet.add(child);
                }
                
                searchStatistics.incrementOpened();
                model.setCellType(child, CellType.OPENED);
            }
            
            for (Cell child : belowSet) {
                double tentativeDistance = g.get(cell)
                                         + ps.getWeight(cell, child);
                
                if (!openSet.contains(child) && !closed.contains(child)) {
                    
                    if (searchState.haltRequested()) {
                        return List.of();
                    }
                    
                    while (searchState.pauseRequested()) {
                        searchSleep(ps);
                        
                        if (searchState.haltRequested()) {
                            return List.of();
                        }
                    }
                    
                    searchStatistics.incrementOpened();
                    searchSleep(ps);
                    
                    g.put(child, tentativeDistance);
                    F.put(child, tentativeDistance + h.estimate(child, target));
                    p.put(child, cell);
                    openSet.add(child);
                    open.add(new HeapNode(child, (double) F.get(child)));
                    
                } else if (openSet.contains(child) 
                        && tentativeDistance < g.get(child)) {
                    
                    if (searchState.haltRequested()) {
                        return List.of();
                    }
                    
                    while (searchState.pauseRequested()) {
                        searchSleep(ps);
                        
                        if (searchState.haltRequested()) {
                            return List.of();
                        }
                    }
                    
                    searchStatistics.incrementOpened();
                    searchSleep(ps);
                    
                    g.put(child, tentativeDistance);
                    F.put(child, tentativeDistance + h.estimate(child, target));
                    p.put(child, cell);
                    
                } else if (closed.contains(child) 
                        && tentativeDistance < g.get(child)) {
                    
                    if (searchState.haltRequested()) {
                        return List.of();
                    }
                    
                    while (searchState.pauseRequested()) {
                        searchSleep(ps);
                        
                        if (searchState.haltRequested()) {
                            return List.of();
                        }
                    }
                    
                    searchStatistics.incrementOpened();
                    searchSleep(ps);
                    
                    g.put(child, tentativeDistance);
                    F.put(child, tentativeDistance + h.estimate(child, target));
                    p.put(child, cell);
                    closed.remove(child);
                    openSet.add(child);
                    open.add(new HeapNode(child, (double) F.get(child)));
                }
                
//                if (!child.getCellType().equals(CellType.SOURCE) &&
//                    !child.getCellType().equals(CellType.TARGET)) {
//                    model.setCellType(child, CellType.OPENED);
//                }
            }
            
            if (aboveSet.isEmpty()) {
                closed.add(cell);
            } else {
                double fmin = Double.POSITIVE_INFINITY;
                
                for (Cell c : aboveSet) {
                    double tentativeGScore = 
                            g.containsKey(c) ?
                                g.get(c) :
                                g.get(cell) + ps.getWeight(cell, c);
                    
                    double f = tentativeGScore + h.estimate(c, target);
                    
                    fmin = Math.min(fmin, f);
                }
                
                F.put(cell, fmin);
                openSet.add(cell);
                open.add(new HeapNode(cell, fmin));
                
                if (!cell.getCellType().equals(CellType.SOURCE) &&
                    !cell.getCellType().equals(CellType.TARGET)) {
                    model.setCellType(cell, CellType.OPENED);
                }
                
                searchStatistics.incrementOpened();
            }
        }
        
        return List.of();
    }
}
