package fileTree.classes;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class PreventDuplicateOperation {
    private Map<Path, FileDuplicateValue> gob_map;
    private ReentrantLock gob_lock;

    PreventDuplicateOperation() {
        gob_map = new HashMap<>();
        gob_lock = new ReentrantLock();
    }

    public void clear() {gob_lock.lock();
        try {
        gob_map.clear();
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void putCreated(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value == null) {
                lob_value = new FileDuplicateValue(true, false, false, false);
                gob_map.put(iob_key, lob_value);
            } else {
                lob_value.gva_created = true;
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void putDeleted(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value == null) {
                lob_value = new FileDuplicateValue(false, true, false, false);
                gob_map.put(iob_key, lob_value);
            } else {
                lob_value.gva_deleted = true;
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void putMoved(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value == null) {
                lob_value = new FileDuplicateValue(false, false, true, false);
                gob_map.put(iob_key, lob_value);
            } else {
                lob_value.gva_moved = true;
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void putRenamed(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value == null) {
                lob_value = new FileDuplicateValue(false, false, false, true);
                gob_map.put(iob_key, lob_value);
            } else {
                lob_value.gva_renamed = true;
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void removeCreated(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value != null) {
                if (!lob_value.gva_deleted && !lob_value.gva_moved && !lob_value.gva_renamed) {
                    gob_map.remove(iob_key);
                } else {
                    lob_value.gva_created = false;
                }
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void removeDeleted(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value != null) {
                if (!lob_value.gva_created && !lob_value.gva_moved && !lob_value.gva_renamed) {
                    gob_map.remove(iob_key);
                } else {
                    lob_value.gva_deleted = false;
                }
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void removeMoved(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value != null) {
                if (!lob_value.gva_created && !lob_value.gva_deleted && !lob_value.gva_renamed) {
                    gob_map.remove(iob_key);
                } else {
                    lob_value.gva_moved = false;
                }
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public void removeRenamed(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            if (lob_value != null) {
                if (!lob_value.gva_created && !lob_value.gva_deleted && !lob_value.gva_moved) {
                    gob_map.remove(iob_key);
                } else {
                    lob_value.gva_renamed = false;
                }
            }
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public boolean wasFileCreated(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            return lob_value != null && lob_value.gva_created;
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public boolean wasFileDeted(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            return lob_value != null && lob_value.gva_deleted;
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public boolean wasFilesMoved(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            return lob_value != null && lob_value.gva_moved;
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
        }
    }

    public boolean wasFileRenamed(Path iob_key) {
        gob_lock.lock();
        try {
            FileDuplicateValue lob_value = gob_map.get(iob_key);
            return lob_value != null && lob_value.gva_renamed;
        } finally {
            if (gob_lock.isHeldByCurrentThread()) {
                gob_lock.unlock();
            }
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
        private boolean gva_moved;
        private boolean gva_renamed;

        private FileDuplicateValue(boolean iva_created, boolean iva_deleted, boolean iva_moved, boolean iva_renamed) {
            this.gva_created = iva_created;
            this.gva_deleted = iva_deleted;
            this.gva_moved = iva_moved;
            this.gva_renamed = iva_renamed;
        }
    }
}


