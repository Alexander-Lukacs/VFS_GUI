package fileTree.interfaces;

import java.util.Collection;

public interface TreeDifference {

    void setFilesToUpdate(Collection<String> ico_filesToUpdate);

    void setFilesToDelete(Collection<String> ico_filesToDelete);

    void setFilesToInsert(Collection<String> ico_filesToInsert);

    void addFileToUpdate(String iva_filePath);

    void removeFileToUpdate(String iva_filePath);

    void addFileToDelete(String iva_filePath);

    void removeFileToDelete(String iva_filePath);

    void addFileToInsert(String iva_filePath);

    void removeFileToInsert(String iva_filePath);

    Collection<String> getFilesToUpdate();

    Collection<String> getFilesToDelete();

    Collection<String> getFilesToInsert();
}