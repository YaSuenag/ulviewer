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
package jp.dip.ysfactory.ulviewer.ui.chart;

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
import jp.dip.ysfactory.ulviewer.classhisto.ClassHistogram;
import jp.dip.ysfactory.ulviewer.logdata.LogData;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;

public class ClassHistoController implements Initializable {

    private static final Color[] CHART_COLORS = {Color.RED, Color.LIMEGREEN, Color.SKYBLUE, Color.ORANGE, Color.VIOLET};

    private static final Font MONOSPACE_FONT = new Font("Monospaced Regular", 12.0d);

    @FXML
    private ComboBox<ClassHistogram> logCombo;

    @FXML
    private PieChart histoPieChart;

    @FXML
    private TableView<ClassHistogram.HistoDataEntry> histoTable;

    @FXML
    private TableColumn<ClassHistogram.HistoDataEntry, Color> colorColumn;

    @FXML
    private TableColumn<ClassHistogram.HistoDataEntry, Integer> numColumn;

    @FXML
    private TableColumn<ClassHistogram.HistoDataEntry, Long> instColumn;

    @FXML
    private TableColumn<ClassHistogram.HistoDataEntry, Long> byteColumn;

    @FXML
    private TableColumn<ClassHistogram.HistoDataEntry, String> nameColumn;

    @FXML
    private Button showLogButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorColumn.setCellFactory(p -> new TableCell<ClassHistogram.HistoDataEntry, Color>(){
            @Override
            protected void updateItem(Color item, boolean empty) {
                super.updateItem(item, empty);
                if(!empty){
                    setText("");
                    setStyle(String.format("-fx-background-color: #%08x;", item.hashCode()));
                }
            }
        });

        numColumn.setCellValueFactory(new PropertyValueFactory<>("num"));
        numColumn.setCellValueFactory(new PropertyValueFactory<>("num"));
        instColumn.setCellValueFactory(new PropertyValueFactory<>("instances"));
        byteColumn.setCellValueFactory(new PropertyValueFactory<>("bytes"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        logCombo.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> histoTable.getItems().setAll(n.getEntries()));
        logCombo.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> drawChart(n.getEntries()));

        showLogButton.disableProperty().bind(logCombo.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void onShowLogClicked(ActionEvent event){
        ClassHistogram entry = logCombo.getSelectionModel().getSelectedItem();

        StringJoiner logText = new StringJoiner("\n");
        logText.add(entry.getLogHeader());
        entry.getEntries()
             .stream()
             .map(ClassHistogram.HistoDataEntry::getMessage)
             .forEach(logText::add);
        logText.add(entry.getLogFooter());

        TextArea logArea = new TextArea(logText.toString());
        logArea.setFont(MONOSPACE_FONT);
        logArea.setEditable(false);

        Stage window = new Stage(StageStyle.UTILITY);
        window.setScene(new Scene(logArea, 500, 300));
        window.initModality(Modality.NONE);
        window.setTitle("Raw log");
        window.show();
    }

    private void drawChart(List<ClassHistogram.HistoDataEntry> entries){
        long others = entries.stream()
                              .skip(CHART_COLORS.length)
                              .mapToLong(ClassHistogram.HistoDataEntry::getBytes)
                              .sum();

        histoPieChart.getData().clear();
        ObservableList<PieChart.Data> chartData = histoPieChart.getData();
        PieChart.Data data;

        for(int Cnt = 0; Cnt < CHART_COLORS.length; Cnt++){
            data = new PieChart.Data(entries.get(Cnt).getName(), entries.get(Cnt).getBytes());
            chartData.add(data);
            data.getNode().setStyle(String.format("-fx-pie-color: #%08x;", CHART_COLORS[Cnt].hashCode()));
        }

        data = new PieChart.Data("Others", others);
        chartData.add(data);
        data.getNode().setStyle("-fx-pie-color: gray;");
    }

    public void setLog(List<LogData> logs, int pid, String hostname){
        List<ClassHistogram> histoData = ClassHistogram.getClassHistogramList(logs, pid, hostname);

        histoData.forEach(h -> {
            for(int Cnt = 0; Cnt < CHART_COLORS.length; Cnt++){
                h.getEntries().get(Cnt).setColor(CHART_COLORS[Cnt]);
            }
        });

        logCombo.getItems().setAll(histoData);
        logCombo.getSelectionModel().selectFirst();
    }

}
