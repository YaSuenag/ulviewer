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
package com.yasuenag.ulviewer.logdata;

public enum LogDecoration {

    TIME("time", "t"),
    UTCTIME("utctime", "utc"),
    UPTIME("uptime", "u"),
    TIMEMILLIS("timemillis", "tm"),
    UPTIMEMILLIS("uptimemillis", "um"),
    TIMENANOS("timenanos", "tn"),
    UPTIMENANOS("uptimenanos", "un"),
    HOSTNAME("hostname", "hn"),
    PID("pid", "p"),
    TID("tid", "ti"),
    LEVEL("level", "l"),
    TAGS("tags", "tg"),
    UNKNOWN("<Unknown>", "unknown");

    private final String decorationName;

    private final String shortName;

    LogDecoration(String decorationName, String shortName){
        this.decorationName = decorationName;
        this.shortName = shortName;
    }

    public String getShortName(){
        return shortName;
    }

    @Override
    public String toString(){
        return decorationName + " (" + shortName + ")";
    }

}
