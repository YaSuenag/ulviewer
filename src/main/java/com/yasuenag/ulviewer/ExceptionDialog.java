/*
 * Copyright (C) 2017, 2021, Yasumasa Suenaga
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

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javafx.scene.control.Alert.AlertType.ERROR;

/**
 * Created by Yasumasa on 2017/01/30.
 */
public class ExceptionDialog {

    private static void showExceptionDialogInternal(Throwable t){
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

    public static void showExceptionDialog(Throwable t){
        Platform.runLater(() -> showExceptionDialogInternal(t));
    }

}
