package epood;

// käsurea argumendid: file fail1.txt echo tere kaks korda file error.txt

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {

        System.out.println("connecting to server");

        secureConnection(1337); //hangi ühendus TLS-iga

        //unsecure connection
//        try (Socket socket = new Socket("localhost", 1337);
//             DataInputStream din = new DataInputStream(socket.getInputStream());
//             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
//             Scanner console = new Scanner(System.in)) {
//
//            System.out.println("connected; entering command: ");
//            //suhtlus
//            readServer(din);
//            while (true) {
//                writeServer(console, dout);
//                try {
//                    readServer(din);
//                } catch (EOFException e) { //handler lõpetas töö
//                    System.out.println("closing connection");
//                    break;
//                }
//            }
//
//        } finally {
//            System.out.println("Client has finished");
//        }
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


    private static void secureConnection(int port) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
        File storeFile = new File("epood/truststore.p12");
        String storePass = "secret";

        KeyStore store = KeyStore.getInstance(storeFile, storePass.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(store);
        TrustManager[] trustManagers = tmf.getTrustManagers();

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustManagers, null);
        try (Socket socket = ctx.getSocketFactory().createSocket("localhost", port);
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
             Scanner console = new Scanner(System.in)) {
            // use like a regular socket

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
}
