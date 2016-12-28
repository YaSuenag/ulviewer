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
package jp.dip.ysfactory.ulviewer.ui.chart;

import javafx.collections.FXCollections;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogTimeValue;
import jp.dip.ysfactory.ulviewer.ui.ChartWizardController;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PauseTimeChartViewer extends MemoryChartBase {

    private static final Pattern PAUSE_TIME_PATTERN = Pattern.compile("^Pause (.+?) \\d+M->\\d+M\\(\\d+M\\) ([0-9.]+)ms$");

    private final Tooltip tooltip;

    public PauseTimeChartViewer(List<LogData> logdata, ChartWizardController chartWizardController){
        super(logdata, chartWizardController);

        tooltip = new Tooltip();
    }

    private void setTooltipValue(String xValStr, String text, long gcid){
        tooltip.setText(xValStr + "\n" + text + "\n" + "GC ID: " + gcid);
    }

    @Override
    public void draw() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setLabel("ms");

        XYChart.Series<Number, Number> youngSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> concurrentSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> fullSeries = new XYChart.Series<>();
        ScatterChart<Number, Number> chart = new ScatterChart<Number, Number>(xAxis, yAxis, FXCollections.observableArrayList(youngSeries, concurrentSeries, fullSeries));
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        Stage stage = super.createStage(chart, "Pause time");


        for(LogData log : super.logdata){
            String phase;
            double time;

            if(!super.shouldProcess(log) || (log.getTags().size() != 1)){
                continue;
            }

            long gcid = super.getGcId();
            Matcher matcher = PAUSE_TIME_PATTERN.matcher(super.getLogBody());
            if(!matcher.matches()){
                continue;
            }

            phase = matcher.group(1);
            time = Double.parseDouble(matcher.group(2));
            LogTimeValue logTimeValue = LogTimeValue.getLogTimeValue(log, super.chartWizardController.getTimeRange());

            XYChart.Data<Number, Number> data = new XYChart.Data<>(logTimeValue.getValue(), time);
            if(phase.startsWith("Full")){
                fullSeries.getData().add(data);
                data.getNode().setStyle("-fx-background-color: black;");
            }
            else if(phase.startsWith("Young")) {
                youngSeries.getData().add(data);
                data.getNode().setStyle("-fx-background-color: skyblue;");
            }
            else{
                concurrentSeries.getData().add(data);
                data.getNode().setStyle("-fx-background-color: orange;");
            }

            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> setTooltipValue(logTimeValue.toString(), phase, gcid));
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> super.showLogWindow(super.gcEventList.get(gcid), "GC ID: " + gcid));
            Tooltip.install(data.getNode(), tooltip);
        }
        
        if(youngSeries.getData().isEmpty() && concurrentSeries.getData().isEmpty() && fullSeries.getData().isEmpty()){
            (new Alert(Alert.AlertType.ERROR, "No GC data", ButtonType.OK)).showAndWait();
            return;
        }

        DoubleSummaryStatistics stats = Stream.concat(youngSeries.getData().stream(), Stream.concat(concurrentSeries.getData().stream(), fullSeries.getData().stream()))
                                              .mapToDouble(d -> d.getXValue().doubleValue())
                                              .summaryStatistics();

        xAxis.setLowerBound(stats.getMin());
        xAxis.setUpperBound(stats.getMax());

        stage.show();
    }
    
}
