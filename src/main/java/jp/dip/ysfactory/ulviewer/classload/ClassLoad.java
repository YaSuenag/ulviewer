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
package jp.dip.ysfactory.ulviewer.classload;

import jp.dip.ysfactory.ulviewer.logdata.LogData;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassLoad {

    private static final Pattern LOAD_PATTERN = Pattern.compile("^(\\[.+?\\])+ (\\S+) source: \\S+ klass: (0x[0-9a-f]+) .+$");

    private static final Pattern UNLOAD_PATTERN = Pattern.compile("^(\\[.+?\\])+ unloading class (\\S+) (0x[0-9a-f]+)$");

    public static class ClassLoadLogEntry{

        private LogData loadLog;

        private LogData unloadLog;

        private String className;

        private long klassPtr;

        public ClassLoadLogEntry(){
            this(null, -1);
        }

        public ClassLoadLogEntry(String className, long klassPtr){
            loadLog = null;
            unloadLog = null;
            this.className = className;
            this.klassPtr = klassPtr;
        }

        public LogData getLoadLog() {
            return loadLog;
        }

        public LogData getUnloadLog() {
            return unloadLog;
        }

        public String getClassName() {
            return className;
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

        for(LogData log : logs){

            if(!shouldProcess(log, pid, hostname)){
                continue;
            }

            if(log.getLevel().equals("debug") && log.getTags().contains("load")){
                Matcher matcher = LOAD_PATTERN.matcher(log.getMessage());

                if(matcher.matches()){
                    ClassLoadLogEntry entry = new ClassLoadLogEntry(matcher.group(2), Long.decode(matcher.group(3)));
                    entry.loadLog = log;

                    result.loadClasses.put(entry.klassPtr, entry);
                }

            }
            else if(log.getLevel().equals("info") && log.getTags().contains("unload")){
                Matcher matcher = UNLOAD_PATTERN.matcher(log.getMessage());

                if(matcher.matches()){
                    long klass = Long.decode(matcher.group(3));
                    ClassLoadLogEntry entry = result.loadClasses.computeIfAbsent(klass, k -> new ClassLoadLogEntry(matcher.group(2), klass));
                    entry.unloadLog = log;
                }

            }

        }

        return result;
    }

}
