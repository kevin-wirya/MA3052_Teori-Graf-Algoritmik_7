package com.grafapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.grafapp.ui.MainView;

import java.io.File;

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

        // Load CSS ke working directory
        File cssFile = new File("styles/theme.css");
        if (cssFile.exists()) {
            scene.getStylesheets().add(cssFile.toURI().toString());
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
