package de.reinhard.merlin.persistency;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileSystemPersistency implements PersistencyInterface {
    private Logger log = LoggerFactory.getLogger(FileSystemPersistency.class);

    public Long getLastModified(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            log.error("Can't check last modification of file '" + getCanonicalPath(path) + "'. It doesn't exist.");
            return null;
        }
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            if (fileTime != null) {
                return fileTime.toMillis();
            }
        } catch (IOException ex) {
            log.error("Can't check last modification of file '" + getCanonicalPath(path) + "': " + ex.getMessage(), ex);
        }
        return null;
    }

    public Path getCanonicalPath(Path path) {
        return Paths.get(getCanonicalPathString(path));
    }

    public String getCanonicalPathString(Path path) {
        File file = path.toFile();
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            log.error("Can't get canonical path for '" + path + "': " + ex.getMessage(), ex);
            return file.getAbsolutePath();
        }
    }

    public boolean exists(Path path) {
        File file = path.toFile();
        return file.exists();
    }

    public List<Path> listFiles(Path dir, boolean recursive, String... extensions) {
        List<Path> list = new ArrayList<>();
        Collection<File> files = FileUtils.listFiles(dir.toFile(), extensions, recursive);
        files.forEach(file -> list.add(file.toPath()));
        return list;
    }

    /**
     * @param path
     * @return The file input stream.
     */
    public InputStream getInputStream(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            log.error("Can't create input stream from unexisting path '" + path.toAbsolutePath() + "'.");
            return null;
        }
        try {
            return new FileInputStream(file);
        } catch (IOException ex) {
            log.error("Can't create input stream from path '" + path.toAbsolutePath() + "': " + ex.getMessage(), ex);
            return null;
        }
    }

    public File getFile(Path path) {
        return path.toFile();
    }
}