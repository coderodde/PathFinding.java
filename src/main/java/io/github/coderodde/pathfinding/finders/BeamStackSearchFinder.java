package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
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
            List<Cell> path = search(model, 
                                     U,
                                     beamStack,
                                     neighbourIterable,
                                     pathfindingSettings);
            
            System.out.println("bye bye");
            
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
                                     PathfindingSettings pathfindingSettings) {
        
        Map<Integer, PriorityQueue<HeapNode>> open = new HashMap<>();
        Map<Integer, Set<Cell>> closed             = new HashMap<>();
        Map<Cell, Double> g                        = new HashMap<>();
        Map<Cell, Cell> p                          = new HashMap<>();
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        open.put(0, new PriorityQueue<>());
        open.put(1, new PriorityQueue<>());
        open.get(0).add(new HeapNode(source, 0.0));
        
        closed.put(0, new HashSet<>());
        g.put(source, 0.0);
        p.put(source, null);
        Cell bestGoal = null;
        int layerIndex = 0;
        
        while (!open.get(layerIndex).isEmpty() ||
               !open.get(layerIndex + 1).isEmpty()) {
            
            while (!open.get(layerIndex).isEmpty()) {
                Cell cell = open.get(layerIndex).remove().cell;
                closed.get(layerIndex).add(cell);
                System.out.println("shit!");
                
                if (cell.equals(target)) {
                    U.value = g.get(cell);
                    bestGoal = cell;
                    System.out.println("U.value = " + U.value);
                }
                
                iterable.setStartingCell(cell);
                BeamStackEntry beamStackEntry = beamStack.peek();
                
                for (Cell child : iterable) {
                    if (g.containsKey(child)) {
                        continue;
                    }
                    
                    double gscore = g.get(cell) 
                                  + pathfindingSettings.getWeight(cell, 
                                                                  child);
                    
                    double f = gscore + h.estimate(child, target);
                    
                    if (beamStackEntry.fmin <= f && f <= beamStackEntry.fmax) {
                        open.get(layerIndex + 1).add(new HeapNode(child, f));
                        g.put(child, gscore);
                        p.put(child, cell);
                    }
                }
                
                if (open.get(layerIndex + 1).size() > 
                        pathfindingSettings.getBeamWidth()) {
                    
                    pruneLayer(open.get(layerIndex + 1),
                                        beamStack,
                                        pathfindingSettings);
                }
            }
            
//            if ((1 < layerIndex && layerIndex <= relay) ||
//                (layerIndex > relay + 1)) {
//                closed.get(layerIndex - 1).clear();
//            }
            
            layerIndex++;
            open.put(layerIndex + 1, new PriorityQueue<>());
            closed.put(layerIndex, new HashSet<>());
            beamStack.push(new BeamStackEntry(0, U.value));
            System.out.println("hello");
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
    
    private static void pruneLayer(PriorityQueue<HeapNode> open,
                                   Deque<BeamStackEntry> beamStack,
                                   PathfindingSettings pathfindingSettings) {
        List<HeapNode> keep = new ArrayList<>(open);
        keep.sort((a, b) -> {
            return Double.compare(a.f, b.f);
        });
        
        keep = keep.subList(0, pathfindingSettings.getBeamWidth());
        Set<Cell> keepSet = new HashSet<>();
        
        for (HeapNode heapNode : keep) {
            keepSet.add(heapNode.cell);
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
