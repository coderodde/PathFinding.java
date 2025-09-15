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
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 15, 2025)
 * @since 1.0.0 (Sep 15, 2025)
 */
public final class BeamStackSearchFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        Deque<BeamStackEntry> beamStack = new ArrayDeque<>();
        beamStack.push(new BeamStackEntry(0, Double.POSITIVE_INFINITY));
        List<Cell> optimalPath = null;
        DoubleHolder U = new DoubleHolder();
        U.value = Double.POSITIVE_INFINITY;
        
        while (!beamStack.isEmpty()) {
            List<Cell> path = null;
            
            try {
                path = search(model, 
                              U,
                              beamStack,
                              neighbourIterable,
                              pathfindingSettings,
                              searchState,
                              searchStatistics);
                
            } catch (HaltRequestedException ex) {
                return List.of();
            }
            
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            while (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                
                if (searchState.haltRequested()) {
                    return List.of();
                }
            }
            
            if (path != null) {
                optimalPath = path;
                U.value = getPathCost(path, pathfindingSettings);
            }
            
            while (beamStack.peek().fmax >= U.value) {
                beamStack.pop();
            }
            
            if (beamStack.isEmpty()) {
                return optimalPath == null ? List.of() : optimalPath;
            }
            
            beamStack.peek().fmin = beamStack.peek().fmax;
            beamStack.peek().fmax = U.value;
        }
        
        throw new IllegalStateException("Should not get here ever");
    }
    
    private static double 
        getPathCost(List<Cell> path,
                    PathfindingSettings pathsPathfindingSettings) {
           
        double cost = 0.0;
        
        for (int i = 0; i < path.size() - 1; ++i) {
            Cell c1 = path.get(i);
            Cell c2 = path.get(i + 1);
            cost += pathsPathfindingSettings.getWeight(c1, c2);
        }
        
        return cost;
    }
   
    private static List<Cell> search(GridModel model,   
                                     DoubleHolder U,
                                     Deque<BeamStackEntry> beamStack,
                                     GridCellNeighbourIterable iterable,
                                     PathfindingSettings pathfindingSettings,
                                     SearchState searchState,
                                     SearchStatistics searchStatistics) {
        
        Map<Integer, PriorityQueue<HeapNode>> open = new HashMap<>();
        Map<Integer, Set<Cell>> closed             = new HashMap<>();
        Map<Cell, Double> g                        = new HashMap<>();
        Map<Cell, Cell> p                          = new HashMap<>();
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        Cell bestGoal = null;
        int layerIndex = 0;
        
        open.put(0, new PriorityQueue<>());
        open.put(1, new PriorityQueue<>());
        open.get(0).add(new HeapNode(source, 0.0));
        
        closed.put(0, new HashSet<>());
        g.put(source, 0.0);
        p.put(source, null);
        
        searchStatistics.incrementOpened();
        
        while (!open.get(layerIndex).isEmpty() ||
               !open.get(layerIndex + 1).isEmpty()) {
            
            while (!open.get(layerIndex).isEmpty()) {
                
                if (searchState.haltRequested()) {
                    throw new HaltRequestedException();
                }
                
                if (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                    continue;
                }
                
                searchSleep(pathfindingSettings);
                
                Cell cell = open.get(layerIndex).remove().cell;
                closed.get(layerIndex).add(cell);
                searchStatistics.decrementOpened();
                searchStatistics.incrementVisited();
                
                if (!cell.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(cell, CellType.VISITED);
                }
                
                System.out.println("shit!");
                
                if (cell.equals(target)) {
                    U.value = g.get(cell);
                    bestGoal = cell;
                    System.out.println("U.value = " + U.value);
                }
                
                iterable.setStartingCell(cell);
                BeamStackEntry beamStackEntry = beamStack.peek();
                
                for (Cell child : iterable) {
                    if (searchState.haltRequested()) {
                        throw new HaltRequestedException();
                    }
                    
                    while (searchState.pauseRequested()) {
                        searchSleep(pathfindingSettings);
                        
                        if (searchState.haltRequested()) {
                            throw new HaltRequestedException();
                        }
                    } 
                    
                    double tentativeGscore = g.get(cell) 
                                           + pathfindingSettings
                                                   .getWeight(cell, 
                                                              child);
                    
                    if (tentativeGscore < g.getOrDefault(
                                            child, 
                                            Double.POSITIVE_INFINITY)) {
                        
                        double f = tentativeGscore + h.estimate(child, target);
                        
                        BeamStackEntry bse = beamStack.peek();
                        searchSleep(pathfindingSettings);
                        
                        if (bse.fmin <= f && f <= bse.fmax) {
                            open.get(layerIndex + 1)
                                .add(new HeapNode(child, f));
                            
                            g.put(child, tentativeGscore);
                            p.put(child, cell);
                            searchStatistics.incrementOpened();
                        }
                    }
                }
                
                if (open.get(layerIndex + 1).size() > 
                        pathfindingSettings.getBeamWidth()) {
                    
                    pruneLayer(model,
                               open.get(layerIndex + 1),
                               beamStack,
                               pathfindingSettings,
                               searchStatistics);
                }
            }
            
            layerIndex++;
            open.put(layerIndex + 1, new PriorityQueue<>());
            closed.put(layerIndex, new HashSet<>());
            beamStack.push(new BeamStackEntry(0, U.value));
            System.out.println("hello");
        }
        
        for (PriorityQueue<HeapNode> queue : open.values()) {
            for (HeapNode heapNode : queue) {
                Cell cell = heapNode.cell;
                
                if (cell.getCellType().equals(CellType.SOURCE)) {
                    System.err.println("cell.getCellType().equals(CellType.SOURCE)");
                }
                
                if (cell.getCellType().equals(CellType.TARGET)) {
                    System.err.println("cell.getCellType().equals(CellType.TARGET)");
                }
                
                model.setCellType(cell, CellType.FREE);
            }
        }
        
        for (Set<Cell> closedSet : closed.values()) {
            for (Cell cell : closedSet) {
                
                if (!cell.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(cell, CellType.FREE); 
                }
            }
        }
        
        if (bestGoal != null) {
            List<Cell> path = new ArrayList<>();
            Cell cell = bestGoal;
            
            while (cell != null) {
                path.add(cell);
                cell = p.get(cell);
            }
            
            Collections.reverse(path);
            System.out.println("Path: " + path);
            return path;
        }
        
        return null;
    }
    
    private static void pruneLayer(GridModel model,
                                   PriorityQueue<HeapNode> open,
                                   Deque<BeamStackEntry> beamStack,
                                   PathfindingSettings pathfindingSettings,
                                   SearchStatistics searchStatistics) {
        List<HeapNode> keep = new ArrayList<>(open);
        searchStatistics.addToOpened(-keep.size());
        for (HeapNode node : keep) {
            Cell cell = node.cell;
            
            if (!cell.getCellType().equals(CellType.TARGET)) {
                model.setCellType(cell, CellType.FREE);
            }
        }
        
        keep.sort((a, b) -> {
            return Double.compare(a.f, b.f);
        });
        
        keep = keep.subList(0, pathfindingSettings.getBeamWidth());
        searchStatistics.addToOpened(keep.size());
        Set<Cell> keepSet = new HashSet<>();
        
        for (HeapNode heapNode : keep) {
            Cell cell = heapNode.cell;
            keepSet.add(cell);
            
            if (!cell.getCellType().equals(CellType.TARGET)) {
                model.setCellType(cell, CellType.OPENED);
            }
        }
        
        Set<HeapNode> pruned = new HashSet<>();
        double fmin = Double.POSITIVE_INFINITY;
        
        for (HeapNode heapNode : open) {
            if (!keepSet.contains(heapNode.cell)) {
                pruned.add(heapNode);
            }
        }
        
        for (HeapNode heapNode : pruned) {
            fmin = Math.min(fmin, heapNode.f);
        }
        
        beamStack.peek().fmax = fmin;
        
        for (HeapNode heapNode : pruned) {
            open.remove(heapNode);
        }
    }
        
    private static final class BeamStackEntry {
        double fmin;
        double fmax;
        
        BeamStackEntry(double fmin, double fmax) {
            this.fmin = fmin;
            this.fmax = fmax;
        }
    }
    
    private static final class DoubleHolder {
        double value;
    }
}
