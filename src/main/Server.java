import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    String path = "serverFiles" + File.separatorChar;
    static final byte TYPE_OK = 5;
    static final byte TYPE_ERROR_NOT_FOUND = 6;
    static final byte TYPE_ERROR_ABS_PATH = 7;

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newCachedThreadPool()) {
            Server server = new Server();

            try (ServerSocket ss = new ServerSocket(1337)) {
                System.out.println("now listening on :1337");
                int n = 0;
                // päriselt käiks siia while True, praegu panin siia arvu, et saaks näha serveri sulgumist
                while (n < 2) {
                    Socket clientsocket = ss.accept();
                    pool.execute(new ClientHandler(clientsocket, server));
                    n++;
                }
            }
        }

        System.out.println("Server pandi kinni, sest serveriga oli üle kahe korra ühendatud");
    }

    public static class ClientHandler implements Runnable {
        private final Socket socket;
        private final Server server;

        public ClientHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
        }

        public void run() {
            try (socket;
                 DataInputStream din = new DataInputStream(socket.getInputStream());
                 DataOutputStream dout = new DataOutputStream(socket.getOutputStream());) {
                System.out.println("client connected; waiting for a number of messages");
                int numOfRequests = din.readInt();
                for (int i = 0; i < numOfRequests; i++) {
                    int response = din.readInt();
                    if (response == 1) {
                        server.sendFile(din, dout);
                    } else if (response == 0) {
                        server.echoText(din, dout);
                        System.out.println("Message echoed");
                    }
                }
                System.out.println("Closed connection with client");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void echoText(DataInputStream din, DataOutputStream dout) throws IOException {
        int messageLength = din.readInt();
        String msg = "";
        for (int i = 0; i < messageLength; i++) {
            msg += din.readUTF() + " ";
        }
        System.out.println("received message: " + msg);
        dout.writeUTF(msg);
    }

    public void sendFile(DataInputStream din, DataOutputStream dout) throws IOException {
        int bytes = 0;
        String failinimi = path + din.readUTF();
        Path pathFile = Paths.get(failinimi);
        System.out.print("Sending of file status: ");
        if (!Files.exists(pathFile)) {
            dout.writeByte(TYPE_ERROR_NOT_FOUND);
            System.out.println("TYPE_ERROR_NOT_FOUND");
            return;
        } else if (pathFile.isAbsolute()) {
            dout.writeByte(TYPE_ERROR_ABS_PATH);
            System.out.println("TYPE_ERROR_ABS_PATH");
            return;
        } else { // eeldame, et rohkem erroreid pole
            dout.writeByte(TYPE_OK);
            System.out.println("TYPE_OK");
        }
        File file = new File(failinimi);

        try(FileInputStream fis = new FileInputStream(file)) {
            dout.writeLong(file.length());
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fis.read(buffer)) != -1) {
                dout.write(buffer, 0, bytes);
                dout.flush();
            }
            System.out.println("File sent");
        }
    }

}

