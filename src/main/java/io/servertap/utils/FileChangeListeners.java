package io.servertap.utils;

import io.servertap.custom.events.OperatorListUpdatedEvent;
import io.servertap.custom.events.WhitelistUpdatedEvent;
import org.bukkit.Bukkit;

import java.io.File;

public class FileChangeListeners {
    public FileChangeListeners() {
        initFileListeners();
    }

    private void initFileListeners() {
        addListener("../whitelist.json", (FileChnageEvent event) -> Bukkit.getPluginManager().callEvent(new WhitelistUpdatedEvent()));
        addListener("../ops.json", (FileChnageEvent event) -> Bukkit.getPluginManager().callEvent(new OperatorListUpdatedEvent()));
    }

    private void addListener(String fName, Consumer<FileChnageEvent> callback) {
        new File(fName).addFileChangeListener(new FileChangeListener() {
            @Override
            public void fileChanged(FileChangeEvent event) {
                callback.accept(event);
            }
        });
    }
}
