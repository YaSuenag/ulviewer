/*
 * Copyright (C) 2021, Yasumasa Suenaga
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
package com.yasuenag.ulviewer.ui.chart;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import com.yasuenag.ulviewer.logdata.LogData;
import com.yasuenag.ulviewer.logdata.LogTimeValue;
import com.yasuenag.ulviewer.ui.ChartWizardController;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.chart.StackedAreaChart;

public class CodeCacheChartViewer extends ChartViewer {

    private static final Pattern CODECACHE_USAGE_PATTERN = Pattern.compile("^.+CodeHeap '(?<name>.+?)': size=\\d+Kb used=(?<used>\\d+)Kb max_used=\\d+Kb free=\\d+Kb$");

    public CodeCacheChartViewer(List<LogData> logdata, ChartWizardController chartWizardController){
        super(logdata, chartWizardController);
    }

    @Override
    public void draw() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setLabel("KB");

        var nonProfiledSeries = new XYChart.Series<Number, Number>();
        nonProfiledSeries.setName("non-profiled nmethods");
        var profiledSeries = new XYChart.Series<Number, Number>();
        profiledSeries.setName("profiled nmethods");
        var nonNMethodSeries = new XYChart.Series<Number, Number>();
        nonNMethodSeries.setName("non-nmethods");
        var chart = new StackedAreaChart<Number, Number>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);

        Stage stage = super.createStage(chart, "CodeCache usage");

        for(LogData log : super.logdata){
            if((log.getTags().size() != 2) || !(log.getTags().contains("compilation") && log.getTags().contains("codecache"))){
                continue;
            }

            Matcher matcher = CODECACHE_USAGE_PATTERN.matcher(log.getMessage());
            if(!matcher.matches()){
                continue;
            }

            String name = matcher.group("name");
            long used = Long.parseLong(matcher.group("used"));
            LogTimeValue logTimeValue = LogTimeValue.getLogTimeValue(log, super.chartWizardController.getTimeRange());

            var usedData = new XYChart.Data<Number, Number>(logTimeValue.getValue(), used);

            switch(name){
                case "non-profiled nmethods" -> nonProfiledSeries.getData().add(usedData);
                case "profiled nmethods" -> profiledSeries.getData().add(usedData);
                case "non-nmethods" -> nonNMethodSeries.getData().add(usedData);
                default -> throw new RuntimeException("Unknown code cache area: " + name);
            }

        }

        if(nonProfiledSeries.getData().isEmpty()){
            (new Alert(Alert.AlertType.ERROR, "No CodeCache data", ButtonType.OK)).showAndWait();
            return;
        }

        chart.getData().addAll(List.of(nonProfiledSeries, profiledSeries, nonNMethodSeries));
        stage.show();
    }

}
