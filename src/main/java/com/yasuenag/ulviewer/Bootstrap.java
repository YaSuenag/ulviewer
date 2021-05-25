/*
 * Copyright (C) 2016, 2021, Yasumasa Suenaga
 *
 * This file is part of UL Viewer.
 *
 * UL Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UL Viewer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with UL Viewer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.yasuenag.ulviewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.yasuenag.ulviewer.ui.MainController;

public class Bootstrap extends Application{

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("UL Viewer");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ui/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        ((MainController)loader.getController()).setStage(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> ExceptionDialog.showExceptionDialog(e)));
        launch(args);
    }

}
