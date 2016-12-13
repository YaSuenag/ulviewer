/*
 * Copyright (C) 2016 Yasumasa Suenaga
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
package jp.dip.ysfactory.ulviewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import jp.dip.ysfactory.ulviewer.ui.MainController;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javafx.scene.control.Alert.AlertType.ERROR;

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

    private static void showExceptionDialog(Throwable t){
        String errStr;
        try(StringWriter strWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(strWriter)){
            t.printStackTrace(printWriter);
            errStr = strWriter.toString();
        }
        catch(IOException e){
                e.printStackTrace();
                return;
        }

        TextArea details = new TextArea(errStr);
        details.setEditable(false);

        Alert dialog = new Alert(ERROR);
        dialog.setTitle("Error");
        dialog.setHeaderText(t.getLocalizedMessage());
        dialog.getDialogPane().setExpandableContent(details);
        dialog.showAndWait();
    }

    public static void main(String[] args){
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> showExceptionDialog(e)));
        launch(args);
    }

}
