package de.micromata.merlin.persistency;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Representing one entry of the directory for checking later changes.
 */
public class DirectoryWatcherContext {
    private long time;
    // Used for detecting deleted items.
    private Set<Path> touchedItems = new HashSet<>();

    DirectoryWatcherContext() {
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public boolean containsTouchedItem(Path path) {
        return touchedItems.contains(path);
    }

    public void add(Path path) {
        this.touchedItems.add(path);
    }
}
