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

public class FileEventListener {
    private final ServerTapMain main;
    private final Logger log;
    private final WatchService ws;
    private final ArrayList<String> monitoredDirs;
    private final Map<String, Runnable> monitoredFiles;
    private final AtomicReference<BukkitTask> EventPollTask;

    public FileEventListener(ServerTapMain main, Logger log) {
        WatchService ws = null;
        this.main = main;
        this.log = log;
        try { ws = FileSystems.getDefault().newWatchService(); }
        catch (IOException e) {
            log.warning("[Servertap] Your servers os does not support file watches. Certain SSE and WS events may not work as a result.");
        }
        this.ws = ws;
        this.monitoredDirs = new ArrayList<>();
        this.monitoredFiles = new ConcurrentHashMap<>();
        this.EventPollTask = null;
    }

    public void start() {
        if(ws == null)
            return;
        startListener();
    }

    public void stop() {
        if(EventPollTask == null)
            return;
        EventPollTask.get().cancel();
    }

    public void addListener(String fName, String dir, Runnable callback) {
        if(!monitoredDirs.contains(dir))
            addDir(dir);
        monitoredFiles.put(fName, callback);
    }

    private void addDir(String dir) {
        try {
            Paths.get(dir).register(ws, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            log.warning("[Servertap] Error adding file event listeners");
            return;
        }
        monitoredDirs.add(dir);
    }

    private void startListener() {
        AtomicReference<WatchKey> key = null;
        EventPollTask.set(Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
            try {
                key.set(ws.poll());
            } catch (IllegalStateException e) {
                log.warning("[Servertap] File change listener Timeout. Please restart your server...");
                EventPollTask.get().cancel();
            }
            key.get().pollEvents().forEach((event) -> {
                if(monitoredFiles.containsKey(event.context()))
                    monitoredFiles.get(event.context()).run();
            });
            key.get().reset();
        }, 0, 20));
    }
}
