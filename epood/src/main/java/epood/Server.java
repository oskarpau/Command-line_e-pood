package epood;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                // päriselt käiks siia while True, praegu panin siia arvu, et saaks näha serveri sulgumist
                while (true) {
                    Socket clientsocket = ss.accept();
                    pool.execute(new ClientHandler(clientsocket, server));
                }
            }
        }

        //System.out.println("Server pandi kinni, sest serveriga oli üle kahe korra ühendatud");
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

