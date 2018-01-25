package models;

import interfaces.NodeInterface;

import java.util.ArrayList;
import java.util.Collection;

public class TreeDifference {
    private Collection<NodeInterface> gco_nodesToUpdate;
    private Collection<NodeInterface> gco_nodesToDelete;
    private Collection<NodeInterface> gco_nodesToInsert;

    public TreeDifference() {
        this.gco_nodesToDelete = new ArrayList<>();
        this.gco_nodesToInsert = new ArrayList<>();
        this.gco_nodesToUpdate = new ArrayList<>();
    }

    public void setNodesToUpdate(Collection<NodeInterface> ico_nodesToUpdate) {
        this.gco_nodesToUpdate = ico_nodesToUpdate;
    }

    public void setNodesToDelete(Collection<NodeInterface> ico_nodesToDelete) {
        this.gco_nodesToDelete = ico_nodesToDelete;
    }

    public void setNodesToInsert(Collection<NodeInterface> ico_nodesToInsert) {
        this.gco_nodesToInsert = ico_nodesToInsert;
    }

    public Collection<NodeInterface> getNodesToUpdate() {
        return this.gco_nodesToUpdate;
    }

    public Collection<NodeInterface> getNodesToDelete() {
        return this.gco_nodesToDelete;
    }

    public Collection<NodeInterface> getNodesToInsert() {
        return this.gco_nodesToInsert;
    }
}
