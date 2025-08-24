package io.github.coderodde.pathfinding;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.GridBounds;
import io.github.coderodde.pathfinding.view.GridView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class PathFindingApp extends Application {

    private static final int DEFAULT_CELL_WIDTH_HEIGHT = 26;
    
    @Override
    public void start(Stage stage) throws Exception {
        GridView view = new GridView();
        GridBounds bounds = 
                new GridBounds(
                        Screen.getPrimary().getBounds(),
                        DEFAULT_CELL_WIDTH_HEIGHT);
        
        System.out.println("Grid bounds: " + bounds);
        
        GridModel model = new GridModel(bounds.horizontalCells, 
                                        bounds.verticalCells);
        
        view.setGridModel(model);
        view.setCellWidthHeight(DEFAULT_CELL_WIDTH_HEIGHT);
        view.draw();

        // Add canvas to a layout (StackPane preserves fixed size)
        StackPane root = new StackPane(view);

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
