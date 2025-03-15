/*
 * Copyright (C) 2019, 2025, Yasumasa Suenaga
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

module ulviewer {
    requires javafx.fxml;
    requires javafx.controls;

    exports com.yasuenag.ulviewer to javafx.graphics;

    opens com.yasuenag.ulviewer.ui to javafx.base, javafx.fxml;
    opens com.yasuenag.ulviewer.ui.chart to javafx.fxml;
    opens com.yasuenag.ulviewer.ui.table to javafx.fxml;
    opens com.yasuenag.ulviewer.classhisto to javafx.base;
    opens com.yasuenag.ulviewer.classload to javafx.base;
}
