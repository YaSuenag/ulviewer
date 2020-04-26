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
package jp.dip.ysfactory.ulviewer.ui.chart;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogLevel;
import jp.dip.ysfactory.ulviewer.logdata.LogTimeValue;
import jp.dip.ysfactory.ulviewer.ui.ChartWizardController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VMOperationChartViewer extends ChartViewer {

    private static final Pattern VMOP_START_PATTERN = Pattern.compile("^(\\[.+?\\])+ begin.+?: (.+?),.+$");

    private static final Pattern VMOP_END_PATTERN = Pattern.compile("^(\\[.+?\\])+ end VM_Operation .+$");

    private final Tooltip tooltip;

    private LinkedHashMap<LogTimeValue, List<LogData>> vmOpMap;

    public VMOperationChartViewer(List<LogData> logdata, ChartWizardController chartWizardController) {
        super(logdata, chartWizardController);

        tooltip = new Tooltip();
    }

    private void setTooltipText(String xStr, String opname, double elapsedTime){
        tooltip.setText(String.format("%s\n%s\n%.3f", xStr, opname, elapsedTime));
    }

    private void collectVmOpInfo(){
        vmOpMap = new LinkedHashMap<>();
        List<LogData> vmOpList = null;

        for(LogData log : super.logdata){

            if((super.chartWizardController.getPid() != log.getPid()) ||
                    !Optional.ofNullable(super.chartWizardController.getHost())
                            .map(h -> h.equals(log.getHostname()))
                            .orElse(true)){
                continue;
            }

            if(vmOpList != null){
                vmOpList.add(log);

                if(VMOP_END_PATTERN.matcher(log.getMessage()).matches()){
                    vmOpList = null;
                }

            }
            else if((log.getLevel() == LogLevel.debug) &&
                     (log.getTags().size() == 1) &&
                     log.getTags().contains("vmoperation")){
                Matcher matcher = VMOP_START_PATTERN.matcher(log.getMessage());

                if(matcher.matches()){
                    vmOpList = vmOpMap.computeIfAbsent(LogTimeValue.getLogTimeValue(log, super.chartWizardController.getTimeRange()), k -> new ArrayList<>());
                    vmOpList.add(log);
                }

            }

        }

    }

    @Override
    public void draw() {
        collectVmOpInfo();

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setMinorTickVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setAutoRanging(false);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(false);

        var series = new XYChart.Series<Number, Number>();
        var chart = new ScatterChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.getData().add(series);
        Stage stage = super.createStage(chart, "VM Operations");

        for(Map.Entry<LogTimeValue, List<LogData>> entry : vmOpMap.entrySet()){
            double elapsedTime = LogTimeValue.getLogTimeValue(entry.getValue().get(entry.getValue().size() - 1), super.chartWizardController.getTimeRange()).getValue().doubleValue() - entry.getKey().getValue().doubleValue();
            XYChart.Data<Number, Number> data = new XYChart.Data<>(entry.getKey().getValue(), elapsedTime);
            series.getData().add(data);

            Matcher matcher = VMOP_START_PATTERN.matcher(entry.getValue().get(0).getMessage());
            matcher.matches();
            String opName = matcher.group(2);

            data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, e -> setTooltipText(entry.getKey().toString(), opName, elapsedTime));
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> super.showLogWindow(entry.getValue(), entry.getKey().toString() + ": " + opName));

            Tooltip.install(data.getNode(), tooltip);
        }

        if(series.getData().isEmpty()){
            (new Alert(Alert.AlertType.ERROR, "No VM Operation data", ButtonType.OK)).showAndWait();
            return;
        }

        DoubleSummaryStatistics stats = series.getData()
                                              .stream()
                                              .mapToDouble(d -> d.getXValue().doubleValue())
                                              .summaryStatistics();

        xAxis.setLowerBound(stats.getMin());
        xAxis.setUpperBound(stats.getMax());

        stage.show();
    }

}
