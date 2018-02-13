package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NotifyServerThread extends Thread {
    private static final int acceptTimeoutMillis = 500;
    private static final int notifyServerPort = 32001;

    @Override
    public void run() {
        ServerSocket lob_notifyServer;
        Socket lob_client = null;
        BufferedReader lob_inputStream = null;
        String lva_message;

        try {

            lob_notifyServer = new ServerSocket(notifyServerPort);
            lob_notifyServer.setSoTimeout(acceptTimeoutMillis);

            while (!isInterrupted()) {
                try {
                    lob_client = lob_notifyServer.accept();
                    lob_inputStream = new BufferedReader(new InputStreamReader(lob_client.getInputStream()));

                    lva_message = lob_inputStream.readLine();

                    if (lva_message.equals("was ganz tolles")) {
                        //TODO do amazing stuff
                    }

                } catch (InterruptedIOException ignored) {}

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
