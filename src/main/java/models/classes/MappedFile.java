package models.classes;

import java.nio.file.Path;

public class MappedFile {
    private Path filePath;
    private int version;
    private long lastModified;

    public MappedFile() {
    }

    public MappedFile(Path filePath, int version, long lastModified) {
        this.filePath = filePath;
        this.version = version;
        this.lastModified = lastModified;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "MappedFile{" +
                "filePath='" + filePath + '\'' +
                ", version=" + version +
                ", lastModified=" + lastModified +
                '}';
    }
}
