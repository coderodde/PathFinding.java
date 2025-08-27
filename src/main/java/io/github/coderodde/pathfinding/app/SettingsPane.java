package io.github.coderodde.pathfinding.app;

import javafx.scene.layout.Pane;

/**
 *
 * @author rodio
 */
public final class SettingsPane extends Pane {
    
    private static final int PIXELS_WIDTH  = 150;
    private static final int PIXELS_HEIGHT = 300;
    private final double[] offset = new double[2];
    
    public SettingsPane() {
        setPrefSize(PIXELS_WIDTH,
                    PIXELS_HEIGHT);
        
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);");
        
        setOnMousePressed(event -> {
            System.out.println("pressed");
            offset[0] = event.getSceneX() - getLayoutX();
            offset[1] = event.getSceneY() - getLayoutY();
        });
        
        setOnMouseDragged(event -> {
            System.out.println("dragged");
            setLayoutX(event.getSceneX() - offset[0]);
            setLayoutY(event.getSceneY() - offset[1]);
        });
    }
}
