package epood;

import failisuhtlus.JsonManagerClient;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;

import java.io.IOException;

public class ClientServerSide {
    private String name;
    private String email;
    private Ostukorv cart;

    public ClientServerSide(){}

    /**
     * Kliendi ostukorv on tühi või ta on uus klient
     * @param name
     * @param email
     */
    public ClientServerSide(String name, String email) {
        this.name = name;
        this.email = email;
        cart = new Ostukorv();
    }

    /**
     * Klient on registreeritud ning tema ostukorv ei ole tühi
     * @param name
     * @param email
     * @param cart
     */
    public ClientServerSide(String name, String email, Ostukorv cart) {
        this.name = name;
        this.email = email;
        this.cart = cart;
    }

    public boolean checkMatch(String name, String email) {
        return this.name.equalsIgnoreCase(name) && this.email.equalsIgnoreCase(email);
    }

    public void addToode(Toode toode, int kogus, JsonManagerClient jsonManager) throws IOException {
        cart.addToode(toode, kogus);
        jsonManager.updateCartJson(new ClientServerSide(name, email, cart)); // võiksime anda ka parameetrid, aga see läheks kohmakamaks, eriti kui tulevikus paramaatreid rohkem tuleb
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Ostukorv getCart() {
        return cart;
    }

    public void setCart(Ostukorv cart) {
        this.cart = cart;
    }
}
