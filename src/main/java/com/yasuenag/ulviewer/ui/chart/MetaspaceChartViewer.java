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
package com.yasuenag.ulviewer.ui.chart;

import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import com.yasuenag.ulviewer.logdata.LogData;
import com.yasuenag.ulviewer.logdata.LogTimeValue;
import com.yasuenag.ulviewer.ui.ChartWizardController;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaspaceChartViewer extends MemoryChartBase {

    private static final Pattern METASPACE_USAGE_PATTERN = Pattern.compile("^Metaspace: \\d+K->(\\d+)K\\((\\d+)K\\)$");

    private static final String BASE_SYMBOL_STYLE = "-fx-opacity: 0; -fx-background-radius: 7px; -fx-padding: 8px;";

    private final LogTooltip tooltip;

    private final Label capacityLabel;

    private final Label usageLabel;

    public MetaspaceChartViewer(List<LogData> logdata, ChartWizardController chartWizardController){
        super(logdata, chartWizardController);

        capacityLabel = new Label();
        usageLabel = new Label();
        tooltip = new LogTooltip(Arrays.asList(new LogTooltip.GridEntry(Color.RED, new Label("Capacity"), capacityLabel),
                                               new LogTooltip.GridEntry(Color.BLUE, new Label("Usage"), usageLabel)));
    }

    private void setTooltipValue(String xValStr, long capacity, long usage, long gcid){
        tooltip.setText(xValStr + "\n" + "GC ID: " + gcid);
        capacityLabel.setText(capacity + " MB");
        usageLabel.setText(usage + " MB");
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
        yAxis.setLabel("MB");

        var capacitySeries = new XYChart.Series<Number, Number>();
        var usageSeries = new XYChart.Series<Number, Number>();
        var chart = new AreaChart<Number, Number>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.getData().add(capacitySeries);
        chart.getData().add(usageSeries);
        chart.lookup(".series0").setStyle("-fx-fill: red; -fx-stroke: red;"); // capacity
        chart.lookup(".series1").setStyle("-fx-fill: blue; -fx-stroke: blue;"); // usage

        Stage stage = super.createStage(chart, "Metaspace usage");

        for(LogData log : super.logdata){
            long capacity;
            long usage;

            if(!super.shouldProcess(log) || (log.getTags().size() != 2) || !log.getTags().contains("metaspace")){
                continue;
            }

            long gcid = super.getGcId();
            Matcher matcher = METASPACE_USAGE_PATTERN.matcher(super.getLogBody());
            if(!matcher.matches()){
                continue;
            }

            usage = Long.parseLong(matcher.group(1)) / 1024; // in MB
            capacity = Long.parseLong(matcher.group(2)) / 1024; // in MB
            LogTimeValue logTimeValue = LogTimeValue.getLogTimeValue(log, super.chartWizardController.getTimeRange());

            var capacityData = new XYChart.Data<Number, Number>(logTimeValue.getValue(), capacity);
            var usageData = new XYChart.Data<Number, Number>(logTimeValue.getValue(), usage);
            capacitySeries.getData().add(capacityData);
            usageSeries.getData().add(usageData);

            Node capacityDataNode = capacityData.getNode();
            Node usageDataNode = usageData.getNode();
            capacityDataNode.lookup(".chart-area-symbol").setStyle(BASE_SYMBOL_STYLE + "-fx-background-color: white, red;");
            usageDataNode.lookup(".chart-area-symbol").setStyle(BASE_SYMBOL_STYLE + "-fx-background-color: white, blue;");
            capacityDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> capacityDataNode.setOpacity(1.0d));
            capacityDataNode.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> capacityDataNode.setOpacity(0.0d));
            usageDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> usageDataNode.setOpacity(1.0d));
            usageDataNode.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> usageDataNode.setOpacity(0.0d));
            capacityDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> setTooltipValue(logTimeValue.toString(), capacity, usage, gcid));
            usageDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> setTooltipValue(logTimeValue.toString(), capacity, usage, gcid));
            capacityDataNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> super.showLogWindow(super.gcEventList.get(gcid), "GC ID: " + gcid));
            usageDataNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> super.showLogWindow(super.gcEventList.get(gcid), "GC ID: " + gcid));

            Tooltip.install(capacityData.getNode(), tooltip);
            Tooltip.install(usageData.getNode(), tooltip);
        }
        
        if(capacitySeries.getData().size() == 0){
            (new Alert(Alert.AlertType.ERROR, "No GC data", ButtonType.OK)).showAndWait();
            return;
        }

        xAxis.setLowerBound(capacitySeries.getData().get(0).getXValue().doubleValue());
        xAxis.setUpperBound(capacitySeries.getData().get(capacitySeries.getData().size() - 1).getXValue().doubleValue());

        stage.show();
    }
    
}
