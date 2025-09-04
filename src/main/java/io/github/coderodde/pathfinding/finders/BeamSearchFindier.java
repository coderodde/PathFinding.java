package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.List;

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
        
        return List.of();
    }
}
