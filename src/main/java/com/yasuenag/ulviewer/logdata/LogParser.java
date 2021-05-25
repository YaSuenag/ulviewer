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

import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogParser extends Task<List<LogData>> {

    private final List<File> targets;

    private final List<LogDecoration> decorations;

    private final Map<LogDecoration, List<Object>> decorationMap;

    private List<LogData> logs;

    public LogParser(List<File> targets, List<LogDecoration> decorations){
        this.targets = targets;
        this.decorations = decorations;
        decorationMap = new HashMap<>();
    }

    private void putDecorationMap(LogDecoration decoration){
        switch (decoration){
            case HOSTNAME:
                decorationMap.put(LogDecoration.HOSTNAME, logs.stream()
                                                                  .map(LogData::getHostname)
                                                                  .distinct()
                                                                  .sorted()
                                                                  .collect(Collectors.toList()));
                break;

            case PID:
                decorationMap.put(LogDecoration.PID, logs.stream()
                                                             .map(LogData::getPid)
                                                             .distinct()
                                                             .sorted()
                                                             .collect(Collectors.toList()));
                break;

            case TID:
                decorationMap.put(LogDecoration.TID, logs.stream()
                                                             .map(LogData::getTid)
                                                             .distinct()
                                                             .sorted()
                                                             .collect(Collectors.toList()));
                break;

            case LEVEL:
                decorationMap.put(LogDecoration.LEVEL, logs.stream()
                                                               .map(LogData::getLevel)
                                                               .distinct()
                                                               .sorted()
                                                               .collect(Collectors.toList()));
                break;

            case TAGS:
                decorationMap.put(LogDecoration.TAGS, logs.stream()
                                                              .flatMap(l -> l.getTags().stream())
                                                              .distinct()
                                                              .sorted()
                                                              .collect(Collectors.toList()));
                break;
        }
    }

    @Override
    protected List<LogData> call() throws Exception {
        logs = new ArrayList<>();

        for(File f : targets){
            try(Stream<String> lines = Files.lines(f.toPath())){
                logs.addAll(lines.map(l -> new LogData(l, decorations))
                                 .collect(Collectors.toList()));
            }
        }

        decorations.forEach(this::putDecorationMap);
        return logs;
    }

    public Map<LogDecoration, List<Object>> getDecorationMap() {
        return decorationMap;
    }
}
