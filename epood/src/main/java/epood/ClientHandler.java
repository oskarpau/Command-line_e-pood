package epood;

import failisuhtlus.Ostukorv;

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
    private String currentScreen = "login";

    private CatalogueHandler catalogueHandler;
    private CartHandler cartHandler;
    private OrderHandler orderHandler;
    private SearchHandler searchHandler;
    private LoginHandler loginHandler;
    private HistoryEmployeeHandler historyEmployeeHandler;
    private HistoryClientHandler historyClientHandler;
    private Ostukorv cart;
    private Context context;
    private ClientServerSide client;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        catalogueHandler = new CatalogueHandler();
        cartHandler = new CartHandler();
        orderHandler = new OrderHandler();
        searchHandler = new SearchHandler();
        loginHandler = new LoginHandler();
        historyClientHandler = new HistoryClientHandler();
        historyEmployeeHandler = new HistoryEmployeeHandler();
        cart = new Ostukorv();
        context = new Context();

        client = null;
    }

    public void run() {
        try (socket;
             DataInputStream din = new DataInputStream(socket.getInputStream());
             DataOutputStream dout = new DataOutputStream(socket.getOutputStream())) {

            // initsialiseerime vajalikud handlerid
            CatalogueHandler catalogueHandler = new CatalogueHandler();

            //kommunikatsioon algab
            System.out.println("client connected; waiting for a command");
            dout.writeInt(1);
            dout.writeUTF("Tere tulemast meie e-poodi!\nVali, kes sa oled:\nklient, töötaja");

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
        if (cmd.equalsIgnoreCase("back")) {
            if (!currentScreen.equals("main")) {
                showMain(dout);
                currentScreen = "main";
                System.out.println("here");
                System.out.println(currentScreen);

            } else {
                dout.writeInt(1);
                dout.writeUTF("Olete juba peamenüüs");
            }
            return;
        }

        //variant 2. saab viidata ka muude klasside meetoditele jne. lisage vaid case juurde viitadega meetoditele
        // '->' süntaksi kasutamine välimises switchis viskas erroreid
        System.out.println(currentScreen);
        switch (currentScreen) {
            case "login":
                client = loginHandler.handler(dout, cmd, args, context); break;
            case "main":
                switch (cmd) {
                    case "echo" -> echo(dout, args);
                    case "help" -> help(dout);
                    case "catalogue" -> {
                        catalogueHandler.show(dout, context.type);
                        currentScreen = "catalogue";
                    }
                    case "search" -> {
                        // proov toodete searchist
                        searchHandler.show(dout);
                        currentScreen = "search";
                    }
                    case "cart" -> {
                        if (context.type == Config.CLIENT) {
                            cartHandler.show(dout, client.getCart());
                            currentScreen = "cart";
                        } else if (context.type == Config.EMPLOYEE) {
                            dout.writeInt(1);
                            dout.writeUTF("invalid command, for common commands type: help");
                        }

                    }
                    case "order" -> {
                        if (context.type == Config.CLIENT) {
                            orderHandler.show(dout);
                            currentScreen = "order";
                        } else if (context.type == Config.EMPLOYEE) {
                            dout.writeInt(1);
                            dout.writeUTF("invalid command, for common commands type: help");
                        }
                    }
                    case "history" -> {
                        if (context.type == Config.EMPLOYEE) {
                            historyEmployeeHandler.show(dout);
                            currentScreen = "history_employee";
                        } else if (context.type == Config.CLIENT) {
                            historyClientHandler.show(dout);
                            currentScreen = "history_client";
                        }
                    }
                    default -> {
                        dout.writeInt(1);
                        dout.writeUTF("invalid command, for common commands type: help");
                    }
                }
                break;

            case "catalogue": catalogueHandler.handler(dout, cmd, args, client, context.type); break;
            case "search": searchHandler.handler(dout, cmd, args, cart); break;
            case "cart": cartHandler.handler(dout, cmd, args, client.getCart(), client); break;
            case "order": orderHandler.handler(dout, cmd, args, cart); break;
            case "history_employee": historyEmployeeHandler.handler(dout, cmd, args);break;
            case "history_client": historyClientHandler.handler(dout, cmd, args);break;
            default: {
                System.out.println("undefined category, smth broken");
            }
        }


    }

    private void showMain(DataOutputStream dout) throws IOException {
        if (context.type == Config.CLIENT) {
            dout.writeInt(1);
            dout.writeUTF("Palun valige üks alljärgnevatest:\ncatalogue; search; cart; order; history; exit\nsisestage 'back', et naasta peamenüüsse");
        } else if (context.type == Config.EMPLOYEE) {
            dout.writeInt(1);
            dout.writeUTF("Palun valige üks alljärgnevatest:\ncatalogue; search; history; exit\nsisestage 'back', et naasta peamenüüsse");
        }
    }



    private void help(DataOutputStream dout) throws IOException {
        System.out.println("helping");
        String[] cmdList = {"exit", "help", "back", "catalogue", "search", "cart", "order"};
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
