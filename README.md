# UL Viewer

UL Viewer is log parser for [JEP 158: Unified JVM Logging](http://openjdk.java.net/jeps/158) and [JEP 271: Unified GC Logging](http://openjdk.java.net/jeps/271).

## How to use

### Start application

#### Linux x64

```
$ cd ulviewer-<version>-Linux-amd64/bin
$ ./ulviewer
```

### Open log(s)

You can open several logs from [File] -> [Open Log] menu.

### Log parser wizard

After selecting logs, Log parser wizard is shown.

You can choose log decorations for parsing. [Field value] column shows the value of decoration. You can choose decoration format on [Decoration] column. [Decoration] is [ChoiceBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ChoiceBox.html), so you can choose Unified JVM Logging official log decorations.  

After that, you can push [OK] button.

### Check your logs!

Your logs are shown in main window.

* You can set filters through log decoration. You can choose decoration from [ComboBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ComboBox.html), and check on [CheckBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/CheckBox.html).
* If you want to search logs with some keywords, you can search it through search window.

#### Drawing chart

If you want to draw chart(s), you have to add tag(s) which indicates time to `-Xlog` option as below:

* `time` (`t`)
* `utctime` (`utc`)
* `uptime` (`u`)
* `timemillis` (`tm`)
* `uptimemillis` (`um`)
* `timenanos` (`tn`)
* `uptimenanos` (`un`)

In addition, you have to add `tags` (`tg`) to log tags.

##### Java heap chart

* [Chart] -> [Memory] -> [Java heap]
* You need to open `info` level log (`gc=info`).
* You can check Java heap memory. This chart shows all STW collection (all major/minor GCs)
* You can also check specific GC events when you click the plot on the chart.

##### GC pause time chart

* [Chart] -> [Memory] -> [Pause time]
* You need to open `info` level log (`gc=info`).
* You can check GC STW time.
* You can also check specific GC events when you click the plot on the chart.

##### Metaspace chart

* [Chart] -> [Memory] -> [Metaspace]
* You need to open `info` level log (`gc+metaspace=info`).
* You can check Metaspace usage and capacity. This chart shows Metaspace GC event.

##### Class histogram

* [Chart] -> [Memory] -> [Class histogram]
* You need to open `trace` level log (`gc+classhisto*=trace`).
* You can check class histogram when Full GC is invoked.

##### VM Operations

* [Chart] -> [VM Operations]
* You need to open `debug` level log (`vmoperation=debug`).
* You can check all VM operations.

#### Show data table

##### Class loading/unloading

* [Table] -> [Class loading]
* You need to open `debug` level log for class loading, and need to open `info` level log for class unloading (`class+load=debug,class+unload=info`).
* You can check class loading / unloading information.

##### Age table

* [Table] -> [Age table]
* You need to open `trace` level log (`gc+age=trace`).
* You can check age table when GC is invoked.

### Push log(s) to others

#### Push to Elasticsearch

You can push your log(s) to [Elasticsearch](https://www.elastic.co/products/elasticsearch).

1. [File] -> [Push logs] -> [Elasticsearch]
2. Set parameters:
    * Host
        * Hostname of Elasticsearch
        * `localhost` is by default.
    * Port
        * HTTP port of Elasticsearch
        * `9200` is by default.
    * Timeout
        * Timeout of HTTP access in ms
        * `5000` is by default.
    * Bulk count
        * Number of logs per bulk request
        * `1000` is by default.
3. Push [OK] button

#### Kibana support

You can use `kibana/export.json` to show your log(s) in [Kibana](https://www.elastic.co/products/kibana).
You need to import this file in Management menu of Kibana.

`export.json` provides one dashboards and four visualizes. They shows GC and Safepoint information.
Also you have to setup Elasticsearch and Kibana as below if you want to use sample dashboard.

##### Sample dashboard requirements

###### Enable regex on Painless Script

You have to set `true` to `script.painless.regex.enabled` in `elasticsearch.yml` .

###### Add Script Field to Kibana

Sample dashboard uses some Script Fields which scrapes actual value. So you have to define them before you check it.

* GCTime
    * Type
        * number
    * Format
        * Duration
    * Input Format
        * Milliseconds
    * Output Format
        * Milliseconds
    * Decimal Places
        * 2
    * Script

```
def msg = doc['message.keyword'].value;
if(msg == null){
  return null;
}
def m = /^.+ Pause .+ ([0-9]+\.[0-9]+)ms$/.matcher(msg);
if(m.matches()){
  return Float.parseFloat(m.group(1));
}else{
  return null;
}
```

* JavaHeapUsage
    * Type
        * number
    * Format
        * Bytes
    * Script

```
def msg = doc['message.keyword'].value;
if(msg == null){
  return null;
}
def m = /^.+ Pause .+->([0-9]+)M.+$/.matcher(msg);
if(m.matches()){
  return Integer.parseInt(m.group(1)) * 1024 * 1024;
}else{
  return null;
}
```

* GCCause
    * Type
        * string
    * Script

```
def msg = doc['message.keyword'].value;
if(msg == null){
  return null;
}
def m = /^.+ Pause .+ \((.+)\)$/.matcher(msg);
if(m.matches()){
  return m.group(1);
}else{
  return null;
}
```

* Safepoint
    * Type
        * string
    * Script

```
def msg = doc['message.keyword'].value;
if(msg == null){
  return null;
}
def m = /^.+: (.+)$/.matcher(msg);
if(m.matches()){
  return m.group(1);
}else{
  return null;
}
```

## How to build

You have to use JDK 8 and JavaFX 8.

```
$ mvn package
```

## License

The GNU Lesser General Public License, version 3.0
