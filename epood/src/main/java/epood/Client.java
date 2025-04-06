package epood;

// käsurea argumendid: file fail1.txt echo tere kaks korda file error.txt

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {

        System.out.println("connecting to server");
        try (Socket socket = new Socket("localhost", 1337);
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             Scanner console = new Scanner(System.in)) {

            System.out.println("connected; entering command: ");
            //suhtlus
            readServer(din);
            while (true) {
                writeServer(console, dout);
                try {
                    readServer(din);
                } catch (EOFException e) { //handler lõpetas töö
                    System.out.println("closing connection");
                    break;
                }
            }

        } finally {
            System.out.println("Client has finished");
        }
    }

    private static void writeServer(Scanner console, DataOutputStream dout) throws IOException {
        String cmd = String.valueOf(console.nextLine());//kasutaja sisend
        dout.writeUTF(cmd);                             //saadab handlerile
    }

    private static void readServer(DataInputStream din) throws IOException {
        int len = din.readInt();                        //mitu vastust tuleb
        for (int i = 0; i < len; i++) {
            System.out.println(din.readUTF());          //väljasta kasutajale
        }
    }


    //vanad kodutöö meetodid, ei tea kas kasulik
    /*
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
    } */

}
