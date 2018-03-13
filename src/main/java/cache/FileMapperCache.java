package cache;

import models.classes.MappedFile;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FileMapperCache {
    private static FileMapperCache gob_fileMapperCache;
    private static Map<Path, MappedFile> gob_fileMapperCacheMap;

    private FileMapperCache() {
        gob_fileMapperCacheMap = new HashMap<>();
    }

    public static FileMapperCache getFileMapperCache() {
        if (gob_fileMapperCache == null) {
            gob_fileMapperCache = new FileMapperCache();
        }

        return gob_fileMapperCache;
    }

    public void put(MappedFile iob_mappedFile) {
        gob_fileMapperCacheMap.put(iob_mappedFile.getFilePath(), iob_mappedFile);
    }

    public MappedFile get(Path iob_path) {
        return gob_fileMapperCacheMap.get(iob_path);
    }

    public void remove(Path iob_path) {
        gob_fileMapperCacheMap.remove(iob_path);
    }

    public Collection<MappedFile> getAll() {
        return gob_fileMapperCacheMap.values();
    }

    public void updateKeyAndValue(Path iob_oldPath, Path iob_newPath, MappedFile iob_mappedFile) {
        gob_fileMapperCacheMap.remove(iob_oldPath);
        iob_mappedFile.setFilePath(iob_newPath);
        gob_fileMapperCacheMap.put(iob_newPath, iob_mappedFile);
    }
}
