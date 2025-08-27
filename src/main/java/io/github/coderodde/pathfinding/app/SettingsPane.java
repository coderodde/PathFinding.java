package io.github.coderodde.pathfinding.app;

import static io.github.coderodde.pathfinding.Configuration.MAXIMUM_FREQUENCY;
import static io.github.coderodde.pathfinding.Configuration.MINIMUM_FREQUENCY;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

/**
 *
 * @author Rodion "rodde" EFremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public final class SettingsPane extends Pane {
    
    private static final int PIXELS_WIDTH  = 150;
    private static final int PIXELS_HEIGHT = 300;
    private static final int PIXELS_MARGIN = 20;
    private final double[] offset = new double[2];
    
    private boolean bfsAllowDiagonals = false;
    private boolean bfsDontCrossCorners = false;
    private boolean requestBiditectinal = false;
    
    public SettingsPane() {
        setPrefSize(PIXELS_WIDTH,
                    PIXELS_HEIGHT);
        
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        
        setLayoutX(screenRectangle.getWidth()  - PIXELS_WIDTH - PIXELS_MARGIN);
        setLayoutY(PIXELS_MARGIN);
        
        ComboBox<String> frequencyComboBox = new ComboBox();
        frequencyComboBox.setPrefWidth(PIXELS_WIDTH);
        
        for (int frequency = MINIMUM_FREQUENCY; 
                 frequency < MAXIMUM_FREQUENCY + 1; 
                 frequency++) {
            
            frequencyComboBox.getItems()
                             .add(String.format("%d Hz", frequency));
        }
        
        TitledPane frequencyTitledPane = new TitledPane("Frequency", 
                                                        frequencyComboBox);
        VBox bfsVBox = new VBox();
        
        CheckBox bfsVBoxAllowDiagonal    = new CheckBox("Allow diagonal");
        CheckBox bfsVBoxDontCrossCorners = new CheckBox("Don't cross coreners");
        CheckBox bfsVBoxBidirectional    = new CheckBox("Bidirectinal");
        
        bfsVBoxAllowDiagonal.setOnAction(e -> {
            this.bfsAllowDiagonals = bfsVBoxAllowDiagonal.isSelected();
        });
        
        bfsVBoxDontCrossCorners.setOnAction(e -> {
            this.bfsDontCrossCorners = bfsVBoxDontCrossCorners.isSelected();
        });
        
        bfsVBoxBidirectional.setOnAction(e -> {
            this.requestBiditectinal = bfsVBoxBidirectional.isSelected();
        });
        
        bfsVBox.getChildren().addAll(bfsVBoxAllowDiagonal,
                                     bfsVBoxDontCrossCorners,
                                     bfsVBoxBidirectional);
        
        TitledPane bfsFinderSettingsPane = new TitledPane("BFS", bfsVBox);
        
        Accordion accordion = new Accordion();  
        accordion.setPrefWidth(PIXELS_WIDTH);
        accordion.getPanes().addAll(frequencyTitledPane, 
                                    bfsFinderSettingsPane);
        
        getChildren().add(accordion);
        
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);"
               + "-fx-background-radius: 8;"
               + "-fx-border-color: gray;"
               + "-fx-border-radius: 8;");
        
        setOnMousePressed(event -> {
            offset[0] = event.getSceneX() - getLayoutX();
            offset[1] = event.getSceneY() - getLayoutY();
        });
        
        setOnMouseDragged(event -> {
            setLayoutX(event.getSceneX() - offset[0]);
            setLayoutY(event.getSceneY() - offset[1]);
        });
    }
}
