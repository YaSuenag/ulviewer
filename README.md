# UL Viewer

UL Viewer is log parser for [JEP 158: Unified JVM Logging](http://openjdk.java.net/jeps/158) and [JEP 271: Unified GC Logging](http://openjdk.java.net/jeps/271).

## How to use

You have to run `ulviewer-<version>.jar` on Java 8 or later.

**You have to use JavaFX 8 runtime.** OracleJDK 8 includes it. However if you use OpenJDK, you need to use OpenJFX runtime.


```
$ java -jar ulviewer-<version>.jar
```

### Open log(s)

You can open several logs from [File] -> [Open Log] menu.

### Log parser wizard

After selecting logs, Log parser wizard is shown.

You can choose log decorations for parsing. [Field value] column shows the value of decoration. You can choose decoration format on [Decoration] column. [Decoration] is [ChoiceBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ChoiceBox.html), so you can choose Unified JVM Logging official log decorations.  

After that, you can push [OK] button.

### Check your logs!

Your logs are shown in main window.

You can set filters through log decoration. You can choose decoration from [ComboBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ComboBox.html), and check on [CheckBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/CheckBox.html).

#### Drawing chart

##### Java heap chart

* [Chart] -> [Memory] -> [Java heap]
* You can check Java heap memory. This chart shows all STW collection (all major/minor GCs)
* You can also check specific GC events when you click the plot on the chart.

##### GC pause time chart

* [Chart] -> [Memory] -> [Pause time]
* You can check GC STW time.
* You can also check specific GC events when you click the plot on the chart.

##### Metaspace chart

* [Chart] -> [Memory] -> [Metaspace]
* You can check Metaspace usage and capacity. This chart shows Metaspace GC event.

##### Class histogram

* [Chart] -> [Memory] -> [Class histogram]
* You can check class histogram when Full GC is invoked.

##### VM Operations

* [Chart] -> [VM Operations]
* You can check all VM operations.

## How to build

You have to use JDK 8 and JavaFX 8.

```
$ mvn package
```

## License

The GNU Lesser General Public License, version 3.0