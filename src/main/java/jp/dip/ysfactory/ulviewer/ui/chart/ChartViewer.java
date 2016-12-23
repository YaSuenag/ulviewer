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

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogDecoration;
import jp.dip.ysfactory.ulviewer.ui.ChartWizardController;
import jp.dip.ysfactory.ulviewer.ui.MainController;

public abstract class ChartViewer {

    private static final Font MONOSPACE_FONT = new Font("Monospaced Regular", 12.0d);

    public static class XValue{

        private final Number value;

        private final String str;

        public XValue(Number value, String str){
            this.value = value;
            this.str = str;
        }

        public Number getValue(){
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            XValue xValue = (XValue) o;

            return value.equals(xValue.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString(){
            return str;
        }

    }

    protected final List<LogData> logdata;
    
    protected final ChartWizardController chartWizardController;
    
    public ChartViewer(List<LogData> logdata, ChartWizardController chartWizardController){
        this.logdata = logdata;
        this.chartWizardController = chartWizardController;
    }
    
    public boolean showChartWizard(List<LogDecoration> decorations, List<MainController.DecoratorValue> values){
        chartWizardController.setup(decorations, values);
        Stage wizard = new Stage(StageStyle.UTILITY);
        wizard.setScene(chartWizardController.getScene());
        wizard.initModality(Modality.APPLICATION_MODAL);
        wizard.setResizable(false);
        wizard.setTitle("Chart wizard");
        wizard.showAndWait();
        
        return chartWizardController.isOkClicked();
    }

    protected Stage createStage(Chart chart, String title){
        Stage window = new Stage(StageStyle.UTILITY);
        window.setScene(new Scene(chart, 800, 450));
        window.initModality(Modality.NONE);
        window.setTitle(title);

        return window;
    }

    public XValue getXValue(LogData log, LogDecoration decoration){
        switch(decoration){
            case TIME:
                return new XValue(log.getTime().toInstant().toEpochMilli(), log.getTime().toString());

            case UTCTIME:
                return new XValue(log.getUtcTime().toInstant(ZoneOffset.UTC).toEpochMilli(), log.getUtcTime().toString());

            case UPTIME:
                return new XValue(log.getUptime(), log.getUptime() + "s");

            case TIMEMILLIS:
                return new XValue(log.getTimeMillis(), log.getTimeMillis() + "ms");

            case UPTIMEMILLIS:
                return new XValue(log.getUptimeMillis(), log.getUptimeMillis() + "ms");

            case TIMENANOS:
                return new XValue(log.getTimeNanos(), log.getTimeNanos() + "ns");

            case UPTIMENANOS:
                return new XValue(log.getUptimeNanos(), log.getUptimeNanos() + "ns");

            default:
                throw new RuntimeException("Unexpected time range");
        }
    }

    protected void showLogWindow(List<LogData> target, String title){
        TextArea logArea = new TextArea(target.stream()
                                               .map(LogData::getMessage)
                                               .collect(Collectors.joining("\n")));
        logArea.setFont(MONOSPACE_FONT);
        logArea.setEditable(false);

        Stage window = new Stage(StageStyle.UTILITY);
        window.setScene(new Scene(logArea, 500, 300));
        window.initModality(Modality.NONE);
        window.setTitle(title);
        window.show();
    }
    
    public abstract void draw();
    
}
