/*
 * Copyright (C) 2016-2017 Yasumasa Suenaga
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
package jp.dip.ysfactory.ulviewer.ui.table;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jp.dip.ysfactory.ulviewer.agetable.AgeTable;
import jp.dip.ysfactory.ulviewer.classhisto.ClassHistogram;
import jp.dip.ysfactory.ulviewer.logdata.LogData;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgeTableController implements Initializable {

    private static final Font MONOSPACE_FONT = new Font("Monospaced Regular", 12.0d);

    @FXML
    private ComboBox<AgeTable> logCombo;

    @FXML
    private TableView<Map.Entry<Integer, Long>> ageTable;

    @FXML
    private TableColumn<Map.Entry<Integer, Long>, Integer> ageColumn;

    @FXML
    private TableColumn<Map.Entry<Integer, Long>, Long> bytesColumn;

    @FXML
    private Button showLogButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        bytesColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        logCombo.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> ageTable.getItems().setAll(IntStream.range(0, AgeTable.MAX_AGE)
                                                                                                                            .mapToObj(i -> new AbstractMap.SimpleEntry<>(i + 1, n.getAgeValue()[i]))
                                                                                                                            .collect(Collectors.toList())));
        showLogButton.disableProperty().bind(logCombo.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void onShowLogClicked(ActionEvent event){
        TextArea logArea = new TextArea(logCombo.getSelectionModel()
                                                  .getSelectedItem()
                                                  .getLogData()
                                                  .stream()
                                                  .map(LogData::getMessage)
                                                  .collect(Collectors.joining("\n")));
        logArea.setFont(MONOSPACE_FONT);
        logArea.setEditable(false);

        Stage window = new Stage(StageStyle.UTILITY);
        window.setScene(new Scene(logArea, 500, 300));
        window.initModality(Modality.NONE);
        window.setTitle("Raw log");
        window.show();
    }

    public void setLog(List<LogData> logs, int pid, String hostname){
        logCombo.getItems().setAll(AgeTable.getAgeTableList(logs, pid, hostname));
        logCombo.getSelectionModel().selectFirst();
    }

}
