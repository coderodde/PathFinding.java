package io.github.coderodde.pathfinding.logic;

import static io.github.coderodde.pathfinding.logic.SearchState.CurrentState.IDLE;
import java.util.Objects;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public final class SearchState {
    
    public enum CurrentState {
        SEARCHING,
        PAUSED,
        IDLE,
    }
    
    private volatile CurrentState currentState;
    private volatile boolean requestHalt;
    private volatile boolean requestPause;
    
    public void resetState() {
        requestHalt  = false;
        requestPause = false;
        currentState = IDLE;
    }
    
    public void requestHalt() {
        this.requestHalt = true;
    }
    
    public boolean haltRequested() {
        return requestHalt;
    }
    
    public void requestPause() {
        this.requestPause = true;
    }
    
    public boolean pauseRequested() {
        return requestPause;
    }
    
    public CurrentState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(CurrentState currentState) {
        this.currentState = 
                Objects.requireNonNull(
                        currentState,
                        "The input current state is null");
    }
}
