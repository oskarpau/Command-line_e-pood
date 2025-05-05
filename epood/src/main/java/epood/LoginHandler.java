package epood;

import failisuhtlus.JsonManagerEmployee;
import failisuhtlus.JsonManagerClient;
import failisuhtlus.Ostukorv;
import org.apache.commons.validator.routines.EmailValidator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class LoginHandler{
    static final byte CHOOSE_TYPE = 1;
    static final byte ENTER_FORENAME = 2;
    static final byte ENTER_SURNAME = 3;
    static final byte ENTER_EMAIL = 4;
    static final byte ENTER_PASSWORD = 5;
    private String name;
    private String email;
    private String password;
    private byte currentSubScreen;
    private JsonManagerEmployee jsonManagerEmployee;
    private JsonManagerClient jsonManagerClient;

    public LoginHandler() {
        this.currentSubScreen = CHOOSE_TYPE;
        this.jsonManagerEmployee = new JsonManagerEmployee();
        this.jsonManagerClient = new JsonManagerClient();
    }


    public ClientServerSide handler(DataOutputStream dout, String cmd, String[] args, Context context) throws IOException {
        ClientServerSide client = null;
        switch (currentSubScreen) {
            case CHOOSE_TYPE:
                if (cmd.equals("klient")) {
                    context.type = Config.CLIENT;
                    currentSubScreen =  ENTER_FORENAME;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage eesnimi: ");
                } else if (cmd.equals("töötaja")) {
                    context.type = Config.EMPLOYEE;
                    currentSubScreen =  ENTER_EMAIL;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage email: ");
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Please enter valid type");
                } break;
            case ENTER_FORENAME:
                name = cmd;
                currentSubScreen =  ENTER_SURNAME;
                dout.writeInt(1);
                dout.writeUTF("Sisestage perekonnanimi: ");
                break;

            case ENTER_SURNAME:
                name += " " + cmd;
                currentSubScreen =  ENTER_EMAIL;
                dout.writeInt(1);
                dout.writeUTF("Sisestage email: ");
                break;
            case ENTER_EMAIL:
                if (EmailValidator.getInstance().isValid(cmd)) {
                    email = cmd;
                    if (context.type == Config.CLIENT) {
                        // vaatame, kas meil on juba selline klient olemas
                        List<ClientServerSide> clients = jsonManagerClient.readJson();
                        for (ClientServerSide c : clients) {
                            if (c.checkMatch(name, email)) {
                                /*if (c.getCart().getItems().isEmpty()) {
                                    client = new ClientServerSide(c.getName(), c.getEmail(), c.getCart()); // peaksime client objektiga edasi tegeleme, et tellimus koos sellega vormistada
                                } else {
                                    client = new ClientServerSide(c.getName(), c.getEmail());
                                }*/
                                dout.writeInt(1);
                                dout.writeUTF("Tere tulemast tagasi " + c.getName() + " " + c.getCart() +
                                        "\nKirjutage 'back', et minna peamenüüsse"); // Võtame salvestatud kliendi väärtused,
                                // sest kui uuesti sisselogimisel tehti nimes kirjaviga, siis kuvatakse ikka esialgne
                                return c;
                            }
                        }
                        ClientServerSide newClient = new ClientServerSide(name, email);
                        jsonManagerClient.addClientJson(newClient);
                        dout.writeInt(1);
                        dout.writeUTF("Olete loonud kasutaja: " + name + " " + email +
                                "\nKirjutage 'back', et minna peamenüüsse");
                        return newClient;
                    } else if (context.type == Config.EMPLOYEE) {
                        currentSubScreen =  ENTER_PASSWORD;
                        dout.writeInt(1);
                        dout.writeUTF("Sisestage parool: ");
                    }
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage korrektne email: ");
                } break;
            case ENTER_PASSWORD:
                password = cmd;

                List<EmployeeServerSide> employees = jsonManagerEmployee.readJson();
                System.out.println(employees);
                EmployeeServerSide loginTarget = null;
                for (EmployeeServerSide employee : employees) {
                    if (employee.getEmail().equals(email)) {
                        loginTarget = employee;
                        break;
                    }

                }
                if (loginTarget != null ){

                    if (loginTarget.checkCredentials(email, password)) {
                        dout.writeInt(1);
                        dout.writeUTF("Olete edukalt sisseloginud (email: " + email + ")" +
                                "\nKirjutage 'back', et minna peamenüüsse");
                        return client;
                    }
                }
                currentSubScreen = ENTER_EMAIL;
                dout.writeInt(1);
                dout.writeUTF("Selliste andmetega töötajat ei leitud.\nSisestage email:");
                break;

        }
        return client;
    }

}
