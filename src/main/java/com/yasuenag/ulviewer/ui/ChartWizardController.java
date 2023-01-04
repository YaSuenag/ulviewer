/*
 * Copyright (C) 2016, 2023, Yasumasa Suenaga
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
package com.yasuenag.ulviewer.ui;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import com.yasuenag.ulviewer.logdata.LogDecoration;

/**
 *
 * @author yasu
 */
public class ChartWizardController implements Initializable{
    
    @FXML
    private GridPane parentGrid;
    
    @FXML
    private ComboBox<LogDecoration> timeRange;
    
    @FXML
    private ComboBox<String> host;
    
    @FXML
    private ComboBox<Integer> pid;
    
    @FXML
    private Button okBtn;
    
    private boolean okClicked;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okClicked = false;
    }
    
    public Scene getScene(){
        return Optional.ofNullable(parentGrid.getScene())
                        .orElseGet(() -> new Scene(parentGrid));
    }
    
    public void setup(List<LogDecoration> decorations, List<MainController.DecoratorValue> values){
        okClicked = false;
        
        timeRange.getItems()
                 .setAll(decorations.stream()
                                    .filter(d -> (d == LogDecoration.TIME) || (d == LogDecoration.UTCTIME) ||
                                                 (d == LogDecoration.UPTIME) || (d == LogDecoration.TIMEMILLIS) ||
                                                 (d == LogDecoration.UPTIMEMILLIS) || (d == LogDecoration.TIMENANOS) ||
                                                 (d == LogDecoration.UPTIMENANOS))
                                    .collect(Collectors.toList()));
        host.getItems()
            .setAll(values.stream()
                          .filter(v -> v.decorationProperty().get() == LogDecoration.HOSTNAME)
                          .flatMap(v -> v.valueProperty().stream())
                          .map(s -> (String)s.categoryProperty().get())
                          .collect(Collectors.toList()));
        pid.getItems()
           .setAll(values.stream()
                         .filter(v -> v.decorationProperty().get() == LogDecoration.PID)
                         .flatMap(v -> v.valueProperty().stream())
                         .map(s -> (Integer)s.categoryProperty().get())
                         .collect(Collectors.toList()));

        timeRange.getSelectionModel().selectFirst();
        host.getSelectionModel().selectFirst();
        pid.getSelectionModel().selectFirst();
    }

    public LogDecoration getTimeRange(){
        return timeRange.getValue();
    }
    
    public String getHost(){
        return host.getValue();
    }
    
    public OptionalInt getPid(){
        return Optional.ofNullable(pid.getValue())
                       .map(OptionalInt::of)
                       .orElse(OptionalInt.empty());
    }
    
    public boolean isOkClicked(){
        return okClicked;
    }
    
    @FXML
    private void onButtonClicked(ActionEvent event){
        okClicked = event.getSource() == okBtn;
        parentGrid.getScene().getWindow().hide();
    }
    
}
