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
package com.yasuenag.ulviewer.ui.table;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.yasuenag.ulviewer.classload.ClassLoad;
import com.yasuenag.ulviewer.logdata.LogData;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClassLoadController implements Initializable{

    @FXML
    private TableView<ClassLoad.ClassLoadLogEntry> classTable;

    @FXML
    private TableColumn<ClassLoad.ClassLoadLogEntry, Long> klassColumn;

    @FXML
    private TableColumn<ClassLoad.ClassLoadLogEntry, String> nameColumn;

    @FXML
    private TableColumn<ClassLoad.ClassLoadLogEntry, LogData> loadColumn;

    @FXML
    private TableColumn<ClassLoad.ClassLoadLogEntry, LogData> unloadColumn;

    private static class LogLabelCellFactory extends TableCell<ClassLoad.ClassLoadLogEntry, LogData>{
        @Override
        protected void updateItem(LogData item, boolean empty) {
            super.updateItem(item, empty);

            if(empty || (item == null)){
                setText("N/A");
                setGraphic(null);
            }
            else{
                Hyperlink link = new Hyperlink("yes");
                link.setOnAction(e -> (new Alert(Alert.AlertType.NONE, item.getMessage(), ButtonType.CLOSE)).show());
                setGraphic(link);
                setText(null);
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        klassColumn.setCellValueFactory(new PropertyValueFactory<>("klassPtr"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        loadColumn.setCellValueFactory(new PropertyValueFactory<>("loadLog"));
        unloadColumn.setCellValueFactory(new PropertyValueFactory<>("unloadLog"));

        klassColumn.setCellFactory(p -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || (item == null)) ? null : String.format("0x%x", item));
            }
        });

        loadColumn.setCellFactory(p -> new LogLabelCellFactory());
        unloadColumn.setCellFactory(p -> new LogLabelCellFactory());
    }

    public void setLog(List<LogData> logs, int pid, String hostname){
        ClassLoad classload = ClassLoad.getClassLoad(logs, pid, hostname);
        classTable.getItems().setAll(classload.getLoadClasses().values());
    }

}
