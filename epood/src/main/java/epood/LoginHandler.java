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
    static final byte ENTER_NAME = 2;
    static final byte ENTER_EMAIL = 3;
    static final byte ENTER_PASSWORD = 4;
    static final byte CLIENT = 1;
    static final byte EMPLOYEE = 2;
    private String name;
    private String email;
    private String password;
    private byte currentSubScreen;
    private byte type;
    private JsonManagerEmployee jsonManagerEmployee;
    private JsonManagerClient jsonManagerClient;

    public LoginHandler() {
        this.currentSubScreen = CHOOSE_TYPE;
        this.jsonManagerEmployee = new JsonManagerEmployee();
        this.jsonManagerClient = new JsonManagerClient();
    }


    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart, ClientServerSide client) throws IOException {
        switch (currentSubScreen) {
            case CHOOSE_TYPE:
                if (cmd.equals("klient")) {
                    type = CLIENT;
                    currentSubScreen =  ENTER_NAME;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage nimi: ");
                } else if (cmd.equals("töötaja")) {
                    type = EMPLOYEE;
                    currentSubScreen =  ENTER_EMAIL;
                    dout.writeInt(1);
                    dout.writeUTF("Sisestage email: ");
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Please enter valid type");
                } break;
            case ENTER_NAME:
                name = cmd;
                currentSubScreen =  ENTER_EMAIL;
                dout.writeInt(1);
                dout.writeUTF("Sisestage email: ");
                break;
            case ENTER_EMAIL:
                if (EmailValidator.getInstance().isValid(cmd)) {
                    email = cmd;
                    if (type == CLIENT) {
                        // vaatame, kas meil on juba selline klient olemas
                        List<ClientServerSide> clients = jsonManagerClient.readJson();
                        System.out.println("here1");
                        for (ClientServerSide c : clients) {
                            if (c.checkMatch(name, email)) {
                                client = c;
                                dout.writeInt(1);
                                dout.writeUTF("Tere tulemast tagasi " + name + " " + email +
                                        "\nKirjutage 'back', et minna peamenüüsse");
                                return;
                            }
                        }
                        System.out.println("here2");
                        ClientServerSide newClient = new ClientServerSide(name, email);
                        jsonManagerClient.writeJson(newClient);
                        client = newClient;
                        dout.writeInt(1);
                        dout.writeUTF("Olete loonud kasutaja: " + name + " " + email +
                                "\nKirjutage 'back', et minna peamenüüsse");
                        return;
                    } else if (type == EMPLOYEE) {
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
                for (EmployeeServerSide employee : employees) {
                    if (employee.checkCredentials(email, password)) {
                        dout.writeInt(1);
                        dout.writeUTF("Olete edukalt sisseloginud (email: " + email + ")" +
                                "\nKirjutage 'back', et minna peamenüüsse");
                        return;
                    }
                }
                currentSubScreen = ENTER_EMAIL;
                dout.writeInt(1);
                dout.writeUTF("Selliste andmetega töötajat ei leitud.\nSisestage email:");
                break;
        }
    }
}
