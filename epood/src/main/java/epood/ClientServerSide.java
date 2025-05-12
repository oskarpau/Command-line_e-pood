package epood;

import failisuhtlus.JsonManagerClient;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;

import java.io.IOException;
import java.util.UUID;

public class ClientServerSide {
    private String name;
    private String email;
    private Ostukorv cart;
    private UUID id;

    public ClientServerSide(){}

    /**
     * Kliendi on uus klient
     * @param name
     * @param email
     */
    public ClientServerSide(String name, String email) {
        this.name = name;
        this.email = email;
        this.id = UUID.randomUUID();
        cart = new Ostukorv();
    }

    /**
     * Kliendi ostukorv on tühi
     * @param name
     * @param email
     * @param id
     */
    public ClientServerSide(String name, String email, UUID id) {
        this.name = name;
        this.email = email;
        this.id = id;
        cart = new Ostukorv();
    }


    /**
     * Klient on registreeritud ning tema ostukorv ei ole tühi
     * @param name
     * @param email
     * @param cart
     */
    public ClientServerSide(String name, String email, Ostukorv cart, UUID id) {
        this.name = name;
        this.email = email;
        this.cart = cart;
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Vaatamaks, kas meil on sellise nime ja emailiga klient juba olemas
     * @param name
     * @param email
     * @return
     */
    public boolean checkMatch(String name, String email) {
        return this.name.equalsIgnoreCase(name) && this.email.equalsIgnoreCase(email);
    }

    public void addToode(Toode toode, int kogus, JsonManagerClient jsonManager) throws IOException {
        cart.addToode(toode, kogus);
        jsonManager.updateCartJson(new ClientServerSide(name, email, cart, id)); // võiksime anda ka parameetrid, mitte uue objekti, aga see läheks kohmakamaks, eriti kui tulevikus paramaatreid rohkem tuleb
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ClientServerSide{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", cart=" + cart +
                '}';
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
