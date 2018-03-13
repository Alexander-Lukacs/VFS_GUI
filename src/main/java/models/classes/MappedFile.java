package models.classes;

public class MappedFile {
    private String filePath;
    private int version;
    private long lastModified;

    public MappedFile() {
    }

    public MappedFile(String filePath, int version, long lastModified) {
        this.filePath = filePath;
        this.version = version;
        this.lastModified = lastModified;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
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
