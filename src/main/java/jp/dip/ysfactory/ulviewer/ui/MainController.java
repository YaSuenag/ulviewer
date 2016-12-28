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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogDecoration;
import jp.dip.ysfactory.ulviewer.logdata.LogParser;
import jp.dip.ysfactory.ulviewer.ui.chart.*;
import jp.dip.ysfactory.ulviewer.ui.table.AgeTableController;
import jp.dip.ysfactory.ulviewer.ui.table.ClassLoadController;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable{

    public static class DecoratorSwitch{
        private final BooleanProperty enable;
        private final ObjectProperty<Object> category;

        public DecoratorSwitch(Object cat){
            enable = new SimpleBooleanProperty(true);
            category = new ReadOnlyObjectWrapper<>(cat);
        }

        public BooleanProperty enableProperty() {
            return enable;
        }

        public ObjectProperty<Object> categoryProperty() {
            return category;
        }

        @Override
        public String toString() {
            return category.get().toString();
        }
    }

    public static class DecoratorValue{
        private final ObjectProperty<LogDecoration> decoration;
        private final ObservableList<DecoratorSwitch> value;

        public DecoratorValue(LogDecoration deco, List<Object> vals){
            decoration = new ReadOnlyObjectWrapper<>(deco);
            value = FXCollections.observableList(vals.stream()
                                                      .map(DecoratorSwitch::new)
                                                      .collect(Collectors.toList()));
        }

        public ObjectProperty<LogDecoration> decorationProperty() {
            return decoration;
        }

        ObservableList<DecoratorSwitch> valueProperty() {
            return value;
        }

        public boolean doFilter(LogData logData){
            switch (decoration.get()){
                case HOSTNAME:
                    return value.stream()
                                  .filter(s -> s.enableProperty().get())
                                  .anyMatch(s -> s.categoryProperty().get().equals(logData.getHostname()));

                case PID:
                    return value.stream()
                                  .filter(s -> s.enableProperty().get())
                                  .anyMatch(s -> s.categoryProperty().get().equals(logData.getPid()));

                case TID:
                    return value.stream()
                                  .filter(s -> s.enableProperty().get())
                                  .anyMatch(s -> s.categoryProperty().get().equals(logData.getTid()));

                case LEVEL:
                    return value.stream()
                                  .filter(s -> s.enableProperty().get())
                                  .anyMatch(s -> s.categoryProperty().get().equals(logData.getLevel()));

                case TAGS:
                    return value.stream()
                                  .filter(s -> s.enableProperty().get())
                                  .anyMatch(s -> logData.getTags().contains(s.categoryProperty().get()));

                default:
                    return false;
            }
        }

        @Override
        public String toString(){
            return decoration.get().toString();
        }

    }

    @FXML
    private ComboBox<DecoratorValue> decoratorBox;

    @FXML
    private ListView<DecoratorSwitch> visibleList;

    @FXML
    private TextArea logArea;

    @FXML
    private MenuItem javaHeapChart;

    @FXML
    private MenuItem pauseTimeChart;

    @FXML
    private MenuItem metaspaceChart;

    @FXML
    private MenuItem vmOperationChart;

    @FXML
    private MenuItem classHisto;

    @FXML
    private MenuItem classLoad;

    @FXML
    private MenuItem ageTable;

    private Stage stage;

    private LogParseWizardController logParseWizardController;

    private Scene logParseWizardScene;

    private ChartWizardController chartWizardController;

    private SamplingWizardController samplingWizardController;

    private Scene classHistoScene;

    private ClassHistoController classHistoController;

    private Scene classLoadScene;

    private ClassLoadController classLoadController;

    private Scene ageTableScene;

    private AgeTableController ageTableController;

    private List<LogDecoration> decorations;

    private List<LogData> logs;

    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader logParseWizardLoader = new FXMLLoader(getClass().getResource("logparse-wizard.fxml"));
        FXMLLoader chartWizardLoader = new FXMLLoader(getClass().getResource("chart-wizard.fxml"));
        FXMLLoader samplingWizardLoader = new FXMLLoader(getClass().getResource("sampling-wizard.fxml"));
        FXMLLoader classHistoLoader = new FXMLLoader(getClass().getResource("chart/classhisto.fxml"));
        FXMLLoader classLoadLoader = new FXMLLoader(getClass().getResource("table/classload.fxml"));
        FXMLLoader ageTableLoader = new FXMLLoader(getClass().getResource("table/agetable.fxml"));

        try{
            logParseWizardLoader.load();
            logParseWizardController = logParseWizardLoader.getController();
            logParseWizardScene = new Scene(logParseWizardLoader.getRoot());
            chartWizardLoader.load();
            chartWizardController = chartWizardLoader.getController();
            samplingWizardLoader.load();
            samplingWizardController = samplingWizardLoader.getController();
            classHistoLoader.load();
            classHistoScene = new Scene(classHistoLoader.getRoot());
            classHistoController = classHistoLoader.getController();
            classLoadLoader.load();
            classLoadScene = new Scene(classLoadLoader.getRoot());
            classLoadController = classLoadLoader.getController();
            ageTableLoader.load();
            ageTableScene = new Scene(ageTableLoader.getRoot());
            ageTableController = ageTableLoader.getController();
        }
        catch(IOException e){
            throw new UncheckedIOException(e);
        }

        visibleList.setCellFactory(CheckBoxListCell.forListView(DecoratorSwitch::enableProperty));
        decoratorBox.getSelectionModel()
                      .selectedItemProperty()
                      .addListener((v, o, n) -> Optional.ofNullable(n)
                                                        .ifPresent(d -> visibleList.getItems().setAll(d.valueProperty())));
    }

    private void showLog(){
        logArea.setText(logs.stream()
                              .filter(l -> decoratorBox.getItems()
                                                         .stream()
                                                         .allMatch(d -> d.doFilter(l)))
                              .map(LogData::getMessage)
                              .collect(Collectors.joining("\n")));
    }

    private ChangeListener<? super Boolean> enableSwitchListener = (v, o, n) -> showLog();

    private void setDecoratorListener(){
        decoratorBox.getItems()
                      .stream()
                      .flatMap(d -> d.valueProperty().stream())
                      .forEach(s -> s.enableProperty().addListener(enableSwitchListener));
    }

    private void removeDecoratorListener(){
        decoratorBox.getItems()
                      .stream()
                      .flatMap(d -> d.valueProperty().stream())
                      .forEach(s -> s.enableProperty().removeListener(enableSwitchListener));
    }

    private void onLogParseSucceeded(LogParser parser){
        logs = parser.getValue();
        decoratorBox.getItems()
                      .setAll(parser.getDecorationMap()
                                    .entrySet()
                                    .stream()
                                    .map(e -> new DecoratorValue(e.getKey(), e.getValue()))
                                    .collect(Collectors.toList()));

        if(!decoratorBox.getItems().isEmpty()){
            decoratorBox.getSelectionModel().selectFirst();
        }

        setDecoratorListener();
        showLog();
    }

    @FXML
    private void onOpenClick(ActionEvent event){
        FileChooser dialog = new FileChooser();
        dialog.setTitle("Open logfile");
        List<File> logList = dialog.showOpenMultipleDialog(stage);

        if(logList != null){
            logParseWizardController.setLogLine(logList);

            Stage wizard = new Stage(StageStyle.UTILITY);
            logParseWizardController.setStage(wizard);
            wizard.setScene(logParseWizardScene);
            wizard.initModality(Modality.APPLICATION_MODAL);
            wizard.setResizable(false);
            wizard.setTitle("Log parser wizard");
            wizard.showAndWait();

            decorations = logParseWizardController.getDecorations();
            if(decorations == null){
                return;
            }

            LogParser parser = new LogParser(logList, decorations);
            parser.setOnSucceeded(e -> onLogParseSucceeded(parser));
            parser.setOnFailed(e -> Optional.ofNullable(e.getSource().getException()).ifPresent(t -> { throw new RuntimeException(t); }));
            Thread th = new Thread(parser);
            th.start();
        }
    }

    @FXML
    private void onSelectAllClick(ActionEvent event){
        removeDecoratorListener();
        visibleList.getItems()
                     .forEach(s -> s.enableProperty().set(true));
        setDecoratorListener();
        showLog();
    }

    @FXML
    private void onUnselectAllClick(ActionEvent event){
        removeDecoratorListener();
        visibleList.getItems()
                     .forEach(s -> s.enableProperty().set(false));
        setDecoratorListener();
        showLog();
    }

    @FXML
    private void onChartMenuClicked(ActionEvent event) {
        ChartViewer viewer;

        if(event.getSource().equals(javaHeapChart)){
            viewer = new JavaHeapChartViewer(logs, chartWizardController);
        }
        else if(event.getSource().equals(pauseTimeChart)){
            viewer = new PauseTimeChartViewer(logs, chartWizardController);
        }
        else if(event.getSource().equals(metaspaceChart)){
            viewer = new MetaspaceChartViewer(logs, chartWizardController);
        }
        else if(event.getSource().equals(vmOperationChart)){
            viewer = new VMOperationChartViewer(logs, chartWizardController);
        }
        else{
            throw new RuntimeException("Unknown menu");
        }

        if(viewer.showChartWizard(decorations, decoratorBox.getItems())) {
            viewer.draw();
        }

    }

    @FXML
    private void onSamplingWindowMenuClicked(ActionEvent event){
        if(samplingWizardController.showDialog(decoratorBox.getItems())) {
            Stage window = new Stage(StageStyle.UTILITY);
            window.initModality(Modality.NONE);

            if(event.getSource().equals(classHisto)){
                classHistoController.setLog(logs, samplingWizardController.getPid(), samplingWizardController.getHost());
                window.setScene(classHistoScene);
                window.setTitle("Class histogram");
            }
            else if(event.getSource().equals(classLoad)){
                classLoadController.setLog(logs, samplingWizardController.getPid(), samplingWizardController.getHost());
                window.setScene(classLoadScene);
                window.setTitle("Class loading");
            }
            else if(event.getSource().equals(ageTable)){
                ageTableController.setLog(logs, samplingWizardController.getPid(), samplingWizardController.getHost());
                window.setScene(ageTableScene);
                window.setTitle("Age table");
            }

            window.show();
        }
    }

}
