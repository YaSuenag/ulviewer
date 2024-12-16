UL Viewer
![CodeQL](../../workflows/CodeQL/badge.svg)
===================

UL Viewer is log parser for [JEP 158: Unified JVM Logging](http://openjdk.java.net/jeps/158) and [JEP 271: Unified GC Logging](http://openjdk.java.net/jeps/271).

# How to use

## Start application

### Linux x64

```
$ cd ulviewer-<version>-Linux-amd64/bin
$ ./ulviewer
```

### Windows x64

* Run `ulviewer.bat`
* If your machine has WSL (Windows Subsystem for Linux), you can run `ulviewer` directly.
    * `ulviewer` is shell script.

## Open log(s)

You can open several logs from [File] -> [Open Log] menu.

## Log parser wizard

After selecting logs, Log parser wizard is shown.

You can choose log decorations for parsing. [Field value] column shows the value of decoration. You can choose decoration format on [Decoration] column. [Decoration] is [ChoiceBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ChoiceBox.html), so you can choose Unified JVM Logging official log decorations.  

After that, you can push [OK] button.

## Check your logs!

Your logs are shown in main window.

* You can set filters through log decoration. You can choose decoration from [ComboBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ComboBox.html), and check on [CheckBox](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/CheckBox.html).
* If you want to search logs with some keywords, you can search it through search window.
* Copy selected log entry when you press CTRL+C.

### Drawing chart

If you want to draw chart(s), you have to add tag(s) which indicates time to `-Xlog` option as below:

* `time` (`t`)
* `utctime` (`utc`)
* `uptime` (`u`)
* `timemillis` (`tm`)
* `uptimemillis` (`um`)
* `timenanos` (`tn`)
* `uptimenanos` (`un`)

In addition, you have to add `tags` (`tg`) to log tags.

#### Java heap chart

* [Chart] -> [Memory] -> [Java heap]
* You need to open `info` level log (`gc=info`).
* You can check Java heap memory. This chart shows all STW collection (all major/minor GCs)
* You can also check specific GC events when you click the plot on the chart.

#### GC pause time chart

* [Chart] -> [Memory] -> [Pause time]
* You need to open `info` level log (`gc=info`).
* You can check GC STW time.
* You can also check specific GC events when you click the plot on the chart.

#### Metaspace chart

* [Chart] -> [Memory] -> [Metaspace]
* You need to open `info` level log (`gc+metaspace=info`).
* You can check Metaspace usage and capacity. This chart shows Metaspace GC event.

#### CodeCache chart

* [Chart] -> [Memory] -> [CodeCache]
* You need to open `debug` level log (`compilation+codecache=debug`).
* You can check CodeCache usage. This chart stacks CodeCache usage by segments (non-profiled nmethods, profiled nmethods, non-nmethods).

> [!NOTE]
> UL Viewer would use the value of free memory in `compilation+codecache=debug` - it might be different from actual free size.
> Both CodeCache Sweeper (~ JDK 19) and GC (for CodeCache: JDK 20~) use free list like CMS GC to manage reclaimed CodeBlob (for nmethod), it does not affect free memory in CodeCache. Free memory in `compilation+codecache=debug` shows unallocated memory only.

#### Class histogram

* [Chart] -> [Memory] -> [Class histogram]
* You need to open `trace` level log (`gc+classhisto*=trace`).
* You can check class histogram when Full GC is invoked.

#### VM Operations

* [Chart] -> [VM Operations]
* You need to open `debug` level log (`vmoperation=debug`).
* You can check all VM operations.

### Show data table

#### Class loading/unloading

* [Table] -> [Class loading]
* You need to open `debug` level log for class loading, and need to open `info` level log for class unloading (`class+load=debug,class+unload=info`).
* You can check class loading / unloading information.

#### Age table

* [Table] -> [Age table]
* You need to open `trace` level log (`gc+age=trace`).
* You can check age table when GC is invoked.

# How to build

You have to use JDK 17 or later.

```
$ mvn package
```

# License

The GNU Lesser General Public License, version 3.0
