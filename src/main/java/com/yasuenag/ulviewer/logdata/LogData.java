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
package com.yasuenag.ulviewer.logdata;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
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

    private ZonedDateTime utcTime;

    private OptionalDouble uptime;

    private OptionalLong timeMillis;

    private OptionalLong uptimeMillis;

    private OptionalLong timeNanos;

    private OptionalLong uptimeNanos;

    private String hostname;

    private OptionalInt pid;

    private OptionalInt tid;

    private LogLevel level;

    private Set<String> tags;

    private String message;

    public LogData(String logline, List<LogDecoration> decorations){
        time = null;
        utcTime = null;
        uptime = OptionalDouble.empty();
        timeMillis = OptionalLong.empty();
        uptimeMillis = OptionalLong.empty();
        timeNanos = OptionalLong.empty();
        uptimeNanos = OptionalLong.empty();
        hostname = "<Unknown>";
        pid = OptionalInt.empty();
        tid = OptionalInt.empty();
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
                    utcTime = ZonedDateTime.parse(decorationStr, LOG_DATETIME_FORMATTER);
                    break;

                case UPTIME:
                    uptime = OptionalDouble.of(Double.parseDouble(
                                 decorationStr.substring(0, decorationStr.length() - 1) // Remove last 1 char ("s")
                             ));
                    break;

                case TIMEMILLIS:
                    timeMillis = OptionalLong.of(Long.parseLong(
                                     decorationStr.substring(0, decorationStr.length() - 2) // Remove last 2 chars ("ms")
                                 ));
                    break;

                case UPTIMEMILLIS:
                    uptimeMillis = OptionalLong.of(Long.parseLong(
                                       decorationStr.substring(0, decorationStr.length() - 2) // Remove last 2 chars ("ms")
                                   ));
                    break;

                case TIMENANOS:
                    timeNanos = OptionalLong.of(Long.parseLong(
                                    decorationStr.substring(0, decorationStr.length() - 2) // Remove last 2 chars ("ns")
                                ));
                    break;

                case UPTIMENANOS:
                    uptimeNanos = OptionalLong.of(Long.parseLong(
                                      decorationStr.substring(0, decorationStr.length() - 2) // Remove last 2 chars ("ns")
                                  ));
                    break;

                case HOSTNAME:
                    hostname = decorationStr;
                    break;

                case PID:
                    pid = OptionalInt.of(Integer.parseInt(decorationStr));
                    break;

                case TID:
                    tid = OptionalInt.of(Integer.parseInt(decorationStr));
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

    public ZonedDateTime getUtcTime() {
        return utcTime;
    }

    public OptionalDouble getUptime() {
        return uptime;
    }

    public OptionalLong getTimeMillis() {
        return timeMillis;
    }

    public OptionalLong getUptimeMillis() {
        return uptimeMillis;
    }

    public OptionalLong getTimeNanos() {
        return timeNanos;
    }

    public OptionalLong getUptimeNanos() {
        return uptimeNanos;
    }

    public String getHostname() {
        return hostname;
    }

    public OptionalInt getPid() {
        return pid;
    }

    public OptionalInt getTid() {
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

    @Override
    public String toString() {
        return message;
    }
}
