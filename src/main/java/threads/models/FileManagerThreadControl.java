package threads.models;

import builder.RestClientBuilder;
import fileTree.interfaces.Tree;
import fileTree.interfaces.TreeDifference;
import fileTree.models.TreeSingleton;
import javafx.application.Platform;
import restful.clients.FileRestClient;
import threads.interfaces.ThreadControl;
import tools.TreeTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static threads.constants.FileManagerConstants.*;

public class FileManagerThreadControl implements ThreadControl, Runnable {
    private volatile boolean isRunning;
    private FileRestClient gob_restClient;
    private AtomicInteger gva_commandIndex = new AtomicInteger(0);
    private volatile List<Command> gco_commands = Collections.synchronizedList(new ArrayList<Command>());

    @Override
    public void start() {
        java.lang.Thread lob_runnerThread = new java.lang.Thread(this, FileManagerThreadControl.class.getSimpleName());
        lob_runnerThread.setDaemon(true);
        gob_restClient = RestClientBuilder.buildFileRestClientWithAuth();
        isRunning = true;
        lob_runnerThread.start();
    }

    @Override
    public void stop() {
        isRunning = true;
    }

    public void addFileWithCommando(File iob_file, String iva_commando, boolean iva_executeCommandOnServer, Object... iar_fileInformation) {
        Command lob_command = new Command();
        Command lob_serverCommand;

        lob_command.gob_file = iob_file;
        lob_command.gva_command = iva_commando;
        lob_command.gva_doOnServer = false;
        lob_command.gva_commandSuccessfullOnServer = false;

        if (iar_fileInformation.length != 0) {
            lob_command.gar_fleInformations = iar_fileInformation;
        } else {
            lob_command.gar_fleInformations = null;
        }
        gco_commands.add(lob_command);

        if (iva_executeCommandOnServer && !iva_commando.equals(GC_DOWNLOAD_FROM_SERVER) && !iva_commando.equals(GC_COMPARE_TREE)) {
            lob_serverCommand = new Command();
            lob_serverCommand.gob_file = iob_file;
            lob_serverCommand.gva_command = iva_commando + "_on_server";
            lob_serverCommand.gva_doOnServer = true;
            lob_serverCommand.gar_fleInformations = iar_fileInformation;

            if (iar_fileInformation.length != 0) {
                lob_serverCommand.gar_fleInformations = iar_fileInformation;
            } else {
                lob_serverCommand.gar_fleInformations = null;
            }

            gco_commands.add(lob_serverCommand);
        }
    }

    /**
     * forward the given command to the matching method
     * @param iob_command contains all information about the command
     */
    private void executeCommand(Command iob_command) {
        switch (iob_command.gva_command) {
            case GC_ADD:
                addLocalFile(iob_command);
                break;

            case GC_UPLOAD_TO_SERVER: break;

            case GC_DELETE:
                deleteLocalFile(iob_command);
                break;

            case GC_DELETE_ON_SERVER:
                deleteFileOnServer(iob_command);
                break;

            case GC_MOVE: break;

            case GC_MOVE_ON_SERVER: break;

            case GC_DELETE_DIR_ONLY: break;

            case GC_DELETE_DIR_ONLY_ON_SERVER: break;

            case GC_RENAME: break;

            case GC_RENAME_ON_SERVER: break;

            case GC_DOWNLOAD_FROM_SERVER: break;

            case GC_COMPARE_TREE:
                compareTrees(iob_command);
                break;

            default: gco_commands.remove(iob_command);
        }
    }

