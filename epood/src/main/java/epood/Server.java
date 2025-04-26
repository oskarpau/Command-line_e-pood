package epood;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newCachedThreadPool()) {
            Server server = new Server();

            secure(server, pool);  //TLS-iga port connection

//            try (ServerSocket ss = new ServerSocket(1337)) {
//                System.out.println("now listening on :1337");
//                while (true) {
//                    Socket clientsocket = ss.accept();
//                    pool.execute(new ClientHandler(clientsocket, server));
//                }
//            }
        }
    }


    private static void secure(Server server, ExecutorService pool) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        File storeFile = new File("epood/keystore.p12"); //public ja private keyd
        String storePass = "secret"; //serveri võtme salasõna, peaks genereerima, aga kuna võtmed tulevad cmdlinelt ss las ta olla

        KeyStore store = KeyStore.getInstance(storeFile, storePass.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(store, storePass.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagers, null, null);
        try (ServerSocket ss = ctx.getServerSocketFactory().createServerSocket(1337)) {
            // use like a regular server socket
            System.out.println("now listening on :1337");
            while (true) {
                Socket clientsocket = ss.accept();
                pool.execute(new ClientHandler(clientsocket, server));
            }
        }
    }

}

