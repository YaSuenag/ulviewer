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

import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogDecoration;
import jp.dip.ysfactory.ulviewer.ui.ChartWizardController;

import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MemoryChartBase extends ChartViewer{

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
        public String toString(){
            return str;
        }

    }

    private static final Pattern GC_EVENT_PATTERN = Pattern.compile("^(\\[.+?\\])+ GC\\((\\d+)\\) (.+)$");

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

        if(!log.getLevel().equals("info")){
            return false;
        }

        gcId = id;
        logBody = body;
        return true;
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

    public long getGcId() {
        return gcId;
    }

    public String getLogBody() {
        return logBody;
    }

}
