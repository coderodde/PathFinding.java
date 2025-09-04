package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.ArrayList;
import java.util.Collections;
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
 * @version 1.0.0 (Sep 4, 2025)
 * @since 1.0.0 (Sep 4, 2025)
 */
public final class BeamSearchFindier implements Finder {
    
    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState) {
        
        Queue<HeapNode> open        = new PriorityQueue<>();
        Set<Cell> closed            = new HashSet<>();
        Map<Cell, Cell> parents     = new HashMap<>();
        Map<Cell, Double> distances = new HashMap<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        open.add(new HeapNode(source, 0.0));
        
        parents.put(source, null);
        distances.put(source, 0.0);
        
        while (!open.isEmpty()) {
            Cell currentCell = open.remove().cell;
            
            if (currentCell.equals(target)) {
                return tracebackPath(target, parents);
            }
            
            if (closed.contains(currentCell)) {
                continue;
            }
            
            closed.add(currentCell);
        
            List<Cell> successors = 
                    getSuccessors(
                            currentCell, 
                            target, 
                            neighbourIterable, 
                            distances, 
                            pathfindingSettings);
            
            for (Cell child : successors) {
                if (closed.contains(child)) {
                    continue;
                }
                
                double tentativeDistance =
                        distances.get(currentCell) 
                        + pathfindingSettings.getDiagonalWeight()
                                             .getWeight();
                
                if (!distances.containsKey(child) ||
                     distances.get(child) > tentativeDistance) {
                    distances.put(child, tentativeDistance);
                    parents.put(child, currentCell);
                    open.add(
                            new HeapNode(
                                    child,
                                    tentativeDistance + 
                                    pathfindingSettings.getHeuristicFunction()
                                                       .estimate(child, 
                                                                 target)));
                }
            }
        }
        
        return List.of();
    }
    
    private List<Cell> getSuccessors(Cell currentCell,
                                     Cell targetCell,
                                     GridCellNeighbourIterable iterable,
                                     Map<Cell, Double> distances,
                                     PathfindingSettings pathfindingSettingss) {
        
        List<Cell> successors = new ArrayList<>();
        Map<Cell, Double> costMap = new HashMap<>();
        iterable.setStartingCell(currentCell);
        
        for (Cell successor : iterable) {
            successors.add(successor);
            costMap.put(successor, 
                        distances.get(currentCell) + 
                        pathfindingSettingss.getDiagonalWeight().getWeight() +
                        pathfindingSettingss.getHeuristicFunction()
                                            .estimate(successor, targetCell));
        }
        
        Collections.sort(successors, (a, b) -> {
            return Double.compare(costMap.get(a), costMap.get(b));
        });
        
        return successors.subList(
                0,
                Math.min(successors.size(), 
                         pathfindingSettingss.getBeamWidth()));
    }

    private static final class HeapNode implements Comparable<HeapNode> {

        Cell cell;
        double f;
        
        public HeapNode(Cell cell, double f) {
            this.cell = cell;
            this.f = f;
        }
        
        @Override
        public int compareTo(HeapNode o) {
            return Double.compare(f, o.f);
        }
    }
}
