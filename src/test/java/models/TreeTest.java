package models;

import interfaces.NodeInterface;
import interfaces.TreeInterface;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

public class TreeTest {

    private TreeInterface tree = new Tree();

    @Test
    public void clear() {
    }

    @Test
    public void addNode() {
        //first-Layer-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        NodeInterface node1 = NodeFactory.createDirectoryNode("firstDirectory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\firstDirectory");
        NodeInterface node2 = NodeFactory.createDirectoryNode("secondDirectory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\secondDirectory");
        NodeInterface node3 = NodeFactory.createDirectoryNode("thirdDirectory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\thirdDirectory");
        NodeInterface node4 = NodeFactory.createDirectoryNode("fourthDirectory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fourthDirectory");
        NodeInterface node5 = NodeFactory.createDirectoryNode("fifthDirectory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fifthDirectory");
        NodeInterface node6 = NodeFactory.createDirectoryNode("sixthFactory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\sixthDirectory");
        NodeInterface node7 = NodeFactory.createDirectoryNode("seventhDirectory", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\AnotherOne\\seventhDirectory");
        NodeInterface node8 = NodeFactory.createFileNode("firstFile", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\firstFile", 10);
        NodeInterface node9 = NodeFactory.createFileNode("firstPDF", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\firstPDF", 312);
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //second-Layer----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        NodeInterface node10 = NodeFactory.createDirectoryNode("first_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\firstDirectory\\firs_first");
        NodeInterface node11 = NodeFactory.createFileNode("first_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\firstDirectory\\firs_second", 200);
        NodeInterface node12 = NodeFactory.createDirectoryNode("first_third", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\firstDirectory\\firs_third");
        NodeInterface node13 = NodeFactory.createFileNode("second_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\secondDirectory\\second_first", 789);
        NodeInterface node14 = NodeFactory.createDirectoryNode("second_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\secondDirectory\\second_second");
        NodeInterface node15 = NodeFactory.createDirectoryNode("second_third", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\secondDirectory\\second_third");
        NodeInterface node16 = NodeFactory.createDirectoryNode("third_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\thirdDirectory\\third_first");
        NodeInterface node17 = NodeFactory.createDirectoryNode("third_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\thirdDirectory\\third_second");
        NodeInterface node18 = NodeFactory.createFileNode("third_third", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\thirdDirectory\\third_third", 7841);
        NodeInterface node19 = NodeFactory.createDirectoryNode("fourth_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fourthDirectory\\fourth_first");
        NodeInterface node20 = NodeFactory.createDirectoryNode("fourth_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fourthDirectory\\fourth_second");
        NodeInterface node21 = NodeFactory.createFileNode("fourth_fourth", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fourthDirectory\\fourth_third", 7878);
        NodeInterface node22 = NodeFactory.createFileNode("fifth_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fifthDirectory\\fifth_first", 7898);
        NodeInterface node23 = NodeFactory.createDirectoryNode("fifth_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fifthDirectory\\fifth_second");
        NodeInterface node24 = NodeFactory.createDirectoryNode("fifth_third", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\fifthDirectory\\fifth_third");
        NodeInterface node25 = NodeFactory.createDirectoryNode("sixth_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\sixthDirectory\\sixth_first");
        NodeInterface node26 = NodeFactory.createDirectoryNode("sixth_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\sixthDirectory\\sixth_second");
        NodeInterface node27 = NodeFactory.createFileNode("sixth_third", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\sixthDirectory\\sixth_third", 6788);
        NodeInterface node28 = NodeFactory.createDirectoryNode("seventh_first", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\seventh_first");
        NodeInterface node29 = NodeFactory.createFileNode("seventh_second", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\seventh_second", 9787);
        NodeInterface node30 = NodeFactory.createDirectoryNode("seventh_third", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\seventh_third");
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //first-Layer-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        tree.addNode(node1);
        tree.addNode(node2);
        tree.addNode(node3);
        tree.addNode(node4);
        tree.addNode(node5);
        tree.addNode(node6);
        tree.addNode(node7);
        tree.addNode(node8);
        tree.addNode(node9);
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //second-Layer----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        tree.addNode(node10);
        tree.addNode(node11);
        tree.addNode(node12);
        tree.addNode(node13);
        tree.addNode(node14);
        tree.addNode(node15);
        tree.addNode(node16);
        tree.addNode(node17);
        tree.addNode(node18);
        tree.addNode(node19);
        tree.addNode(node20);
        tree.addNode(node21);
        tree.addNode(node22);
        tree.addNode(node23);
        tree.addNode(node24);
        tree.addNode(node25);
        tree.addNode(node26);
        tree.addNode(node27);
        tree.addNode(node28);
        tree.addNode(node29);
        tree.addNode(node30);
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //first-Layer-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        Assert.assertEquals(node1, tree.getRoot().getChild(node1.getPath()));
        Assert.assertEquals(node2, tree.getRoot().getChild(node2.getPath()));
        Assert.assertEquals(node3, tree.getRoot().getChild(node3.getPath()));
        Assert.assertEquals(node4, tree.getRoot().getChild(node4.getPath()));
        Assert.assertEquals(node5, tree.getRoot().getChild(node5.getPath()));
        Assert.assertEquals(node6, tree.getRoot().getChild(node6.getPath()));
        Assert.assertEquals(node8, tree.getRoot().getChild(node8.getPath()));
        Assert.assertEquals(node9, tree.getRoot().getChild(node9.getPath()));
        Assert.assertEquals("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic", tree.getRoot().getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic").getPath());
        Assert.assertEquals("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\AnotherOne", tree.getRoot().getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic").getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\AnotherOne").getPath());
        Assert.assertEquals(node7, tree.getRoot().getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic").getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic\\AnotherOne").getChild(node7.getPath()));
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

        //second-Layer----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        Assert.assertEquals(node10, tree.getRoot().getChild(node1.getPath()).getChild(node10.getPath()));
        Assert.assertEquals(node11, tree.getRoot().getChild(node1.getPath()).getChild(node11.getPath()));
        Assert.assertEquals(node12, tree.getRoot().getChild(node1.getPath()).getChild(node12.getPath()));

        Assert.assertEquals(node13, tree.getRoot().getChild(node2.getPath()).getChild(node13.getPath()));
        Assert.assertEquals(node14, tree.getRoot().getChild(node2.getPath()).getChild(node14.getPath()));
        Assert.assertEquals(node15, tree.getRoot().getChild(node2.getPath()).getChild(node15.getPath()));

        Assert.assertEquals(node16, tree.getRoot().getChild(node3.getPath()).getChild(node16.getPath()));
        Assert.assertEquals(node17, tree.getRoot().getChild(node3.getPath()).getChild(node17.getPath()));
        Assert.assertEquals(node18, tree.getRoot().getChild(node3.getPath()).getChild(node18.getPath()));

        Assert.assertEquals(node19, tree.getRoot().getChild(node4.getPath()).getChild(node19.getPath()));
        Assert.assertEquals(node20, tree.getRoot().getChild(node4.getPath()).getChild(node20.getPath()));
        Assert.assertEquals(node21, tree.getRoot().getChild(node4.getPath()).getChild(node21.getPath()));

        Assert.assertEquals(node22, tree.getRoot().getChild(node5.getPath()).getChild(node22.getPath()));
        Assert.assertEquals(node23, tree.getRoot().getChild(node5.getPath()).getChild(node23.getPath()));
        Assert.assertEquals(node24, tree.getRoot().getChild(node5.getPath()).getChild(node24.getPath()));

        Assert.assertEquals(node25, tree.getRoot().getChild(node6.getPath()).getChild(node25.getPath()));
        Assert.assertEquals(node26, tree.getRoot().getChild(node6.getPath()).getChild(node26.getPath()));
        Assert.assertEquals(node27, tree.getRoot().getChild(node6.getPath()).getChild(node27.getPath()));

        Assert.assertEquals(node28, tree.getRoot().getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic").getChild(node28.getPath()));
        Assert.assertEquals(node29, tree.getRoot().getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic").getChild(node29.getPath()));
        Assert.assertEquals(node30, tree.getRoot().getChild("C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Automatic").getChild(node30.getPath()));
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    }

    @Test
    public void compareTrees() {
        TreeInterface tree = new Tree();
        TreeInterface tree2 = new Tree();
        TreeDifference difference = new TreeDifference();

        NodeInterface node1 = new Node("node1", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\TextFile.txt", false, 2);
        NodeInterface node2 = new Node("node2", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Directory", true, 0);
        NodeInterface node3 = new Node("node3", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Directory\\AnotherTextFile", false, 100);
        NodeInterface node4 = new Node("node4", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Dir", true, 0);
        NodeInterface node5 = new Node("node5", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Dir\\text.txt", false, 30);
        NodeInterface node6 = new Node("node6", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\testText.txt", false, 19);
        NodeInterface node7 = new Node("node7", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\testPDF.pdf", false, 1367);
        NodeInterface node8 = new Node("node8", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Directory\\DIR", true, 0);
        NodeInterface node9 = new Node("node9", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\DynamicDirectory\\AntoherOne\\Test.txt");
        tree.addNode(node1);
        tree.addNode(node2);
        tree.addNode(node3);
        tree.addNode(node4);
        tree.addNode(node5);
        tree.addNode(node6);
        tree.addNode(node7);
        tree.addNode(node8);
        tree.addNode(node9);

        NodeInterface node11 = new Node("ndoe11", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\TextFile.txt", false, 3);
        NodeInterface node12 = new Node("node12", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Directory", true, 0);
        NodeInterface node13 = new Node("node13", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Directory\\RenamedTextFile", false, 100);
        NodeInterface node14 = new Node("node14", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\Dir");
        NodeInterface node15 = new Node("node15", "C:\\Users\\Darth-Vader\\Documents\\Fileserver\\root\\create.txt");

        tree2.addNode(node11);
        tree2.addNode(node12);
        tree2.addNode(node13);
        tree2.addNode(node14);
        tree2.addNode(node15);


        difference = tree.compareTrees(tree2);
    }

    @Test
    public void getNode() {
    }

    @Test
    public void getNode1() {
    }

    @Test
    public void getRoot() {
    }

    @Test
    public void getRootSubNodes() {
    }

    @Test
    public void getNodesByKey() {
    }

    @Test
    public void getNodesByPath() {
    }

    @Test
    public void getAllFiles() {
    }

    @Test
    public void gettAllDirectories() {
    }

    @Test
    public void gettAll() {
    }

    @Test
    public void removeNode() {
    }

    @Test
    public void removeNode1() {
    }

    @Test
    public void removeNode2() {
    }

    @Test
    public void removeNodes() {
    }

    @Test
    public void removeNodesByKey() {
    }

    @Test
    public void removeNodesByPath() {
    }

    @Test
    public void toList() {
    }

    @Test
    public void toArray() {
    }

    @Test
    public void setNodeNotFoundExceptionStatus() {
    }

    @Test
    public void isExceptionActive() {
    }
}