package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.util.DoublePriorityBinaryHeap;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
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
        
        boolean saveddDontSleep     = pathfindingSettings.dontSleep();
        boolean savedDontColorCells = pathfindingSettings.dontColorCells();
        
        pathfindingSettings.setDontSleep(true);
        pathfindingSettings.setDontColorCells(true);
        
        Finder helperFinder = new AStarFinder();
        
        List<Cell> helperPath = helperFinder.findPath(model, 
                                                      neighbourIterable,
                                                      pathfindingSettings, 
                                                      searchState, 
                                                      searchStatistics);
        
        pathfindingSettings.setDontSleep(saveddDontSleep);
        pathfindingSettings.setDontColorCells(savedDontColorCells);
        
        if (helperPath.isEmpty()) {
            return List.of();
        }
        
        setUpperBound(helperPath.size()); // Set the tightest upper bound.
        
        try {
            return findPathImpl(model, 
                                neighbourIterable, 
                                pathfindingSettings, 
                                searchState, 
                                searchStatistics,
                                model.getSourceGridCell(),
                                model.getTargetGridCell(), 
                                upperBound);
        } catch (HaltRequestedException ex) {
            return List.of();
        }
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
            if (searchState.haltRequested()) {
                throw new HaltRequestedException();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell n = open.get(level).extractTop();
            searchStatistics.decrementOpened();
            
            if (!n.equals(source) && 
                !n.equals(target)) {
                
                model.setCellType(n, CellType.VISITED);
            }
            
            searchStatistics.incrementVisited();
            closed.get(level).add(n);
            
            searchSleep(pathfindingSettings);
            
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
                                       pathfindingSettings,
                                       searchState,
                                       searchStatistics);
            
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
        
        if (level > 0) {
            for (Cell cell : open.get(level - 1)) {
                model.setCellType(cell, CellType.FREE);
            }
            
            open.set(level - 1, null);
        }
        
        if (1 < level && level <= relayLevel || level > relayLevel + 1) {
            for (Cell cell : closed.get(level - 1)) {
                model.setCellType(cell, CellType.FREE);
            }
            
            closed.set(level - 1, null);
        }
        
        ++level;
        open.addLast(new DoublePriorityBinaryHeap<>());
        closed.addLast(new HashSet<>());
        
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
                                   PathfindingSettings pathfindingSettings,
                                   SearchState searchState,
                                   SearchStatistics searchStatistics) {
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        neighbourIterable.setStartingCell(n);
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        expansionLoop:
        for (Cell neighbour : neighbourIterable) {
            if (searchState.haltRequested()) {
                throw new HaltRequestedException();
            }
            
            while (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue expansionLoop;
            }
            
            searchSleep(pathfindingSettings);
            
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
            
            if (!neighbour.getCellType().equals(CellType.TARGET)) {
                model.setCellType(neighbour, CellType.OPENED);
            }
            
            open.get(level + 1)
                .insert(neighbour, 
                        g.get(neighbour) + h.estimate(neighbour, target));
            
            searchStatistics.incrementOpened();
            searchSleep(pathfindingSettings);
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
