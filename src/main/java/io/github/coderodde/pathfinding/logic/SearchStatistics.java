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
        
        if (labelVisited != null) {
            this.labelVisited .setText("Visited: N/A");
        }
            
        if (labelOpened != null) {
            this.labelOpened  .setText("Opened: N/A");
        }
            
        if (labelTraced != null) {
            this.labelTraced  .setText("Traced: N/A");
        }
            
        if (labelRejected != null) {
            this.labelRejected.setText("Rejected: N/A");
        }
    }
    
    public void incrementVisited() {
        addToVisited(1);
    }
    
    public void incrementOpened() {
        addToOpened(1);
    }
    
    public void decrementOpened() {
        addToOpened(-1);
    }
    
    public void incrementTraced() {
        traced++;
        
        if (labelTraced == null) {
            return;
        }
        
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
        
        if (labelRejected == null) {
            return;
        }
        
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
        
        if (labelOpened == null) {
            return;
        }
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.OPENED)) {
                labelOpened.setText(String.format("Opened: %d", opened));
            } else {
                labelOpened.setText("Opened: N/A");
            }
        });
    }
    
    public void addToVisited(int delta) {
        visited += delta;
        
        if (labelVisited == null) {
            return;
        }
        
        Platform.runLater(() -> {
            if (labelSelectors.contains(LabelSelector.VISITED)) {
                labelVisited.setText(String.format("Visited: %d", visited));
            } else {
                labelVisited.setText("Visited: N/A");
            }
        });
    }
}
