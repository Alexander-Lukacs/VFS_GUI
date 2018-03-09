package threads.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NotifyServerThread extends Thread {
    private static final int GC_ACCEPT_TIMEOUT_MILLIS = 500;
    private static final int GC_NOTIFY_SERVER_PORT = 32001;

    private static final String GC_MESSAGE_MOVE = "move";
    private static final String GC_MESSAGE_ADD = "add";
    private static final String GC_MESSAGE_DELETE = "delete";
    private static final String GC_MESSAGE_RENAME = "rename";

    @Override
    public void run() {
        ServerSocket lob_notifyServer;
        Socket lob_client = null;
        BufferedReader lob_inputStream = null;
        String lva_message;
        String[] lar_messageArray;

        try {

            lob_notifyServer = new ServerSocket(GC_NOTIFY_SERVER_PORT);
            lob_notifyServer.setSoTimeout(GC_ACCEPT_TIMEOUT_MILLIS);

            while (!isInterrupted()) {
                try {
                    lob_client = lob_notifyServer.accept();
                    lob_inputStream = new BufferedReader(new InputStreamReader(lob_client.getInputStream()));

                    lva_message = lob_inputStream.readLine();
                    lar_messageArray = lva_message.split("_");

                    if (lar_messageArray[0].equals(GC_MESSAGE_MOVE)) {
                        //TODO move file
                    }

                    if (lar_messageArray[0].equals(GC_MESSAGE_ADD)) {
                        //TODO add new file
                    }

                    if (lar_messageArray[0].equals(GC_MESSAGE_DELETE)) {
                        //TODO delete file
                    }

                    if (lar_messageArray[0].equals(GC_MESSAGE_RENAME)) {
                        //TODO rename file
                    }

                } catch (InterruptedIOException ignore) {
                }

                if (lob_client != null) {
                    lob_client.close();
                }

                if (lob_inputStream != null) {
                    lob_inputStream.close();
                }
            }

            lob_notifyServer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}