package epood;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    private static void echo(DataInputStream din, DataOutputStream dout) throws IOException {
        int messageLength = din.readInt();
        String msg = "";
        for (int i = 0; i < messageLength; i++) {
            msg += din.readUTF() + " ";
        }
        System.out.println("received message: " + msg);
        dout.writeInt(1);
        dout.writeUTF(msg);
    }

    public void run() {
        try (socket;
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream())) {

            //kommunikatsioon algab
            System.out.println("client connected; waiting for a command");

            while (true) {
                String cmd = din.readUTF();
                System.out.println("received cmd: " + cmd);

                //variant 1, ärge seda kasutage
                if (cmd.equals("exit")) {
                    System.out.println("client disconnected");
                    dout.write(-1);
                    break;
                }

                //variant 2. saab viidata ka muude klasside meetoditele jne. lisage vaid case juurde viitadega meetoditele
                switch (cmd) {
                    case "echo" -> echo(din, dout);
                    case "help" -> help(dout);
                    default -> {
                        dout.writeInt(1);
                        dout.writeUTF("invalid command, for common commands type: help");
                    }
                }
            }
            //kommunikatsioon lõpeb
            System.out.println("Closed connection with client");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void help(DataOutputStream dout) throws IOException {
        System.out.println("helping");
        String[] cmdList = {"exit", "help"};
        dout.writeInt(cmdList.length);
        for (String cmd : cmdList) {
            dout.writeUTF(cmd);
        }
    }


}
