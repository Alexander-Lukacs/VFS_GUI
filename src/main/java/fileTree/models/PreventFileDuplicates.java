package fileTree.models;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PreventFileDuplicates {
    private Map<Path, FileDuplicateValue> gob_map;

    public PreventFileDuplicates() {
        gob_map = new HashMap<>();
    }

    public void putCreated(Path iob_key) {
        FileDuplicateValue lob_value = gob_map.get(iob_key);
        if (lob_value == null) {
            lob_value = new FileDuplicateValue(true, false);
            gob_map.put(iob_key, lob_value);
        } else {
            lob_value.gva_created = true;
        }
    }

    public void putDeleted(Path iob_key) {
        FileDuplicateValue lob_value = gob_map.get(iob_key);
        if (lob_value == null) {
            lob_value = new FileDuplicateValue(false, true);
            gob_map.put(iob_key, lob_value);
        } else {
            lob_value.gva_deleted = true;
        }
    }

    public void removeCreated(Path iob_key) {
        FileDuplicateValue lob_value = gob_map.get(iob_key);
        if (lob_value != null) {
            if (!lob_value.gva_deleted) {
                gob_map.remove(iob_key);
            } else {
                lob_value.gva_created = false;
            }
        }
    }

    public void removeDeleted(Path iob_key) {
        FileDuplicateValue lob_value = gob_map.get(iob_key);
        if (lob_value != null) {
            if (!lob_value.gva_created) {
                gob_map.remove(iob_key);
            } else {
                lob_value.gva_deleted = false;
            }
        }
    }

    public boolean isFileCreated(Path iob_key) {
        FileDuplicateValue lob_value = gob_map.get(iob_key);
        return lob_value != null && lob_value.gva_created;
    }

    public boolean isFileDeted(Path iob_key) {
        FileDuplicateValue lob_value = gob_map.get(iob_key);
        return lob_value != null && lob_value.gva_deleted;
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


