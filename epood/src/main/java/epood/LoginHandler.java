package epood;

import failisuhtlus.JsonManagerEmployee;
import failisuhtlus.JsonManagerClient;
import failisuhtlus.Ostukorv;
import org.apache.commons.validator.routines.EmailValidator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import static failisuhtlus.EncryptionManager.passwordToArgon;

public class LoginHandler {
    static final byte CHOOSE_TYPE = 1;
    static final byte ENTER_FORENAME = 2;
    static final byte ENTER_SURNAME = 3;
    static final byte ENTER_EMAIL = 4;
    static final byte ENTER_PASSWORD = 5;
    static final byte LOG_OR_SIGN = 6;
    static final byte AGAIN_OR_SIGN = 7;
    static final byte LOG = 8;
    static final byte SIGN = 9;
    static final byte AGAIN_OR_LOG = 10;
    static final byte ENTER_PASSWORD_AGAIN = 11;
    static final byte AGAIN_OR_START = 12;
    private byte action;
    private String name;
    private String email;
    private String password;
    private String lastPassword;
    private byte currentSubScreen;
    private JsonManagerEmployee jsonManagerEmployee;
    private JsonManagerClient jsonManagerClient;

    public LoginHandler() {
        this.currentSubScreen = CHOOSE_TYPE;
        this.jsonManagerEmployee = new JsonManagerEmployee();
        this.jsonManagerClient = new JsonManagerClient();
        action = -1;
    }


