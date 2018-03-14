package models.classes;

public class DownloadedContent {
    private boolean gva_isDirectory;
    private byte[] gar_contentAsByteArray;
    private int gva_version;

    public DownloadedContent(byte[] iar_contentAsByteArray, boolean iva_isDirectory, int iva_version) {
        this.gar_contentAsByteArray = iar_contentAsByteArray;
        this.gva_isDirectory = iva_isDirectory;
        this.gva_version = iva_version;
    }

    public byte[] getFileContent() {
        return this.gar_contentAsByteArray;
    }

    public boolean isDirectory() {
        return gva_isDirectory;
    }

    public int getVersion() {
        return gva_version;
    }
}