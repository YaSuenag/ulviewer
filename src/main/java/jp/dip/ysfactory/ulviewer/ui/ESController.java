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
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;


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

    private static XContentBuilder buildContent(LogData logData) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                                                 .startObject();
        if(logData.getTime() != null) {
            builder = builder.field("time", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(logData.getTime()));
        }
        if(logData.getUtcTime() != null){
            builder = builder.field("utcTime", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(logData.getUtcTime()));
        }
        if(logData.getUptime() != Double.NaN){
            builder = builder.field("uptime", logData.getUptime());
        }
        if(logData.getTimeMillis() != -1){
            builder = builder.field("timeMillis", logData.getTimeMillis());
        }
        if(logData.getUptimeMillis() != -1){
            builder = builder.field("uptimeMillis", logData.getUptimeMillis());
        }
        if(logData.getTimeNanos() != -1){
            builder = builder.field("timeNanos", logData.getTimeNanos());
        }
        if(logData.getUptimeNanos() != -1){
            builder = builder.field("uptimeNanos", logData.getUptimeNanos());
        }
        if(!logData.getHostname().equals("<Unknown>")){
            builder = builder.field("hostname", logData.getHostname());
        }
        if(logData.getPid() != -1){
            builder = builder.field("pid", logData.getPid());
        }
        if(logData.getTid() != -1){
            builder = builder.field("tid", logData.getTid());
        }

        builder = builder.field("level", logData.getLevel());

        if(!logData.getTags().contains("<Unknown>")) {
            builder = builder.field("tags", logData.getTags());
        }

        builder = builder.field("message", logData.getMessage());

        return builder.endObject();
    }

    private static class PushToESTask extends Task<Void> implements BulkProcessor.Listener{

        private final int timeout;

        private final String hostName;

        private final int port;

        private final List<LogData> logs;

        private final int bulkCount;

        private final Stage parentStage;

        public PushToESTask(int timeout, String hostName, int port, int bulkCount, List<LogData> logs, Stage parentStage) {
            this.timeout = timeout;
            this.hostName = hostName;
            this.port = port;
            this.bulkCount = bulkCount;
            this.logs = logs;
            this.parentStage = parentStage;
        }

        @Override
        public void beforeBulk(long executionId, BulkRequest bulkRequest) {
            // Do nothing.
        }

        @Override
        public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
            // Do noting
        }

        @Override
        public void afterBulk(long executionId, BulkRequest bulkRequest, Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        @Override
        protected Void call() throws Exception {
            int performed = 0;

            try(RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(hostName, port, "http"))
                                                                                .setRequestConfigCallback(b -> b.setConnectTimeout(timeout).setSocketTimeout(timeout))
                                                                                .setMaxRetryTimeoutMillis(timeout));
                BulkProcessor processor = BulkProcessor.builder(client::bulkAsync, this)
                                                       .setBulkActions(bulkCount)
                                                       .build()){

                for(LogData log : logs){
                    String index = "jvmul";
                    if(log.getTime() != null){
                        index += "-" + DateTimeFormatter.ISO_LOCAL_DATE.format(log.getTime());
                    }
                    else if(log.getUtcTime() != null){
                        index += "-" + DateTimeFormatter.ISO_LOCAL_DATE.format(log.getUtcTime());
                    }

                    processor.add(new IndexRequest(index, "jvmul").source(buildContent(log)));
                    updateProgress(++performed, logs.size());
                    updateMessage(Integer.toString(performed) + " / " + Integer.toString(logs.size()));
                }

            }

            return null;
        }

        @Override
        protected void succeeded() {
            Alert dialog =new Alert(Alert.AlertType.INFORMATION, "Finish", ButtonType.OK);

            Platform.runLater(dialog::showAndWait);
            Platform.runLater(parentStage::close);
        }

        @Override
        protected void failed() {
            ExceptionDialog.showExceptionDialog(this.getException());
            Platform.runLater(parentStage::close);
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
