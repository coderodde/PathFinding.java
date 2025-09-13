package io.github.coderodde.pathfinding.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 9, 2025)
 * @since 1.0.0 (Sep 9, 2025)
 */
public final class SearchStatistics {
    
    public enum LabelSelector {
        VISITED,
        OPENED,
        TRACED,
        REJECTED,
    }
    
    private int visited;
    private int opened;
    private int traced;
    private int rejected;
    
    private final Label labelVisited;
    private final Label labelOpened;
    private final Label labelTraced;
    private final Label labelRejected;
    private final Set<LabelSelector> labelSelectors = new HashSet<>();
    
    public SearchStatistics(Label labelVisited,
                            Label labelOpened,
                            Label labelTraced,
                            Label labelRejected,
                            LabelSelector... selectors) {
        
        this.labelVisited  = labelVisited;
        this.labelOpened   = labelOpened;
        this.labelTraced   = labelTraced;
        this.labelRejected = labelRejected;
        this.labelSelectors.addAll(Arrays.asList(selectors));
        
        this.labelVisited .setText("Visited: N/A");
        this.labelOpened  .setText("Opened: N/A");
        this.labelTraced  .setText("Traced: N/A");
        this.labelRejected.setText("Rejected: N/A");
    }
    
    public void incrementVisited() {
        visited++;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.VISITED)) {
                labelVisited.setText(String.format("Visited: %d", visited));
            } else {
                labelVisited.setText("Visited: N/A");
            }
        });
    }
    
    public void incrementOpened() {
        opened++;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.OPENED)) {
                labelOpened.setText(String.format("Opened: %d", opened));
            } else {
                labelOpened.setText("Opened: N/A");
            }
        });
    }
    
    public void decrementOpened() {
        opened--;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.OPENED)) {
                labelOpened.setText(String.format("Opened: %d", opened));
            } else {
                labelOpened.setText("Opened: N/A");
            }
        });
    }
    
    public void incrementTraced() {
        traced++;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.TRACED)) {
                labelTraced.setText(String.format("Traced: %d", traced));
            } else {
                labelTraced.setText("Traced: N/A");
            }
        });
    }
    
    public void incrementRejected() {
        rejected++;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.REJECTED)) {
                labelRejected.setText(String.format("Rejected: %d", rejected));
            } else {
                labelRejected.setText("Rejected: N/A");
            }
        });
    }
    
    public void addToOpened(int delta) {
        opened += delta;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.OPENED)) {
                labelVisited.setText(String.format("Opened: %d", visited));
            } else {
                labelVisited.setText("Opened: N/A");
            }
        });
    }
    
    public void addToVisited(int delta) {
        visited += delta;
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.OPENED)) {
                labelVisited.setText(String.format("Visited: %d", visited));
            } else {
                labelVisited.setText("Visited: N/A");
            }
        });
    }

    public int getVisited() {
        return visited;
    }

    public int getOpened() {
        return opened;
    }

    public int getTraced() {
        return traced;
    }
    
    public int getRejected() {
        return rejected;
    }
}
