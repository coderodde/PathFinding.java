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
import java.util.Queue;
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
        beamStack.push(new BeamStackEntry(0.0, Double.POSITIVE_INFINITY));
        
        List<Cell> optimalPath = null;
        DoubleHolder U = new DoubleHolder();
        U.value = Double.POSITIVE_INFINITY;
        
        while (!beamStack.isEmpty()) {
            System.out.println("outermost");
            List<Cell> path;
            
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
            
            System.out.println("Received path: " + path);
            
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
                double pathCost = getPathCost(path, pathfindingSettings);
                System.out.println("pathCost: " + pathCost + ", U: " + U.value);
                U.value = Math.min(U.value, pathCost);
            }
            
            while (!beamStack.isEmpty() && beamStack.peek().fmax >= U.value) {
                beamStack.pop();
            }
            
            if (beamStack.isEmpty()) {
                return optimalPath == null ? List.of() : optimalPath;
            }
            
            BeamStackEntry top = beamStack.peek();
            top.fmin = top.fmax;
            top.fmax = U.value;
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
        System.out.println("search:"); 
        
        while (!open.get(layerIndex).isEmpty() ||
               !open.get(layerIndex + 1).isEmpty()) {
            System.out.println("search on open outer");
            
            boolean prunedAtThisLayer = false;
            double nextBound = Double.POSITIVE_INFINITY;
            
            while (!open.get(layerIndex).isEmpty()) {
                System.out.println("search on open inner");
                
                if (searchState.haltRequested()) {
                    throw new HaltRequestedException();
                }
                
                while (searchState.pauseRequested()) {
                    searchSleep(pathfindingSettings);
                    
                    if (searchState.haltRequested()) {
                        throw new HaltRequestedException();
                    }
                }
                
                Cell cell = open.get(layerIndex).remove().cell;
                closed.get(layerIndex).add(cell);
                searchStatistics.decrementOpened();
                searchStatistics.incrementVisited();
                
                if (!cell.getCellType().equals(CellType.SOURCE) &&
                    !cell.getCellType().equals(CellType.TARGET)) {
                    model.setCellType(cell, CellType.VISITED);
                }
                
                System.out.println("shit!");
                
                if (cell.equals(target)) {
//                    U.value = g.get(cell);
                    System.out.println("U = " + U.value + ", g(cell) = " + g.get(cell));
                    bestGoal = cell;
                }
                
                iterable.setStartingCell(cell);
                BeamStackEntry bse = beamStack.peek();
                
                for (Cell child : iterable) {
                    if (searchState.haltRequested()) {
                        throw new HaltRequestedException();
                    }
                    
                    while (searchState.pauseRequested()) {
                        searchSleep(pathfindingSettings);
                        System.out.println("sleep 2");
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
                        
                        searchSleep(pathfindingSettings);
                        
                        if (bse.fmin <= f && f < bse.fmax) {
                            Queue<HeapNode> nextOpen = open.get(layerIndex + 1);
                            
                            // Add for the first time or improve the g-cost:
                            nextOpen.removeIf(c -> c.cell.equals(child));
                            nextOpen.add(new HeapNode(child, f));
                            
                            g.put(child, tentativeGscore);
                            p.put(child, cell);
                            searchStatistics.incrementOpened();
                            
                            if (!child.getCellType().equals(CellType.TARGET)) {
                                model.setCellType(cell, CellType.OPENED);
                            }
                        }
                    }
                }
                
                PriorityQueue<HeapNode> nextOpen = open.get(layerIndex + 1);
                
                if (nextOpen.size() > pathfindingSettings.getBeamWidth()) {
                    double fMinPruned = pruneLayer(model,
                                                   nextOpen,
                                                   pathfindingSettings,
                                                         searchStatistics);
                    
                    prunedAtThisLayer = true;
                    nextBound = Math.min(nextBound, fMinPruned);
                }
            }
            
            BeamStackEntry bse = beamStack.peek();
            bse.fmax = prunedAtThisLayer ? 
                       nextBound :
                       Double.POSITIVE_INFINITY;
            
            layerIndex++;
            open.put(layerIndex + 1, new PriorityQueue<>());
            closed.put(layerIndex, new HashSet<>());
            
            if (beamStack.size() == layerIndex) {
                beamStack.push(new BeamStackEntry(0.0, U.value));
            }
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
        System.out.println("before path returning");
        if (bestGoal != null) {
            List<Cell> path = new ArrayList<>();
            
            for (Cell cell = bestGoal; cell != null; cell = p.get(cell)) {
                path.add(cell);
            }
            
            Collections.reverse(path);
            System.out.println("Path: " + path);
            return path;
        }
        
        System.out.println("no path in this search(...)");
        return null;
    }
    
    private static double pruneLayer(GridModel model,
                                     PriorityQueue<HeapNode> open,
                                     PathfindingSettings pathfindingSettings,
                                     SearchStatistics searchStatistics) {
        
        List<HeapNode> all = new ArrayList<>(open);
        searchStatistics.addToOpened(-all.size());
        
        for (HeapNode node : all) {
            Cell cell = node.cell;
            
            if (!cell.getCellType().equals(CellType.TARGET)) {
                model.setCellType(cell, CellType.FREE);
            }
        }
        
        all.sort((a, b) -> Double.compare(a.f, b.f));
        
        List<HeapNode> keep = 
                all.subList(0, pathfindingSettings.getBeamWidth());
        
        double fMinPruned = Double.POSITIVE_INFINITY;
        
        for (HeapNode heapNode : keep) {
            fMinPruned = Math.min(fMinPruned, heapNode.f);
        }
        
        open.clear();
        
        for (HeapNode heapNode : keep) {
            open.add(heapNode);
            Cell cell = heapNode.cell;
            
            if (!cell.getCellType().equals(CellType.TARGET)) {
                model.setCellType(cell, CellType.OPENED);
            }
        }
        
        searchStatistics.addToOpened(open.size());
        
        return fMinPruned;
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
