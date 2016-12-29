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
package jp.dip.ysfactory.ulviewer.agetable;

import jp.dip.ysfactory.ulviewer.logdata.LogData;
import jp.dip.ysfactory.ulviewer.logdata.LogLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgeTable {

    public static int MAX_AGE = 15;

    private static final Pattern GCID_PATTERN = Pattern.compile("^(\\[.+?\\])+ (GC\\(\\d+\\)).+$");

    private static final Pattern AGE_PATTERN = Pattern.compile("^(\\[.+?\\])+ GC\\(\\d+\\) - age\\s+(\\d+):\\s+(\\d+) bytes.+$");

    private String label;

    private final long[] ageValue;

    private final List<LogData> logData;

    public AgeTable(){
        this.ageValue = new long[MAX_AGE];
        this.logData = new ArrayList<>();
    }

    public void setLabel(String label){
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public long[] getAgeValue(){
        return ageValue;
    }

    public List<LogData> getLogData(){
        return logData;
    }

    @Override
    public String toString() {
        return label;
    }

    private static boolean shouldProcess(LogData log, int pid, String hostname){
        boolean notValid = (log.getLevel() != LogLevel.trace) ||
                            (log.getTags().size() != 2) ||
                            !(log.getTags().contains("gc") && log.getTags().contains("age")) ||
                            (pid != log.getPid()) ||
                            !Optional.ofNullable(hostname)
                                     .map(h -> h.equals(log.getHostname()))
                                     .orElse(true);

        return !notValid;
    }

    public static List<AgeTable> getAgeTableList(List<LogData> logs, int pid, String hostname){
        List<AgeTable> result = new ArrayList<>();
        AgeTable current = null;

        for(LogData log : logs){

            if(!shouldProcess(log, pid, hostname)){
                current = null;
                continue;
            }

            if(current == null){
                current = new AgeTable();
                Matcher idMatcher = GCID_PATTERN.matcher(log.getMessage());
                idMatcher.matches();
                current.label = idMatcher.group(2);
                result.add(current);
            }

            current.logData.add(log);

            Matcher matcher = AGE_PATTERN.matcher(log.getMessage());
            if(matcher.matches()){
                current.ageValue[Integer.valueOf(matcher.group(2)) - 1] = Long.parseLong(matcher.group(3));
            }

        }

        return result;
    }

}
