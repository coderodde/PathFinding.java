package io.github.coderodde.pathfinding.finders.jps.jumpers;

import io.github.coderodde.pathfinding.finders.JumpPointSearchFinder;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;

/**
 * This class implements the algorithm for doing jumps only vertically and
 * horizontally regardless the crossing obstacle walls.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Oct 21, 2025)
 * @since 1.0.0 (Oct 21, 2025)
 */
public final class NoDiagonalJumper implements JumpPointSearchFinder.Jumper {

    /**
     * This method implements jumping when diagonal moves are not allowed.
     * 
     * @param x     the {@code X}-coordinate of the current cell.
     * @param y     the {@code Y}-coordinate of the current cell.
     * @param px    the {@code X}-coordinate of the parent cell.
     * @param py    the {@code Y}-coordinate of the parent cell.
     * @param model the grid model.
     * 
     * @return the next cell.
     */
    @Override
    public Cell jump(int x, 
                     int y, 
                     int px, 
                     int py, 
                     GridModel model) {
    
        int dx = x - px;
        int dy = y - py;
        
        if (!model.isWalkable(x, y)) {
            return null;
        }
        
        if (!model.getCellType(x, y).equals(CellType.SOURCE) &&
            !model.getCellType(x, y).equals(CellType.TARGET)) {
            model.setCellType(x, y, CellType.TRACED);
        }
        
        if (model.getCell(x, y).equals(model.getTargetGridCell())) {
            return model.getTargetGridCell();
        }
        
        if (dx != 0) {
            if ((model.isWalkable(x, y - 1) &&
                !model.isWalkable(x - dx, y - 1)) ||
                (model.isWalkable(x, y + 1) &&
                !model.isWalkable(x - dx, y + 1))) {
                
                return model.getCell(x, y);
            }
        } else if (dy != 0) {
            if ((model.isWalkable(x - 1, y) &&
                !model.isWalkable(x - 1, y - dy)) ||
                (model.isWalkable(x + 1, y) &&
                !model.isWalkable(x + 1, y - dy))) {
                
                return model.getCell(x, y);
            }
            
            if (jump(x + 1, y, x, y, model) != null ||
                jump(x - 1, y, x, y, model) != null) {
                
                return model.getCell(x, y);
            }
        } else {
            throw new IllegalStateException("Should not get here");
        }
        
        return jump(x + dx, 
                    y + dy,
                    x,
                    y, 
                    model);
    }
}
