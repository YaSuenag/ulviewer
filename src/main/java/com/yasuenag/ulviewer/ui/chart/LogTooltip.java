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

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class LogTooltip extends Tooltip {

    public static class GridEntry{

        private final Color color;

        private final Label label;

        private final Label contentLabel;

        public GridEntry(Color color, Label label, Label contentLabel){
            this.color = color;
            this.label = label;
            this.contentLabel = contentLabel;
        }

        public Color getColor(){
            return color;
        }

        public Label getLabel(){
            return label;
        }

        public Label getContentLabel(){
            return contentLabel;
        }

    }

    private static final double LABEL_RECT_SIZE = 10.0d;

    private static final double GRID_HGAP = 5.0d;

    private final List<GridEntry> gridEntries;

    public LogTooltip(List<GridEntry> gridEntries){
        super();

        this.gridEntries = gridEntries;
        GridPane grid = new GridPane();
        grid.setHgap(GRID_HGAP);

        for(int Cnt = 0; Cnt < gridEntries.size(); Cnt++){
            grid.add(new Rectangle(LABEL_RECT_SIZE, LABEL_RECT_SIZE, gridEntries.get(Cnt).getColor()), 0, Cnt);
            grid.add(gridEntries.get(Cnt).getLabel(), 1, Cnt);
            grid.add(gridEntries.get(Cnt).getContentLabel(), 2, Cnt);
        }

        this.setContentDisplay(ContentDisplay.BOTTOM);
        this.setGraphic(grid);
    }

}
