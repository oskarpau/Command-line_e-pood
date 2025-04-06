package epood;

import failisuhtlus.JsonReader;
import failisuhtlus.Toode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private final JsonReader dataReader = new JsonReader("andmebaas.json");

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try (socket;
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream())) {

            //kommunikatsioon algab
            System.out.println("client connected; waiting for a command");

            while (true) {
                String[] cmdFull = extractArgs(din.readUTF());
                String cmd = cmdFull[0];
                String[] args = Arrays.copyOfRange(cmdFull, 1, cmdFull.length);
                System.out.println("received cmd: " + cmd);
                System.out.println("args: " + Arrays.toString(args));

                //variant 1, ärge seda rohkem kasutage
                if (cmd.equals("exit")) { //exit ja ainult exit
                    System.out.println("client disconnected");
                    break;
                }

                interpretCmd(cmd, args, din, dout);
            }
            //kommunikatsioon lõpeb
            System.out.println("Closed connection with client");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] extractArgs(String cmd) {
        List<String> parts = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|\\S+"); // Match quoted text OR words
        Matcher matcher = pattern.matcher(cmd);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1)); // Extract text inside quotes (group 1)
            } else {
                parts.add(matcher.group()); // Extract normal words
            }
        }
        return parts.toArray(new String[0]);
    }

    private void interpretCmd(String cmd, String[] args, DataInputStream din, DataOutputStream dout) throws IOException {
        //variant 2. saab viidata ka muude klasside meetoditele jne. lisage vaid case juurde viitadega meetoditele
        switch (cmd) {
            case "echo" -> echo(dout, args);
            case "help" -> help(dout);

            // proov toodete searchist
            case "show" -> search(dout, "products");

            default -> {
                dout.writeInt(1);
                dout.writeUTF("invalid command, for common commands type: help");
            }
        }
    }

    private void search(DataOutputStream dout, String search) throws IOException {
        System.out.println("Seaching " + search);
        List<Toode> tooted = dataReader.getTooted();
        if (!tooted.isEmpty()) {
            dout.writeInt(tooted.size());
            for (Toode t : tooted) {
                dout.writeUTF(t.getNimi());
            }
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

    private static void echo(DataOutputStream dout, String[] args) throws IOException {
        System.out.println("echoing");
        if (args.length == 1 && !args[0].startsWith("-")) {
            dout.writeInt(1);
            dout.writeUTF(args[0]);
        }
//        for (int i = 0; i < messageLength; i++) {
//            msg += din.readUTF() + " ";
//        }
//        System.out.println("received message: " + msg);
//        dout.writeInt(1);
//        dout.writeUTF(msg);
    }
}