    public ClientServerSide handler(DataOutputStream dout, String cmd, String[] args, Context context) throws IOException {
        ClientServerSide client = null;
        System.out.println(currentSubScreen);
        switch (currentSubScreen) {
            case CHOOSE_TYPE:
                if (cmd.equalsIgnoreCase("klient")) {
                    context.type = Config.CLIENT;
                    currentSubScreen = LOG_OR_SIGN;
                    dout.writeInt(1);
                    dout.writeUTF("Kas soovite sisse logida või luua uue kasutaja?\n(sisestage 'log' või 'sign')");
                } else if (cmd.equalsIgnoreCase("töötaja")) {
                    context.type = Config.EMPLOYEE;
                    currentSubScreen = ENTER_EMAIL;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage email: ");
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Palun sisestage 'klient' või 'töötaja'");
                }
                break;

            case LOG_OR_SIGN:
                if (cmd.equalsIgnoreCase("log")) {
                    if (!jsonManagerClient.readJson().isEmpty()) {
                        dout.writeInt(1);
                        dout.writeUTF("Palun sisestage email");
                        action = LOG;
                        currentSubScreen = ENTER_EMAIL;
                    } else {
                        dout.writeInt(1);
                        dout.writeUTF("Ühtegi kasutajat ei ole veel loodud!\nSisestage 'sign'");
                    }
                } else if (cmd.equalsIgnoreCase("sign")) {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage eesnimi:");
                    action = SIGN;
                    currentSubScreen = ENTER_FORENAME;
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage 'log' või 'sign'");
                    currentSubScreen = LOG_OR_SIGN;
                }
                break;
            case ENTER_FORENAME:
                name = cmd;
                currentSubScreen = ENTER_SURNAME;
                dout.writeInt(1);
                dout.writeUTF("Sisestage perekonnanimi: ");
                break;

            case ENTER_SURNAME:
                name += " " + cmd;
                currentSubScreen = ENTER_EMAIL;
                dout.writeInt(1);
                dout.writeUTF("Sisestage email: ");
                break;
            case ENTER_EMAIL:
                if (EmailValidator.getInstance().isValid(cmd)) {
                    email = cmd;
                    if (context.type == Config.CLIENT) {
                        // vaatame, kas meil on juba selline klient olemas
                        List<ClientServerSide> clients = jsonManagerClient.readJson();
                        System.out.println(clients);
                        System.out.println(email);
                        for (ClientServerSide c : clients) {
                            if (c.checkMatch(email)) {
                                System.out.println("c.checkMatch(email)");
                                System.out.println(action);
                                if (action == LOG) {
                                    dout.writeInt(1);
                                    dout.writeUTF("Sisestage parool:");
                                    currentSubScreen = ENTER_PASSWORD;
                                    return client;
                                } else if (action == SIGN) {
                                    dout.writeInt(1);
                                    dout.writeUTF("Sellise emailiga kasutaja on juba olemas:\n" +
                                            "Kas soovite sisestada emaili uuesti või logida sisse?\n" +
                                            "Sisestage (again) või (log)");
                                    currentSubScreen = AGAIN_OR_LOG;
                                    return client;
                                }
                            }
                        }
                        if (action == LOG) {
                            dout.writeInt(1);
                            dout.writeUTF("Sellise emailiga kasutajat ei leitud:\n" +
                                    "Kas soovite sisestada uue emaili või luua konto?\n" +
                                    "Sisestage (again) või (sign)");
                            currentSubScreen = AGAIN_OR_SIGN;
                        } else if (action == SIGN) {
                            dout.writeInt(1);
                            dout.writeUTF("Sisestage parool:");
                            currentSubScreen = ENTER_PASSWORD;
                        }


                    } else if (context.type == Config.EMPLOYEE) {
                        currentSubScreen = ENTER_PASSWORD;
                        dout.writeInt(1);
                        dout.writeUTF("Sisestage parool: ");
                    }
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage korrektne email: ");
                }
                break;

            case AGAIN_OR_SIGN:
                if (cmd.equalsIgnoreCase("again")) {
                    currentSubScreen = ENTER_EMAIL;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage email: ");
                } else if (cmd.equalsIgnoreCase("sign")) {
                    currentSubScreen = ENTER_FORENAME;
                    action = SIGN;
                    dout.writeInt(1);
                    dout.writeUTF("Olete valinud uue kasutaja loomise!\nSisestage eesnimi:");
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage (again) või (sign)");
                }

                break;
            case AGAIN_OR_LOG:
                if (cmd.equalsIgnoreCase("again")) {
                    currentSubScreen = ENTER_EMAIL;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage email: ");
                } else if (cmd.equalsIgnoreCase("log")) {
                    currentSubScreen = ENTER_EMAIL;
                    action = LOG;
                    dout.writeInt(1);
                    dout.writeUTF("Olete valinud sisselogimise!\nSisestage email:");
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage (again) või (log)");
                }
                break;
            case ENTER_PASSWORD:
                password = cmd;
                if (context.type == Config.EMPLOYEE || action == LOG) {
                    if (context.type == Config.EMPLOYEE) {
                        List<EmployeeServerSide> employees = jsonManagerEmployee.readJson();
                        System.out.println(employees);
                        EmployeeServerSide loginTarget = null;
                        for (EmployeeServerSide employee : employees) {
                            if (employee.getEmail().equals(email)) {
                                loginTarget = employee;
                                break;
                            }
                        }
                        if (loginTarget != null) {
                            if (loginTarget.checkCredentials(email, password)) {
                                dout.writeInt(1);
                                dout.writeUTF("Olete edukalt sisseloginud (email: " + email + ")" +
                                        "\nKirjutage 'back', et minna peamenüüsse");
                                return client;
                            }
                        }

                        currentSubScreen = ENTER_EMAIL;
                        dout.writeInt(1);
                        dout.writeUTF("Selliste andmetega kontot ei leitud.\nSisestage email:");
                    } else if (action == LOG) {
                        List<ClientServerSide> clients = jsonManagerClient.readJson();
                        System.out.println(clients);
                        ClientServerSide loginTarget = null;
                        for (ClientServerSide clientAcc : clients) {
                            if (clientAcc.getEmail().equals(email)) {
                                loginTarget = clientAcc;
                                break;
                            }
                        }
                        if (loginTarget != null) {
                            if (loginTarget.checkCredentials(email, password)) {

                                client = jsonManagerClient.getClientByEmail(email);
                                dout.writeInt(1);
                                dout.writeUTF("Tere tulemast tagasi " + client.getName() +
                                        "\nKirjutage 'back', et minna peamenüüsse");
                                return client; // tagastame juba registeeritud kliendi objekti
                            }
                        }
                        currentSubScreen = AGAIN_OR_START;
                        dout.writeInt(1);
                        dout.writeUTF("Selliste andmetega kontot ei leitud.\nKas soovite minna tagasi algusesse " +
                                "või sisestada parooli uuesti?\nSisestage (start) või (again):");
                    }

                } else if (action == SIGN) {
                    lastPassword = password;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage parool uuesti:");
                    currentSubScreen = ENTER_PASSWORD_AGAIN;
                }
                break;

            case ENTER_PASSWORD_AGAIN:
                password = cmd;
                if (password.equals(lastPassword)) {
                    ClientServerSide newClient = new ClientServerSide(name, email, passwordToArgon(password));
                    jsonManagerClient.addClientJson(newClient);
                    dout.writeInt(1);
                    dout.writeUTF("Olete loonud kasutaja: " + name + " " + email +
                            "\nKirjutage 'back', et minna peamenüüsse");
                    return newClient;
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Paroolid ei klapi!\nSisestage parool:");
                    currentSubScreen = ENTER_PASSWORD;
                } break;

            case AGAIN_OR_START:
                if (cmd.equalsIgnoreCase("again")) {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage parool:");
                    currentSubScreen = ENTER_PASSWORD;
                } else if (cmd.equalsIgnoreCase("start")) {
                    dout.writeInt(1);
                    dout.writeUTF("Kas soovite sisse logida või luua uue kasutaja?\n(sisestage 'log' või 'sign')");
                    currentSubScreen = LOG_OR_SIGN;
                }
                break;
        }
        return client;
    }

}
