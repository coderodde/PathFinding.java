package io.github.coderodde.pathfinding.logic;

import io.github.coderodde.pathfinding.utils.Cell;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class GridCellNeighbourIterator implements Iterator<Cell> {

    private final List<Cell> gridCellNeighbours;
    private final long waitTime;
    private int iterated = 0;
    
    public GridCellNeighbourIterator(Cell startingCell,
                                     GridNodeExpander gridNodeExpander,
                                     PathfindingSettings pathfindingSettings) {
        this.gridCellNeighbours = gridNodeExpander.expand(startingCell);
        this.waitTime = 1000L / pathfindingSettings.getFrequency();
    }
    
    @Override
    public boolean hasNext() {
        return iterated < gridCellNeighbours.size();
    }

    @Override
    public Cell next() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            System.getLogger(
                    GridCellNeighbourIterator
                            .class
                            .getName())
                    .log(System.Logger.Level.ERROR, 
                         (String) null, 
                         ex);
        }
        
        return gridCellNeighbours.get(iterated++);
    }    
}
