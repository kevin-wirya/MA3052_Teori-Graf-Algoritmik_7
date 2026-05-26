package com.grafapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.grafapp.ui.MainView;

/**
 * Arsitektur:
 *   - Model:         com.grafapp.model
 *   - Algorithm:     com.grafapp.algorithm
 *   - Layout:        com.grafapp.layout
 *   - Visualization: com.grafapp.visualization
 *   - UI:            com.grafapp.ui
 *   - Utility:       com.grafapp.util
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainView mainView = new MainView();

        Scene scene = new Scene(mainView, 1440, 860);

        java.net.URL cssUrl = Main.class.getResource("/styles/theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setTitle("Graf Algoritmik \u2014 Integrated Visualization Platform");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start force-directed layout setelah stage tampil
        mainView.startLayout();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
