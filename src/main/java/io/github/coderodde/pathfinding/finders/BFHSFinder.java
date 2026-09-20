package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.util.DoublePriorityBinaryHeap;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This finder implements the BFHS 
 * (<a href="">Breadth-first heuristic search</a>) algorithm.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 20, 2026)
 * @since 1.0.0 (Sep 20, 2026)
 */
public final class BFHSFinder implements Finder {

    private int upperBound = Integer.MAX_VALUE;
    
    public int getUpperBound() {
        return upperBound;
    }
    
    public void setUpperBound(int upperBound) {
        this.upperBound = checkUpperBound(upperBound);
    }
    
    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        return findPathImpl(model, 
                            neighbourIterable, 
                            pathfindingSettings, 
                            searchState, 
                            searchStatistics,
                            model.getSourceGridCell(),
                            model.getTargetGridCell(), 
                            upperBound);
    }
    
    private List<Cell> findPathImpl(GridModel model,
                                    GridCellNeighbourIterable neighbourIterable,
                                    PathfindingSettings pathfindingSettings,
                                    SearchState searchState,
                                    SearchStatistics searchStatistics,
                                    Cell source,
                                    Cell target,
                                    int upperBound) {
        
        if (source.equals(target)) {
            return List.of(target);
        }
        
        Map<Cell, Integer> g      = new HashMap<>();
        Map<Cell, Cell> ancestors = new HashMap<>();
        
        List<DoublePriorityBinaryHeap<Cell>> open = new ArrayList<>();
        List<Set<Cell>> closed                    = new ArrayList<>();
        
        open.addLast(new DoublePriorityBinaryHeap<>());
        open.addLast(new DoublePriorityBinaryHeap<>());
        
        closed.addLast(new HashSet<>());
        
        open.getFirst().insert(source, 0.0);
        
        g.put(source, 0);
        ancestors.put(source, null);
        
        int level = 0;
        int relayLevel = upperBound / 2;
        
        while (!open.get(level).isEmpty() || !open.get(level + 1).isEmpty()) {
            Cell n = open.get(level).extractTop();
            
            closed.get(level).add(n);
            
            Cell solution = expandNode(model, 
                                       neighbourIterable, 
                                       n,
                                       level,
                                       relayLevel, 
                                       upperBound, 
                                       open,
                                       closed, 
                                       g,
                                       ancestors, 
                                       pathfindingSettings);
            
            if (solution != null) {
                List<Cell> path1;
                List<Cell> path2;
                
                Cell middle = ancestors.get(solution);
                
                if (g.get(middle) == 1) {
                    path1 = List.of(source, middle);
                } else {
                    path1 = findPathImpl(model, 
                                         neighbourIterable,
                                         pathfindingSettings, 
                                         searchState, 
                                         searchStatistics,
                                         source,
                                         middle, 
                                         g.get(middle));
                }
                
                if (g.get(solution) - g.get(middle) == 1) {
                    path2 = List.of(middle, solution);
                } else {
                    path2 = findPathImpl(model, 
                                         neighbourIterable,
                                         pathfindingSettings,
                                         searchState, 
                                         searchStatistics,
                                         middle, 
                                         solution, 
                                         g.get(solution) - g.get(middle));
                }
                
                List<Cell> path  = new ArrayList<>(path1);
                
                path.addAll(path2.subList(1, path2.size()));
                
                return path;
            }
        }
        
        return List.of();
    }
    
    private static Cell expandNode(GridModel model,
                                   GridCellNeighbourIterable neighbourIterable,
                                   Cell n,
                                   int level,
                                   int relayLevel,
                                   int upperBound,
                                   List<DoublePriorityBinaryHeap<Cell>> open,
                                   List<Set<Cell>> closed,
                                   Map<Cell, Integer> g,
                                   Map<Cell, Cell> ancestors,
                                   PathfindingSettings pathfindingSettings) {
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        neighbourIterable.setStartingCell(n);
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        for (Cell neighbour : neighbourIterable) {
            if (g.get(n) + 1 + h.estimate(neighbour, target) > upperBound) {
                continue;
            }
            
            if (level > 0 && closed.get(level - 1).contains(neighbour)) {
                continue;
            }
            
            if (closed.get(level).contains(neighbour) 
                    || open.get(level).containsDatum(neighbour)
                    || open.get(level + 1).containsDatum(neighbour)) {
                continue;
            }
            
            g.put(neighbour, g.get(neighbour) + 1);
            
            if (level < relayLevel) {
                ancestors.put(neighbour, source);
            } else if (level == relayLevel) {
                ancestors.put(neighbour, n);
            } else {
                ancestors.put(neighbour, ancestors.get(n));
            }
            
            if (neighbour.equals(target)) {
                return neighbour;
            }
            
            open.get(level + 1)
                .insert(neighbour, 
                        g.get(neighbour) + h.estimate(neighbour, target));
        }
        
        return null;
    }
    
    private int checkUpperBound(int upperBound) {
        if (upperBound < 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Too small upper bound (%d). Must be at least zero (0).", 
                    upperBound));
        }
        
        return upperBound;
    }
}
