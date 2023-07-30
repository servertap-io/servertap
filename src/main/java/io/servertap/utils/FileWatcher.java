package io.servertap.utils;

import io.servertap.ServerTapMain;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class FileWatcher {
    private final ServerTapMain main;
    private final Logger log;
    private final WatchService ws;
    private final ArrayList<String> monitoredDirs;
    private final Map<String, Runnable> monitoredFiles;
    private final AtomicReference<BukkitTask> EventPollTask;

    public FileWatcher(ServerTapMain main, Logger log) {
        WatchService ws = null;
        this.main = main;
        this.log = log;
        try { ws = FileSystems.getDefault().newWatchService(); }
        catch (IOException e) {
            log.warning("[ServerTap] Your servers OS does not support our the Java Watch Service. Certain SSE and WS events may not work as a result.");
        }
        this.ws = ws;
        this.monitoredDirs = new ArrayList<>();
        this.monitoredFiles = new ConcurrentHashMap<>();
        this.EventPollTask = new AtomicReference<>();
    }

    public void start() {
        if(ws == null)
            return;
        startWatcher();
    }

    public void stop() {
        if(EventPollTask == null)
            return;
        EventPollTask.get().cancel();
    }

    public void watch(String fName, String dir, Runnable callback) {
        if(!monitoredDirs.contains(dir))
            registerDirectory(dir);
        monitoredFiles.put(fName, callback);
    }

    private void registerDirectory(String dir) {
        try {
            Paths.get(System.getProperty("user.dir").concat("/").concat(dir)).register(ws, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            log.warning("[ServerTap] FileWatcher could no register the ".concat(dir).concat("directory..."));
            return;
        }
        monitoredDirs.add(dir);
    }

    private void startWatcher() {
        AtomicReference<WatchKey> key = new AtomicReference<>();
        EventPollTask.set(Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
            try {
                key.set(ws.poll());
            } catch (IllegalStateException e) {
                log.warning("[ServerTap] FileWatcher Timed out... Some SSE & WebSocket Events may stop working!");
                EventPollTask.get().cancel();
            }

            if(key.get() == null)
                return;

            key.get().pollEvents().forEach((event) -> {
                String fileName = event.context().toString();
                if(monitoredFiles.containsKey(fileName))
                    monitoredFiles.get(fileName).run();
            });
            key.get().reset();
        }, 0, 20));
    }
}
