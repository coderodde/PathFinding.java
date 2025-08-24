package io.github.coderodde.pathfinding;

import io.github.coderodde.pathfinding.ui.GridCanvas;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class PathFindingJava extends Application {

    private static final int DEFAULT_CELL_WIDTH_HEIGHT = 26;
    
    @Override
    public void start(Stage stage) throws Exception {
        GridCanvas canvas = new GridCanvas(DEFAULT_CELL_WIDTH_HEIGHT);
//        canvas.setCellWidthHeight(47); // optional, choose cell size
        canvas.draw();                // trigger initial draw

        // Add canvas to a layout (StackPane preserves fixed size)
        StackPane root = new StackPane(canvas);

        // Create the scene
        Scene scene = new Scene(root,
                                canvas.getWidth(),
                                canvas.getHeight());

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
