package epood;

import failisuhtlus.Ostukorv;

public class ClientServerSide {
    private String name;
    private String email;
    private Ostukorv cart;

    /**
     * Ostukorv on initsialiseeritud konstruktoris.
     * @param name
     * @param email
     */
    public ClientServerSide(String name, String email) {
        this.name = name;
        this.email = email;
        cart = new Ostukorv();
    }

    public boolean checkMatch(String name, String email) {
        return this.name.equalsIgnoreCase(name) && this.email.equalsIgnoreCase(email);
    }
}
