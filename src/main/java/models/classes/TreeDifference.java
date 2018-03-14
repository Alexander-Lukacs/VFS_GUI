package models.classes;

import java.util.ArrayList;
import java.util.Collection;

public class TreeDifference {

    private Collection<String> filesToUpdate;
    private Collection<String> filesToDelete;
    private Collection<String> filesToInsert;


    public TreeDifference() {
        filesToDelete = new ArrayList<>();
        filesToUpdate = new ArrayList<>();
        filesToInsert = new ArrayList<>();
    }

    public void setFilesToUpdate(Collection<String> ico_filesToUpdate) {
        this.filesToUpdate = ico_filesToUpdate;
    }

    public void setFilesToDelete(Collection<String> ico_filesToDelete) {
        this.filesToDelete = ico_filesToDelete;
    }

    public void setFilesToInsert(Collection<String> ico_filesToInsert) {
        this.filesToInsert = ico_filesToInsert;
    }

    public void addFileToUpdate(String iva_filePath) {
        this.filesToUpdate.add(iva_filePath);
    }

    public void removeFileToUpdate(String iva_filePath) {
        this.filesToUpdate.remove(iva_filePath);
    }

    public void addFileToDelete(String iva_filePath) {
        this.filesToDelete.add(iva_filePath);
    }

    public void removeFileToDelete(String iva_filePath) {
        this.filesToDelete.remove(iva_filePath);
    }

    public void addFileToInsert(String iva_filePath) {
        this.filesToInsert.add(iva_filePath);
    }

    public void removeFileToInsert(String iva_filePath) {
        this.filesToInsert.remove(iva_filePath);
    }

    public Collection<String> getFilesToUpdate() {
        return this.filesToUpdate;
    }

    public Collection<String> getFilesToDelete() {
        return this.filesToDelete;
    }

    public Collection<String> getFilesToInsert() {
        return this.filesToInsert;
    }
}
