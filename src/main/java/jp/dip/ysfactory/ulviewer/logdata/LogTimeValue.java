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
package jp.dip.ysfactory.ulviewer.logdata;

import java.time.ZoneOffset;

public class LogTimeValue {

    private final Number value;

    private final String str;

    public LogTimeValue(Number value, String str){
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

        LogTimeValue logTimeValue = (LogTimeValue) o;

        return value.equals(logTimeValue.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString(){
        return str;
    }

    public static LogTimeValue getLogTimeValue(LogData log, LogDecoration decoration){
        switch(decoration){
            case TIME:
                return new LogTimeValue(log.getTime().toInstant().toEpochMilli(), log.getTime().toString());

            case UTCTIME:
                return new LogTimeValue(log.getUtcTime().toInstant(ZoneOffset.UTC).toEpochMilli(), log.getUtcTime().toString());

            case UPTIME:
                return new LogTimeValue(log.getUptime(), log.getUptime() + "s");

            case TIMEMILLIS:
                return new LogTimeValue(log.getTimeMillis(), log.getTimeMillis() + "ms");

            case UPTIMEMILLIS:
                return new LogTimeValue(log.getUptimeMillis(), log.getUptimeMillis() + "ms");

            case TIMENANOS:
                return new LogTimeValue(log.getTimeNanos(), log.getTimeNanos() + "ns");

            case UPTIMENANOS:
                return new LogTimeValue(log.getUptimeNanos(), log.getUptimeNanos() + "ns");

            default:
                throw new RuntimeException("Unexpected time range");
        }
    }

}
