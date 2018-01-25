package models;

import exceptions.InvalidPathException;
import interfaces.NodeInterface;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class NodeTest {

    @Test
    public void isDirectory() {
        NodeInterface node = new Node("node","Path");
        node.setDirectory(false);

        Assert.assertFalse(node.isDirectory());

        node.setDirectory(true);

        Assert.assertTrue(node.isDirectory());
    }

    @Test
    public void setPath() {
        NodeInterface node = new Node("node","Path");

        Assert.assertEquals("Path", node.getPath());

        node.setPath("Test");

        Assert.assertEquals("Test", node.getPath());
    }

    @Test(expected = InvalidPathException.class)
    public void setInvalidPath() {
        new Node("node","");
    }

    @Test
    public void addChild() {
        NodeInterface node = new Node("node","Path");
        NodeInterface child = new Node ("node","Child");
        node.addChild(child);

        Assert.assertEquals(child, node.getChild("Child"));
    }

    @Test
    public void addChildren() {
        NodeInterface node = new Node("node","Path");
        NodeInterface child1 = new Node ("node","Child1");
        NodeInterface child2 = new Node ("node","Child2");
        NodeInterface child3 = new Node ("node","Child3");

        Collection<NodeInterface> collection = new ArrayList<>();
        collection.add(child1);
        collection.add(child2);
        collection.add(child3);

        node.addChildren(collection);

        Assert.assertEquals(child1, node.getChild("Child1"));
        Assert.assertEquals(child2, node.getChild("Child2"));
        Assert.assertEquals(child3, node.getChild("Child3"));
    }

    @Test
    public void getChild() {
        NodeInterface node = new Node("node","Path", true);
        NodeInterface child1 = new Node ("node","Child1");
        NodeInterface child2;

        node.addChild(child1);

        child2 = node.getChild(child1.getPath());

        Assert.assertEquals(child1, child2);
    }

    @Test
    public void getChildren() {
    }

    @Test
    public void removeChild() {
    }

    @Test
    public void removeChildren() {
    }

    @Test
    public void removeAllChildren() {
    }

    @Test
    public void setParent() {
    }

    @Test
    public void getParent() {
    }

    @Test
    public void setNodeNotFoundExceptionStatus() {
    }


}