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
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.ui.ChartWizardController;

import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaHeapUsageChartViewer extends ChartViewer {
    
    private static final Pattern GC_EVENT_PATTERN = Pattern.compile("^(\\[.+?\\])+ GC\\((\\d+)\\) (.+)$");

    private static final Pattern JAVA_HEAP_USAGE_PATTERN = Pattern.compile("^Pause (.+?) \\d+M->(\\d+)M\\((\\d+)M\\) [0-9.]+ms$");

    private final LogTooltip tooltip;

    private final Label capacityLabel;

    private final Label usageLabel;
    
    public JavaHeapUsageChartViewer(List<LogData> logdata, ChartWizardController chartWizardController){
        super(logdata, chartWizardController);

        capacityLabel = new Label();
        usageLabel = new Label();
        tooltip = new LogTooltip(Arrays.asList(new LogTooltip.GridEntry(Color.RED, new Label("Capacity"), capacityLabel),
                                                 new LogTooltip.GridEntry(Color.BLUE, new Label("Usage"), usageLabel)));
    }

    private void setTooltipValue(String xValStr, long capacity, long usage, String text){
        tooltip.setText(xValStr + "\n" + text);
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

        XYChart.Series<Number, Long> capacitySeries = new XYChart.Series<>();
        XYChart.Series<Number, Long> usageSeries = new XYChart.Series<>();
        AreaChart<Number, Long> chart = new AreaChart(xAxis, yAxis, FXCollections.observableArrayList(capacitySeries, usageSeries));
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.lookup(".series0").setStyle("-fx-fill: red; -fx-stroke: red; -fx-background-color: white, red;"); // capacity
        chart.lookup(".series1").setStyle("-fx-fill: blue; -fx-stroke: blue; -fx-background-color: white, blue;"); // usage

        Stage stage = super.createStage(chart, "Java heap usage");
        Map<Integer, List<LogData>> gcEventList = new HashMap<>();

        for(LogData log : super.logdata){
            Number xValue;
            String xValStr;
            String phase;
            long capacity;
            long usage;

            if(!log.getTags().contains("gc") ||
               (super.chartWizardController.getPid() != log.getPid()) ||
               !Optional.ofNullable(super.chartWizardController.getHost())
                        .map(h -> h.equals(log.getHostname()))
                        .orElse(true)){
                continue;
            }

            Matcher gcMatcher = GC_EVENT_PATTERN.matcher(log.getMessage());
            if(!gcMatcher.matches()){
                continue;
            }

            int gcid = Integer.parseInt(gcMatcher.group(2));
            gcEventList.computeIfAbsent(gcid, k -> new ArrayList<>()).add(log);

            if((log.getTags().size() != 1) || !log.getLevel().equals("info")){
                continue;
            }

            Matcher matcher = JAVA_HEAP_USAGE_PATTERN.matcher(gcMatcher.group(3));
            if(!matcher.matches()){
                continue;
            }

            phase = matcher.group(1);
            usage = Long.parseLong(matcher.group(2));
            capacity = Long.parseLong(matcher.group(3));

            switch (super.chartWizardController.getTimeRange()){
                case TIME:
                    xValue = log.getTime().toInstant().toEpochMilli();
                    xValStr = log.getTime().toString();
                    break;

                case UTCTIME:
                    xValue = log.getUtcTime().toInstant(ZoneOffset.UTC).toEpochMilli();
                    xValStr = log.getUtcTime().toString();
                    break;

                case UPTIME:
                    xValue = log.getUptime();
                    xValStr = log.getUptime() + "s";
                    break;

                case TIMEMILLIS:
                    xValue = log.getTimeMillis();
                    xValStr = log.getTimeMillis() + "ms";
                    break;

                case UPTIMEMILLIS:
                    xValue = log.getUptimeMillis();
                    xValStr = log.getUptimeMillis() + "ms";
                    break;

                case TIMENANOS:
                    xValue = log.getTimeNanos();
                    xValStr = log.getTimeNanos() + "ns";
                    break;

                case UPTIMENANOS:
                    xValue = log.getUptimeNanos();
                    xValStr = log.getUptimeNanos() + "ns";
                    break;

                default:
                    throw new RuntimeException("Unexpected time range");
            }

            XYChart.Data<Number, Long> capacityData = new XYChart.Data<>(xValue, capacity);
            XYChart.Data<Number, Long> usageData = new XYChart.Data<>(xValue, usage);
            capacitySeries.getData().add(capacityData);
            usageSeries.getData().add(usageData);

            Node capacityDataNode = capacityData.getNode();
            Node usageDataNode = usageData.getNode();
            capacityDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> setTooltipValue(xValStr, capacity, usage, phase));
            usageDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> setTooltipValue(xValStr, capacity, usage, phase));
            capacityDataNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> super.showLogWindow(gcEventList.get(gcid), "GC ID: " + gcid));
            usageDataNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> super.showLogWindow(gcEventList.get(gcid), "GC ID: " + gcid));

            Node capacityNodeStyle = capacityDataNode.lookup(".chart-area-symbol");
            Node usageNodeStyle = usageDataNode.lookup(".chart-area-symbol");
            capacityNodeStyle.setStyle("-fx-opacity: 0.0; -fx-background-radius: 10px; -fx-padding: 12px;");
            usageNodeStyle.setStyle("-fx-opacity: 0.0; -fx-background-radius: 10px; -fx-padding: 12px;");

            if(phase.startsWith("Full")){
                capacityNodeStyle.setStyle("-fx-background-color: black;");
                usageNodeStyle.setStyle("-fx-background-color: black;");
            }
            else if(phase.startsWith("Young")) {
                capacityDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> capacityDataNode.setVisible(true));
                capacityDataNode.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> capacityDataNode.setVisible(false));
                usageDataNode.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> usageDataNode.setVisible(true));
                usageDataNode.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, e -> usageDataNode.setVisible(false));
            }
            else{
                capacityNodeStyle.setStyle("-fx-background-color: orange;");
                usageNodeStyle.setStyle("-fx-background-color: orange;");
            }

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
