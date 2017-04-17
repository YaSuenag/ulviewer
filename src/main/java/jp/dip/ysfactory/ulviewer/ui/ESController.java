/*
 * Copyright (C) 2017 Yasumasa Suenaga
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
package jp.dip.ysfactory.ulviewer.ui;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jp.dip.ysfactory.ulviewer.ExceptionDialog;
import jp.dip.ysfactory.ulviewer.logdata.LogData;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;


public class ESController implements Initializable{

    @FXML
    private VBox rootVBox;

    @FXML
    private TextField hostName;

    @FXML
    private TextField port;

    @FXML
    private TextField timeout;

    @FXML
    private TextField bulkCount;

    @FXML
    private Button okBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    private List<LogData> logs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Do nothing
    }

    @FXML
    private void onCancelClick(ActionEvent actionEvent) {
        ((Stage)rootVBox.getScene().getWindow()).close();
    }

    private static class LogDataSerializer extends StdSerializer<LogData> {

        public LogDataSerializer(){
            this(LogData.class);
        }

        protected LogDataSerializer(Class<LogData> t) {
            super(t);
        }

        @Override
        public void serialize(LogData logData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();

            if(logData.getTime() != null){
                jsonGenerator.writeStringField("time", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(logData.getTime()));
            }
            if(logData.getUtcTime() != null){
                jsonGenerator.writeStringField("utcTime", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(logData.getUtcTime()));
            }
            if(logData.getUptime() != Double.NaN){
                jsonGenerator.writeNumberField("uptime", logData.getUptime());
            }
            if(logData.getTimeMillis() != -1){
                jsonGenerator.writeNumberField("timeMillis", logData.getTimeMillis());
            }
            if(logData.getUptimeMillis() != -1){
                jsonGenerator.writeNumberField("uptimeMillis", logData.getUptimeMillis());
            }
            if(logData.getTimeNanos() != -1){
                jsonGenerator.writeNumberField("timeNanos", logData.getTimeNanos());
            }
            if(logData.getUptimeNanos() != -1){
                jsonGenerator.writeNumberField("uptimeNanos", logData.getUptimeNanos());
            }
            if(!logData.getHostname().equals("<Unknown>")){
                jsonGenerator.writeStringField("hostname", logData.getHostname());
            }
            if(logData.getPid() != -1){
                jsonGenerator.writeNumberField("pid", logData.getPid());
            }
            if(logData.getTid() != -1){
                jsonGenerator.writeNumberField("tid", logData.getTid());
            }

            jsonGenerator.writeObjectField("level", logData.getLevel());

            if(!logData.getTags().contains("<Unknown>")) {
                jsonGenerator.writeObjectField("tags", logData.getTags());
            }

            jsonGenerator.writeObjectField("message", logData.getMessage());

            jsonGenerator.writeEndObject();
        }

    }

    private static class PushToESTask extends Task<Boolean> {

        private final int timeout;

        private final String hostName;

        private final int port;

        private final List<LogData> logs;

        private final int bulkCount;

        private final ObjectMapper mapper;

        private final Stage parentStage;

        public PushToESTask(int timeout, String hostName, int port, int bulkCount, List<LogData> logs, Stage parentStage) {
            this.timeout = timeout;
            this.hostName = hostName;
            this.port = port;
            this.bulkCount = bulkCount;
            this.logs = logs;
            this.parentStage = parentStage;

            SimpleModule module = new SimpleModule();
            module.addSerializer(LogData.class, new LogDataSerializer());
            this.mapper = new ObjectMapper();
            mapper.registerModule(module);
        }

        @Override
        protected Boolean call() throws Exception {
            JsonFactory factory = new JsonFactory();
            boolean succeeded = true;

            try(RestClient client = RestClient.builder(new HttpHost(hostName, port, "http"))
                                               .setRequestConfigCallback(b -> b.setConnectTimeout(timeout).setSocketTimeout(timeout))
                                               .setMaxRetryTimeoutMillis(timeout)
                                               .build()){
                LogData log = logs.get(0);
                String endpoint = "/jvmul";
                if(log.getTime() != null){
                    endpoint += "-" + DateTimeFormatter.ISO_LOCAL_DATE.format(log.getTime());
                }
                else if(log.getUtcTime() != null){
                    endpoint += "-" + DateTimeFormatter.ISO_LOCAL_DATE.format(log.getUtcTime());
                }
                endpoint += "/jvmul/_bulk";

                for(int start = 0; start < logs.size(); start += bulkCount){
                    StringJoiner contents = new StringJoiner("\n");
                    int last = ((start + bulkCount) > logs.size()) ? logs.size() : start + bulkCount;

                    for(LogData bulkData : logs.subList(start, last)){
                        contents.add("{ \"index\" : {} }");
                        contents.add(mapper.writeValueAsString(bulkData));
                    }

                    Response response = client.performRequest("PUT", endpoint, Collections.emptyMap(), new NStringEntity(contents.toString(), ContentType.create("application/x-ndjson")));

                    try(InputStream content = response.getEntity().getContent();
                        JsonParser responseParser = factory.createParser(content);){
                        while(responseParser.nextToken() != JsonToken.END_OBJECT){
                            String objName = responseParser.getCurrentName();
                            if((objName != null) && objName.equals("errors")){

                                if(responseParser.getValueAsBoolean()){
                                    succeeded = false;
                                }

                                break;
                            }
                        }
                    }

                    updateProgress(last - 1, logs.size());
                    updateMessage(Integer.toString(last) + " / " + Integer.toString(logs.size()));
                }

            }

            return succeeded;
        }

        @Override
        protected void succeeded() {
            Alert dialog = this.getValue() ? new Alert(Alert.AlertType.INFORMATION, "Finish", ButtonType.OK)
                                            : new Alert(Alert.AlertType.WARNING, "Part of log(s) couldn't be pushed.", ButtonType.OK);

            Platform.runLater(() -> dialog.showAndWait());
            Platform.runLater(() -> parentStage.close());
        }

        @Override
        protected void failed() {
            ExceptionDialog.showExceptionDialog(this.getException());
            Platform.runLater(() -> parentStage.close());
        }

    }

    @FXML
    private void onOkClick(ActionEvent actionEvent) {
        PushToESTask task = new PushToESTask(Integer.parseInt(timeout.getText()), hostName.getText(), Integer.parseInt(port.getText()), Integer.parseInt(bulkCount.getText()), logs, (Stage)rootVBox.getScene().getWindow());
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
        okBtn.setDisable(true);
        cancelBtn.setDisable(true);
        Thread th = new Thread(task);
        th.start();
    }

    public void setLogs(List<LogData> logs) {
        this.logs = logs;
    }

}
