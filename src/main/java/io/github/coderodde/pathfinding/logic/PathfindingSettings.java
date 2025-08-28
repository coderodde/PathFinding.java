package io.github.coderodde.pathfinding.logic;

import static io.github.coderodde.pathfinding.Configuration.MAXIMUM_FREQUENCY;
import static io.github.coderodde.pathfinding.Configuration.MINIMUM_FREQUENCY;
import java.util.Objects;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class PathfindingSettings {
    
    public enum DiagonalWeight {
        UNIFORM(1.0),
        SQRT(Math.sqrt(2.0));
        
        private final double weight;
        
        private DiagonalWeight(double weight) {
            this.weight = weight;
        }
        
        public double getWeight() {
            return weight;
        }
    }
    
    private boolean allowDiagonals;
    private boolean dontCrossCorners;
    private boolean bidirectional;
    private int frequency = MAXIMUM_FREQUENCY;
    private DiagonalWeight diagonalWeight = DiagonalWeight.SQRT;

    public boolean allowDiagonals() {
        return allowDiagonals;
    }

    public void setAllowDiagonals(boolean allowDiagonals) {
        this.allowDiagonals = allowDiagonals;
    }

    public boolean dontCrossCorners() {
        return dontCrossCorners;
    }

    public void setDontCrossCorners(boolean dontCrossCorners) {
        this.dontCrossCorners = dontCrossCorners;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        checkFrequency(frequency);
        this.frequency = frequency;
    }
    
    public DiagonalWeight getDiagonalWeight() {
        return diagonalWeight;
    }
    
    public void setDiagonalWeight(DiagonalWeight diagonalWeight) {
        this.diagonalWeight = 
                Objects.requireNonNull(
                        diagonalWeight, 
                        "The diagonal weight is null");
    }
    
    public long getWaitTime() {
        return 1000L / frequency;
    }
    
    private void checkFrequency(int frequency) {
        if (frequency < MINIMUM_FREQUENCY) {
            throw new IllegalArgumentException(
                    String.format("frequency(%d) < 1", 
                                  frequency));
        }
        
        if (frequency > MAXIMUM_FREQUENCY) {
            throw new IllegalArgumentException(
                    String.format("frequency(%d) > 10", 
                                  frequency));
        }
    }
}
