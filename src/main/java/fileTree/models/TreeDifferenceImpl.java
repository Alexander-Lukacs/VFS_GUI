package fileTree.models;

import fileTree.interfaces.TreeDifference;

import java.util.ArrayList;
import java.util.Collection;

public class TreeDifferenceImpl implements TreeDifference {

    private Collection<String> filesToUpdate;
    private Collection<String> filesToDelete;
    private Collection<String> filesToInsert;


    public TreeDifferenceImpl() {
        filesToDelete = new ArrayList<>();
        filesToUpdate = new ArrayList<>();
        filesToInsert = new ArrayList<>();
    }

    @Override
    public void setFilesToUpdate(Collection<String> ico_filesToUpdate) {
        this.filesToUpdate = ico_filesToUpdate;
    }

    @Override
    public void setFilesToDelete(Collection<String> ico_filesToDelete) {
        this.filesToDelete = ico_filesToDelete;
    }

    @Override
    public void setFilesToInsert(Collection<String> ico_filesToInsert) {
        this.filesToInsert = ico_filesToInsert;
    }

    @Override
    public void addFileToUpdate(String iva_filePath) {
        this.filesToUpdate.add(iva_filePath);
    }

    @Override
    public void removeFileToUpdate(String iva_filePath) {
        this.filesToUpdate.remove(iva_filePath);
    }

    @Override
    public void addFileToDelete(String iva_filePath) {
        this.filesToDelete.add(iva_filePath);
    }

    @Override
    public void removeFileToDelete(String iva_filePath) {
        this.filesToDelete.remove(iva_filePath);
    }

    @Override
    public void addFileToInsert(String iva_filePath) {
        this.filesToInsert.add(iva_filePath);
    }

    @Override
    public void removeFileToInsert(String iva_filePath) {
        this.filesToInsert.remove(iva_filePath);
    }

    @Override
    public Collection<String> getFilesToUpdate() {
        return this.filesToUpdate;
    }

    @Override
    public Collection<String> getFilesToDelete() {
        return this.filesToDelete;
    }

    @Override
    public Collection<String> getFilesToInsert() {
        return this.filesToInsert;
    }
}
