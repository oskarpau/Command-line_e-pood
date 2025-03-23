// käsurea argumendid: file fail1.txt echo tere kaks korda file error.txt

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client {
    String path =  "received" + File.separatorChar;

    public static void main(String[] args) throws Exception {
        List<String> argsC = Arrays.asList(args);
        Client client = new Client();

        System.out.println("connecting to server");
        try (Socket socket = new Socket("localhost", 1337);
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());) {
            System.out.println("connected; sending data");

            // loeme requestide arvu
            int numOfRequests = 0;
            for (String s : argsC) {
                if (s.equals("file") || s.equals("echo")) {
                    numOfRequests++;
                }
            }
            dout.writeInt(numOfRequests);
            System.out.println("sent number of requests: " + numOfRequests);

            for (int i = 0; i < argsC.size(); i++) {
                if (argsC.get(i).equals("file")) {
                    client.requestFile(din, dout, argsC.get(i + 1));
                    i++;
                } else if (argsC.get(i).equals("echo")) {
                    List<String> msg = new ArrayList<String>();
                    for (int i1 = i+1; i1 < argsC.size(); i1++) {
                        if (argsC.get(i1).equals("file") || argsC.get(i1).equals("echo")) {
                            i = i1 - 1;
                            break;
                        }
                        msg.add(argsC.get(i1));
                    }
                    client.sendMsg(din, dout, msg);
                }
            }
        }
        System.out.println("Client has finished");
    }

    public void sendMsg(DataInputStream din, DataOutputStream dout, List<String> msg) throws Exception {
        dout.writeInt(0);
        dout.writeInt(msg.size());
        for (String s : msg) {
            dout.writeUTF(s);
        }
        System.out.println("sent message: " + msg);
        String resp = din.readUTF();
        System.out.println("received response: " + resp);
    }

    public void requestFile(DataInputStream din, DataOutputStream dout, String failinimi) throws IOException {
        dout.writeInt(1);
        dout.writeUTF(failinimi);
        System.out.println("Asked for file: " + failinimi);
        receiveFile(din, failinimi);
    }

    public void receiveFile(DataInputStream din, String failinimi) throws IOException {
        int bytes = 0;
        int ok_status = din.readByte();
        if (ok_status == 6) {
            System.out.println("ERROR: no file found");
            return;
        } else if (ok_status == 7) {
            System.out.println("ERROR: absolute path was provided");
            return;
        } else if (ok_status == 5) {
            System.out.println("file received from server");
        } else {
            System.out.println("ERROR: unknown status " + ok_status);
            return;
        }

        // try with resources
        try (FileOutputStream fos = new FileOutputStream(path + failinimi)) {
            long size = din.readLong();
            byte[] buffer = new byte[4 * 1024];
            while (size > 0 && (bytes = din.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                fos.write(buffer, 0, bytes);
                size -= bytes;
            }
            System.out.println("file saved");
        }
    }
}