    private void addFiles(TreeDifference iob_difference, Tree iob_tree, int iva_loopIndex) {
        for (String lva_addFile : iob_difference.getFilesToInsert()) {
            if (iva_loopIndex == 1) {
                lva_addFile = "Public" + lva_addFile;
            }

            File lob_newFile = gob_restClient.downloadFile(lva_addFile);
            if (lob_newFile != null) {
                //add to private directory
                TreeTool.getInstance().addToTreeView(lob_newFile);
                iob_tree.addFile(lob_newFile, lob_newFile.isDirectory());
            }
        }
    }


//----------------------------------------------------------------------------------------------------------------------
// methods to execute a command
//----------------------------------------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------------------------------------------
    // "GC_ADD"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * add the file only to the client
     * @param iob_command expected input in gar_information:
     *                    1. boolean is the file a directory
     */
    private void addLocalFile(Command iob_command) {
        boolean lva_isDirectory;
        Tree lob_tree = TreeSingleton.getInstance().getTree();
        System.out.println(iob_command.gob_file);

        if (!iob_command.gob_file.exists()) {
            if (iob_command.gar_fleInformations[0] instanceof Boolean) {
                lva_isDirectory = (Boolean) iob_command.gar_fleInformations[0];
            } else {
                // the information if it was a file or directory was not provided, therefore remove the command
                // and return
                gco_commands.remove(iob_command);
                return;
            }
        } else {
            lva_isDirectory = iob_command.gob_file.isDirectory();
        }
        //check if the file is already in the TreeView
        if (!TreeTool.isFileInTreeView(iob_command.gob_file)) {
            //Add the file to the TreeView
            if (!lob_tree.getRoot().equals(iob_command.gob_file)) {
                TreeTool.getInstance().addToTreeView(iob_command.gob_file);
            }
        }

        //add the file to the tree
        lob_tree.addFile(iob_command.gob_file, lva_isDirectory);

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_COMPARE_TREE"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * compare the local tree to the tree on the server
     * @param iob_command expected input in gar_information:
     *                    1. tree from the client
     */
    private void compareTrees(Command iob_command) {
        Tree lob_tree;
        int lva_loopIndex = 0;

        if (iob_command.gar_fleInformations == null) {
            gco_commands.remove(iob_command);
            return;
        }

        if (iob_command.gar_fleInformations[0] instanceof Tree) {
            lob_tree = (Tree) iob_command.gar_fleInformations[0];
        } else {
            gco_commands.remove(iob_command);
            return;
        }

        Collection<TreeDifference> lco_differences = gob_restClient.compareClientAndServerTree(lob_tree);
        for (TreeDifference lob_difference : lco_differences) {
            addFiles(lob_difference, lob_tree, lva_loopIndex);
            deleteFiles(lob_difference, lob_tree, lva_loopIndex);
            lva_loopIndex++;
        }

        gco_commands.remove(iob_command);
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE"
    //------------------------------------------------------------------------------------------------------------------
    /**
     * delete file on the client
     * @param iob_command expected input in gar_information:
     *                    1. tree from the client
     */
    private void deleteLocalFile(Command iob_command) {
        Tree lob_tree;
        Platform.runLater(() -> TreeTool.getInstance().deleteItem(iob_command.gob_file));

        if (iob_command.gob_file == null) {
            removeCommand(iob_command);
        }

        if (iob_command.gar_fleInformations[0] instanceof Tree) {
            lob_tree = (Tree) iob_command.gar_fleInformations[0];
        } else {
            removeCommand(iob_command);
            return;
        }

        if (lob_tree.deleteFile(iob_command.gob_file)) {
            removeCommand(iob_command);
        } else {
            gva_commandIndex.incrementAndGet();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // "GC_DELETE_ON_SERVER"
    //------------------------------------------------------------------------------------------------------------------
    private void deleteFileOnServer(Command iob_command) {
        String lva_relativePath;

        try {
            if (iob_command.gob_file == null) {
                gco_commands.remove(iob_command);
            }

            lva_relativePath = TreeTool.getRelativePath(iob_command.gob_file.getCanonicalPath());
            if (gob_restClient.deleteOnServer(lva_relativePath)) {
                gco_commands.remove(iob_command);
            } else {
                gva_commandIndex.incrementAndGet();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            gva_commandIndex.incrementAndGet();
        }
    }

//----------------------------------------------------------------------------------------------------------------------
// helper methods
//----------------------------------------------------------------------------------------------------------------------

    private void deleteFiles(TreeDifference iob_difference, Tree iob_tree, int iva_loopIndex) {
        for (String lva_deleteFile : iob_difference.getFilesToDelete()) {
            if (iva_loopIndex == 0) {
                lva_deleteFile = iob_tree.getRoot().getAbsolutePath() + "\\Private" + lva_deleteFile;
            }

            if (iva_loopIndex == 1) {
                lva_deleteFile = iob_tree.getRoot().getAbsolutePath() + "\\Public" + lva_deleteFile;
            }

            File lob_file = new File(lva_deleteFile);
            iob_tree.deleteFile(lva_deleteFile);
            TreeTool.getInstance().deleteItem(lob_file);
        }
    }

    private void removeCommand(Command iob_command) {
        gco_commands.remove(iob_command);
        gva_commandIndex.getAndDecrement();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (isRunning) {
            if (!(gva_commandIndex.get() >= gco_commands.size() || gva_commandIndex.get() < 0)) {
                Command lob_command = gco_commands.get(gva_commandIndex.get());
                executeCommand(lob_command);
            } else {
                gva_commandIndex.set(0);
                try {
                    java.lang.Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class Command {
        private Object[] gar_fleInformations;
        private File gob_file;
        private String gva_command;
        private boolean gva_doOnServer;
        private boolean gva_commandSuccessfullOnServer;
    }
}
