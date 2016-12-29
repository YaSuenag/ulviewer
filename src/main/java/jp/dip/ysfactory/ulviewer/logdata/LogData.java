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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class LogData {

    private static final Pattern DECORATION_PATTERN = Pattern.compile("\\[(.+?)\\]");

    private static final DateTimeFormatter LOG_DATETIME_FORMATTER = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                                                                                         .appendOffset("+HHMM", "0000")
                                                                                                         .toFormatter();

    private ZonedDateTime time;

    private LocalDateTime utcTime;

    private double uptime;

    private long timeMillis;

    private long uptimeMillis;

    private long timeNanos;

    private long uptimeNanos;

    private String hostname;

    private int pid;

    private int tid;

    private LogLevel level;

    private Set<String> tags;

    private String message;

    public LogData(String logline, List<LogDecoration> decorations){
        time = null;
        utcTime = null;
        uptime = Double.NaN;
        timeMillis = -1;
        uptimeMillis = -1;
        timeNanos = -1;
        uptimeNanos = -1;
        hostname = "<Unknown>";
        pid = -1;
        tid = -1;
        level = LogLevel.unknown;
        tags = Collections.singleton("<Unknown>");
        message = logline;

        if(!logline.startsWith("[")){
            return;
        }

        Matcher matcher = DECORATION_PATTERN.matcher(logline);
        for (LogDecoration decoration : decorations){
            matcher.find();
            String decorationStr = matcher.group(1);

            switch(decoration){
                case TIME:
                    time = ZonedDateTime.parse(decorationStr, LOG_DATETIME_FORMATTER);
                    break;

                case UTCTIME:
                    utcTime = LocalDateTime.parse(decorationStr, LOG_DATETIME_FORMATTER);
                    break;

                case UPTIME:
                    uptime = Double.parseDouble(decorationStr.substring(0, decorationStr.length() - 1)); // Remove last 1 char ("s")
                    break;

                case TIMEMILLIS:
                    timeMillis = Long.parseLong(decorationStr.substring(0, decorationStr.length() - 2)); // Remove last 2 chars ("ms")
                    break;

                case UPTIMEMILLIS:
                    uptimeMillis = Long.parseLong(decorationStr.substring(0, decorationStr.length() - 2)); // Remove last 2 chars ("ms")
                    break;

                case TIMENANOS:
                    timeNanos = Long.parseLong(decorationStr.substring(0, decorationStr.length() - 2)); // Remove last 2 chars ("ns")
                    break;

                case UPTIMENANOS:
                    uptimeNanos = Long.parseLong(decorationStr.substring(0, decorationStr.length() - 2)); // Remove last 2 chars ("ns")
                    break;

                case HOSTNAME:
                    hostname = decorationStr;
                    break;

                case PID:
                    pid = Integer.parseInt(decorationStr);
                    break;

                case TID:
                    tid = Integer.parseInt(decorationStr);
                    break;

                case LEVEL:
                    level = LogLevel.valueOf(decorationStr.trim());
                    break;

                case TAGS:
                    tags = Arrays.stream(decorationStr.split(","))
                                  .map(String::trim)
                                  .map(String::intern)
                                  .collect(Collectors.toSet());
                    break;
            }
        }
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public LocalDateTime getUtcTime() {
        return utcTime;
    }

    public double getUptime() {
        return uptime;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public long getUptimeMillis() {
        return uptimeMillis;
    }

    public long getTimeNanos() {
        return timeNanos;
    }

    public long getUptimeNanos() {
        return uptimeNanos;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPid() {
        return pid;
    }

    public int getTid() {
        return tid;
    }

    public LogLevel getLevel() {
        return level;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getMessage() {
        return message;
    }

}
