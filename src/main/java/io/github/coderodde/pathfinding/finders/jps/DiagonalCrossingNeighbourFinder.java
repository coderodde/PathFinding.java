package io.github.coderodde.pathfinding.finders.jps;

import io.github.coderodde.pathfinding.finders.JumpPointSearchFinder;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implements the 
 * {@link io.github.coderodde.pathfinding.finders.JumpPointSearchFinder.NeighbourFinder} 
 * interface for computing neighbours with diagonal movement regardless the 
 * obstacle walls.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Oct 21, 2025)
 * @since 1.0.0 (Oct 21, 2025)
 */
public final class DiagonalCrossingNeighbourFinder 
        implements JumpPointSearchFinder.NeighbourFinder {

    @Override
    public List<Cell> findNeighbours(Cell current, 
                                     Map<Cell, Cell> parentsMap, 
                                     GridModel model, 
                                     PathfindingSettings ps) {
        
        List<Cell> neighbours = new ArrayList<>();
        Cell parent = parentsMap.get(current);

        int x = current.getx();
        int y = current.gety();
        int px;
        int py;
        int dx;
        int dy;

        if (parent != null) {
            px = parent.getx();
            py = parent.gety();

            dx = (x - px) / Math.max(Math.abs(x - px), 1);
            dy = (y - py) / Math.max(Math.abs(y - py), 1);

            // Diagonal search:
            if (dx != 0 && dy != 0) {
                if (model.isWalkable(x, y + dy)) {
                    neighbours.add(model.getCell(x, y + dy));
                }

                if (model.isWalkable(x + dx, y)) {
                    neighbours.add(model.getCell(x + dx, y));
                }

                if (model.isWalkable(x + dx, y + dy)) {
                    neighbours.add(model.getCell(x + dx, y + dy));
                }

                if (model.isWalkable(x - dx, y)) {
                    neighbours.add(model.getCell(x - dx, y + dy));
                }

                if (model.isWalkable(x, y - dy)) {
                    neighbours.add(model.getCell(x + dx, y - dy));
                }
            } else {
                // Once here, search horizontally and vertically:
                if (dx == 0) {
                    if (model.isWalkable(x, y + dy)) {
                        neighbours.add(model.getCell(x, y + dy));
                    }

                    if (model.isWalkable(x + 1, y)) {
                        neighbours.add(model.getCell(x + 1, y + dy));
                    }

                    if (model.isWalkable(x - 1, y)) {
                        neighbours.add(model.getCell(x - 1, y + dy));
                    }
                } else {
                    if (model.isWalkable(x + dx, y)) {
                        neighbours.add(model.getCell(x + dx, y));
                    }

                    if (model.isWalkable(x, y + 1)) {
                        neighbours.add(model.getCell(x + dx, y + 1));
                    }

                    if (model.isWalkable(x, y - 1)) {
                        neighbours.add(model.getCell(x + dx, y - 1));
                    }
                }
            }
        } else {
            // Once here, return all neighbours:
            neighbours.addAll(
                    new GridNodeExpander(model, 
                                         ps).expand(current));
        }

        return neighbours;
    }
}
