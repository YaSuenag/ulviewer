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

import java.util.List;

import javafx.scene.Scene;
import javafx.scene.chart.Chart;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogDecoration;
import jp.dip.ysfactory.ulviewer.ui.ChartWizardController;
import jp.dip.ysfactory.ulviewer.ui.MainController;

public abstract class ChartViewer {
    
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
    
    public abstract void draw();
    
}
