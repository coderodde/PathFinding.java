package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
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
        open.add(new HeapNode(source, 0.0));
        
        while (!open.isEmpty()) {
            HeapNode heapNode = open.remove();
            Cell cell = heapNode.cell;
            
            if (cell.equals(target)) {
                return tracebackPath(target, p);
            }
            
            Set<Cell> belowSet = new HashSet<>();
            Set<Cell> aboveSet = new HashSet<>();
            neighbourIterable.setStartingCell(cell);
            
            for (Cell child : neighbourIterable) {
                if (g.containsKey(child)) {
                    double f = g.get(child) + h.estimate(child, target);
                    
                    if (f <= F.get(cell) + C) {
                        belowSet.add(child);
                    } else {
                        aboveSet.add(child);
                    }
                } else {
                    aboveSet.add(child);
                }
            }
            
            for (Cell child : belowSet) {
                if (!openSet.contains(child) && !closed.contains(child)) {
                    double gScore = g.get(cell) + ps.getWeight(cell, child);
                    F.put(child, gScore + h.estimate(child, target));
                    openSet.add(child);
                    open.add(new HeapNode(child, (double) F.get(child)));
                } else if (openSet.contains(child) 
                        && g.get(cell) + 
                        ps.getWeight(cell, child) < g.get(child)) {
                    
                    g.put(child, g.get(cell) + ps.getWeight(cell, child));
                    F.put(child, g.get(child) + h.estimate(child, target));
                    
                } else if (closed.contains(child) 
                        && g.get(cell) +
                        ps.getWeight(cell, child) < g.get(child)) {
                    
                    g.put(child, g.get(cell) + ps.getWeight(cell, child));
                    F.put(child, g.get(child) + h.estimate(child, target));
                    
                    closed.remove(child);
                    openSet.add(child);
                    open.add(new HeapNode(child, (double) F.get(child)));
                }
            }
            
            if (aboveSet.isEmpty()) {
                closed.add(cell);
            } else {
                double fmin = Double.POSITIVE_INFINITY;
                
                for (Cell c : aboveSet) {
                    fmin = Math.min(fmin,
                                    g.get(c) + h.estimate(c, target));
                }
                
                F.put(cell, fmin);
                openSet.add(cell);
                open.add(new HeapNode(cell, fmin));
            }
        }
        
        return List.of();
    }
}
