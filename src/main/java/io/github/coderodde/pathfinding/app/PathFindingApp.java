package io.github.coderodde.pathfinding.app;

import static io.github.coderodde.pathfinding.Configuration.DEFAULT_CELL_WIDTH_HEIGHT;
import io.github.coderodde.pathfinding.controller.GridController;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.GridBounds;
import io.github.coderodde.pathfinding.view.GridView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class implements a JavaFX program showcasing the pathfinding in grid-
 * based games.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class PathFindingApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        GridView view = new GridView();
        view.setCellWidthHeight(DEFAULT_CELL_WIDTH_HEIGHT);
        view.initializeState(); // Mandatory!
        
        GridBounds bounds = 
                new GridBounds(
                        Screen.getPrimary().getBounds(),
                        DEFAULT_CELL_WIDTH_HEIGHT);
        
        GridModel model = new GridModel(bounds.horizontalCells, 
                                        bounds.verticalCells);
        
        model.setGridView(view);    
        
        GridController controller = new GridController();

        controller.setGridModel(model);
        controller.setGridView(view);
        controller.setEventHandlers();
        
        view.setGridModel(model);
        view.setCellWidthHeight(DEFAULT_CELL_WIDTH_HEIGHT);
        
        view.drawBorders();
        view.drawAllCels();

        SettingsPane settingsPane = new SettingsPane();
        Pane root = new Pane();
        
        root.getChildren().addAll(view, settingsPane);
        
        // Create the scene
        Scene scene = new Scene(root,
                                view.getWidth(),
                                view.getHeight());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
