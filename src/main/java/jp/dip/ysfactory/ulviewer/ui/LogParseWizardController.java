/*
 * Copyright (C) 2016-2020 Yasumasa Suenaga
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

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import jp.dip.ysfactory.ulviewer.logdata.LogDecoration;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogParseWizardController implements Initializable {

    private static final Pattern DECORATION_LINE_PATTERN = Pattern.compile("^((\\[.+?\\])+)\\s.+");

    private static final Pattern DECORATION_PATTERN = Pattern.compile("\\[(.+?)\\]");

    public static class WizardTableValue{
        private final StringProperty logdata;
        private final ObjectProperty<LogDecoration> logDecoration;

        public WizardTableValue(String log, LogDecoration defaultDecoration){
            logdata = new SimpleStringProperty(log);
            logDecoration = new SimpleObjectProperty<>(defaultDecoration);
        }

        public StringProperty logdataProperty(){
            return logdata;
        }

        public ObjectProperty<LogDecoration> logDecorationProperty(){
            return logDecoration;
        }

    }

    @FXML
    private Stage stage;

    @FXML
    private Label logLine;

    @FXML
    private TableView<WizardTableValue> mappingTable;

    @FXML
    private TableColumn<WizardTableValue, String> fieldColumn;

    @FXML
    private TableColumn<WizardTableValue, LogDecoration> decorationColumn;

    private List<LogDecoration> decorations;

    public void initialize(URL location, ResourceBundle resources) {
        decorations = null;
        fieldColumn.setCellValueFactory(new PropertyValueFactory<>("logdata"));
        decorationColumn.setCellValueFactory(new PropertyValueFactory<>("logDecoration"));
        decorationColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(LogDecoration.values()));
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    private Optional<String> getLogLine(Path path){
        try(Stream<String> stream = Files.lines(path)){
            return stream.map(DECORATION_LINE_PATTERN::matcher)
                          .filter(Matcher::matches)
                          .map(m -> m.group(0))
                          .findAny();
        }
        catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public void setLogLine(List<File> logFiles){
        decorations = null;
        Optional<String> line = logFiles.stream()
                                         .map(f -> this.getLogLine(f.toPath()))
                                         .filter(Optional::isPresent)
                                         .map(Optional::get)
                                         .findAny();

        if(line.isEmpty()){
            return;
        }

        String log = line.get();
        logLine.setText(log);
        Matcher matcher = DECORATION_PATTERN.matcher(line.get());
        mappingTable.getItems().clear();
        while(matcher.find()){
            String decoration = matcher.group(1);

            if(decoration.endsWith("+0000")){
                mappingTable.getItems().add(new WizardTableValue(decoration, LogDecoration.UTCTIME));
            }
            else if(decoration.matches(".+\\+\\d{4}$")){
                mappingTable.getItems().add(new WizardTableValue(decoration, LogDecoration.TIME));
            }
            else if(decoration.matches("^\\d+\\.\\d+s$")){
                mappingTable.getItems().add(new WizardTableValue(decoration, LogDecoration.UPTIME));
            }
            else if(List.of("trace", "debug", "info", "warning", "error").contains(decoration)){
                mappingTable.getItems().add(new WizardTableValue(decoration, LogDecoration.LEVEL));
            }
            else{
                mappingTable.getItems().add(new WizardTableValue(decoration, LogDecoration.UNKNOWN));
            }

            if(log.charAt(matcher.end()) == ' '){
                break;
            }

        }

    }

    public List<LogDecoration> getDecorations(){
        return decorations;
    }

    @FXML
    private void onOkClick(ActionEvent event){
        decorations = mappingTable.getItems()
                                      .stream()
                                      .map(e -> e.logDecorationProperty().getValue())
                                      .collect(Collectors.toList());
        stage.close();
    }

}
