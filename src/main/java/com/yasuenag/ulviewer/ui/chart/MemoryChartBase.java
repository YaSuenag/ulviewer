/*
 * Copyright (C) 2016, 2023, Yasumasa Suenaga
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

import com.yasuenag.ulviewer.logdata.LogData;
import com.yasuenag.ulviewer.logdata.LogLevel;
import com.yasuenag.ulviewer.ui.ChartWizardController;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MemoryChartBase extends ChartViewer{

    private static final Pattern GC_EVENT_PATTERN = Pattern.compile("^(\\[[^\\]]+?\\])+ GC\\((\\d+)\\) (.+)$");

    protected final Map<Long, List<LogData>> gcEventList;

    /*
     * GC ID is defined as uint in HotSpot:
     *   hotspot/src/share/vm/gc/shared/gcId.hpp
     */
    private long gcId;

    private String logBody;

    public MemoryChartBase(List<LogData> logdata, ChartWizardController chartWizardController){
        super(logdata, chartWizardController);

        gcEventList = new HashMap<>();
        gcId = -1;
        logBody = null;
    }

    protected boolean shouldProcess(LogData log){
        gcId = -1;
        logBody = null;
        boolean notValid = !log.getTags().contains("gc") ||
                           (super.chartWizardController.getPid() != log.getPid()) ||
                           !Optional.ofNullable(super.chartWizardController.getHost())
                                    .map(h -> h.equals(log.getHostname()))
                                    .orElse(true);

        if(notValid){
            return false;
        }

        Matcher matcher = GC_EVENT_PATTERN.matcher(log.getMessage());
        if(!matcher.matches()){
            return false;
        }

        long id = Long.parseLong(matcher.group(2));
        String body = matcher.group(3);
        gcEventList.computeIfAbsent(id, k -> new ArrayList<>()).add(log);

        if(log.getLevel() != LogLevel.info){
            return false;
        }

        gcId = id;
        logBody = body;
        return true;
    }

    public long getGcId() {
        return gcId;
    }

    public String getLogBody() {
        return logBody;
    }

}
