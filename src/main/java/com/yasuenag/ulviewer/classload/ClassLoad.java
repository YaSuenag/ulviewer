/*
 * Copyright (C) 2016, 2025, Yasumasa Suenaga
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
package com.yasuenag.ulviewer.classload;

import com.yasuenag.ulviewer.logdata.LogData;
import com.yasuenag.ulviewer.logdata.LogLevel;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassLoad {

    // for JDK 9
    private static final Pattern LOAD_PATTERN_JDK9 = Pattern.compile("^(\\[[^\\]]+?\\])+ (\\S+) source: (\\S+) klass: (0x[0-9a-f]+) .+$");

    // for JDK 10 or later
    //   https://bugs.openjdk.org/browse/JDK-8154791
    private static final Pattern LOAD_PATTERN_JDK10_INFO = Pattern.compile("^(\\[[^\\]]+?\\])+ (\\S+) source: (.+)$");
    private static final Pattern LOAD_PATTERN_JDK10_DEBUG = Pattern.compile("^(\\[[^\\]]+?\\])+  klass: (0x[0-9a-f]+) .+$");

    private static final Pattern UNLOAD_PATTERN = Pattern.compile("^(\\[[^\\]]+?\\])+ unloading class (\\S+) (0x[0-9a-f]+)$");

    public static class ClassLoadLogEntry{

        private LogData[] loadLog;

        private LogData unloadLog;

        private String className;

        private String source;

        private long klassPtr;

        public ClassLoadLogEntry(String className, String source, long klassPtr){
            loadLog = null;
            unloadLog = null;
            this.className = className;
            this.source = source;
            this.klassPtr = klassPtr;
        }

        public LogData[] getLoadLog() {
            return loadLog;
        }

        public LogData getUnloadLog() {
            return unloadLog;
        }

        public String getClassName() {
            return className;
        }

        public String getSource() {
            return source;
        }

        public long getKlassPtr() {
            return klassPtr;
        }

    }

    private final Map<Long, ClassLoadLogEntry> loadClasses;

    public ClassLoad(){
        loadClasses = new HashMap<>();
    }

    public Map<Long, ClassLoadLogEntry> getLoadClasses() {
        return loadClasses;
    }

    private static boolean shouldProcess(LogData log, int pid, String hostname){
        boolean notValid = !(log.getTags().contains("class") && (log.getTags().contains("load") || log.getTags().contains("unload"))) ||
                            (pid != log.getPid()) ||
                            !Optional.ofNullable(hostname)
                                     .map(h -> h.equals(log.getHostname()))
                                     .orElse(true);

        return !notValid;
    }

    public static ClassLoad getClassLoad(List<LogData> logs, int pid, String hostname){
        ClassLoad result = new ClassLoad();
        ClassLoadLogEntry cachedEntry = null;

        for(LogData log : logs){

            if(!shouldProcess(log, pid, hostname)){
                continue;
            }

            if(log.getLevel() == LogLevel.info){
                if(log.getTags().contains("load")){
                    Matcher matcher = LOAD_PATTERN_JDK10_INFO.matcher(log.getMessage());
                    if(matcher.matches()){
                        cachedEntry = new ClassLoadLogEntry(matcher.group(2), matcher.group(3), 0 /* It should be set later in debug log */);
                        cachedEntry.loadLog = new LogData[]{log, null};
                    }
                }
                else if(log.getTags().contains("unload")){
                    Matcher matcher = UNLOAD_PATTERN.matcher(log.getMessage());
                    if(matcher.matches()){
                        long klass = Long.decode(matcher.group(3));
                        ClassLoadLogEntry entry = result.loadClasses.computeIfAbsent(klass, k -> new ClassLoadLogEntry(matcher.group(2), null, klass));
                        entry.unloadLog = log;
                    }
                }
            }
            else if(log.getLevel() == LogLevel.debug){
                Matcher matcher = LOAD_PATTERN_JDK9.matcher(log.getMessage());
                if(matcher.matches()){
                    ClassLoadLogEntry entry = new ClassLoadLogEntry(matcher.group(2), matcher.group(3), Long.decode(matcher.group(4)));
                    entry.loadLog = new LogData[]{log};
                    result.loadClasses.put(entry.klassPtr, entry);
                }
                else{
                    matcher = LOAD_PATTERN_JDK10_DEBUG.matcher(log.getMessage());
                    if(matcher.matches()){
                        cachedEntry.klassPtr = Long.decode(matcher.group(2));
                        cachedEntry.loadLog[1] = log;
                        result.loadClasses.put(cachedEntry.klassPtr, cachedEntry);
                        cachedEntry = null;
                    }
                }
            }

        }

        return result;
    }

}
