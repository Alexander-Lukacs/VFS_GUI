package fileTree.models;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class PreventFileDuplicates {
    private Map<Path, FileDuplicateValue> gob_map;
    private ReentrantLock gob_lock;

    public PreventFileDuplicates() {
        gob_map = new HashMap<>();
        gob_lock = new ReentrantLock();
    }

    public void putCreated(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value == null) {
                lob_value = new FileDuplicateValue(true, false);
                gob_map.put(iob_key, lob_value);
            } else {
                lob_value.gva_created = true;
            }
        } finally {
            gob_lock.unlock();
        }
    }

    public void putDeleted(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value == null) {
                lob_value = new FileDuplicateValue(false, true);
                gob_map.put(iob_key, lob_value);
            } else {
                lob_value.gva_deleted = true;
            }
        } finally {
            gob_lock.unlock();
        }
    }

    public void removeCreated(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value != null) {
                if (!lob_value.gva_deleted) {
                    gob_map.remove(iob_key);
                } else {
                    lob_value.gva_created = false;
                }
            }
        } finally {
            gob_lock.unlock();
        }
    }

    public void removeDeleted(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value != null) {
                if (!lob_value.gva_created) {
                    gob_map.remove(iob_key);
                } else {
                    lob_value.gva_deleted = false;
                }
            }
        } finally {
            gob_lock.unlock();
        }
    }

    public boolean isFileCreated(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            //gob_lock.unlock();
            return lob_value != null && lob_value.gva_created;
        } finally {
            gob_lock.unlock();
        }
    }

    public boolean isFileDeted(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            gob_lock.unlock();
            return lob_value != null && lob_value.gva_deleted;
        } finally {
            gob_lock.unlock();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    //
    //
    //
    //------------------------------------------------------------------------------------------------------------------
    private class FileDuplicateValue {
        private boolean gva_created;
        private boolean gva_deleted;

        private FileDuplicateValue(boolean iva_created, boolean iva_deleted) {
            this.gva_created = iva_created;
            this.gva_deleted = iva_deleted;
        }
    }
}


