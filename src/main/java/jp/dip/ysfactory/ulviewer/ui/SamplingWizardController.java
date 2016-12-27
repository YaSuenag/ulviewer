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
package jp.dip.ysfactory.ulviewer.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jp.dip.ysfactory.ulviewer.logdata.LogDecoration;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SamplingWizardController implements Initializable{
    
    @FXML
    private GridPane parentGrid;
    
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
    
    public void setup(List<MainController.DecoratorValue> values){
        okClicked = false;
        
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

        host.getSelectionModel().selectFirst();
        pid.getSelectionModel().selectFirst();
    }

    public String getHost(){
        return host.getValue();
    }
    
    public int getPid(){
        return Optional.ofNullable(pid.getValue())
                        .orElse(-1);
    }

    public boolean showDialog(List<MainController.DecoratorValue> values){
        setup(values);
        Stage wizard = new Stage(StageStyle.UTILITY);
        wizard.setScene(getScene());
        wizard.initModality(Modality.APPLICATION_MODAL);
        wizard.setResizable(false);
        wizard.setTitle("Chart wizard");
        wizard.showAndWait();

        return isOkClicked();
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
