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
package jp.dip.ysfactory.ulviewer.classhisto;

import javafx.scene.paint.Color;
import jp.dip.ysfactory.ulviewer.logdata.LogData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassHistogram {

    public static class HistoDataEntry{

        private Color color;

        private final int num;

        private final long instances;

        private final long bytes;

        private final String name;

        private final String message;

        public HistoDataEntry(int num, long instances, long bytes, String name, String message) {
            this.color = Color.TRANSPARENT;
            this.num = num;
            this.instances = instances;
            this.bytes = bytes;
            this.name = name;
            this.message = message;
        }

        public void setColor(Color color){
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public int getNum() {
            return num;
        }

        public long getInstances(){
            return instances;
        }

        public long getBytes() {
            return bytes;
        }

        public String getName() {
            return name;
        }

        public String getMessage() {
            return message;
        }

    }

    private static final Pattern HEADER_PATTERN = Pattern.compile("^.+?Class Histogram \\((.+)\\)$");

    private static final Pattern CLASSHISTO_PATTERN = Pattern.compile("^(\\[.+?\\])+ GC\\((\\d+)\\)\\s+(\\d+):\\s+(\\d+)\\s+(\\d+)\\s+(.+)$");

    private final List<HistoDataEntry> entries;

    private String label;

    private String logHeader;

    private String logFooter;

    public ClassHistogram(String label){
        this.entries = new ArrayList<>();
        this.label = label;
        this.logHeader = null;
        this.logFooter = null;
    }

    public List<HistoDataEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString(){
        return label;
    }

    private static boolean shouldProcess(LogData log, int pid, String hostname){
        boolean notValid = !(log.getTags().contains("gc") && log.getTags().contains("classhisto")) ||
                            (pid != log.getPid()) ||
                            !Optional.ofNullable(hostname)
                                     .map(h -> h.equals(log.getHostname()))
                                     .orElse(true);

        return !notValid;
    }

    public static List<ClassHistogram> getClassHistogramList(List<LogData> logs, int pid, String hostname){
        List<ClassHistogram> result = new ArrayList<>();
        ClassHistogram current = null;

        for(int Cnt = 0; Cnt < logs.size(); Cnt++){
            LogData log = logs.get(Cnt);

            if(!shouldProcess(log, pid, hostname)){
                continue;
            }

            if(log.getTags().contains("start")){
                StringJoiner header = new StringJoiner("\n");
                Matcher matcher = HEADER_PATTERN.matcher(log.getMessage());
                matcher.matches();
                String str = matcher.group(1);

                do{
                    header.add(log.getMessage());
                    log = logs.get(++Cnt);
                    matcher = CLASSHISTO_PATTERN.matcher(log.getMessage());
                }while(!matcher.matches());

                current = new ClassHistogram("GC ID: " + matcher.group(2) + " (" + str + ")");
                current.logHeader = header.toString();
                result.add(current);
                current.entries.add(new HistoDataEntry(Integer.parseInt(matcher.group(3)), Long.parseLong(matcher.group(4)), Long.parseLong(matcher.group(5)), matcher.group(6), log.getMessage()));
            }
            else if(current != null){
                Matcher matcher = CLASSHISTO_PATTERN.matcher(log.getMessage());

                if(matcher.matches()){
                    current.entries.add(new HistoDataEntry(Integer.parseInt(matcher.group(3)), Long.parseLong(matcher.group(4)), Long.parseLong(matcher.group(5)), matcher.group(6), log.getMessage()));
                }
                else{
                    current.logFooter = log.getMessage();
                    current = null;
                }

            }

        }

        return result;
    }

    public String getLogHeader() {
        return logHeader;
    }

    public String getLogFooter() {
        return logFooter;
    }

}
