package epood;

import failisuhtlus.EncryptionManager;
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
    private String password;

    public ClientServerSide(){}

    /**
     * Klient on uus klient
     * @param name
     * @param email
     * @param password
     */
    public ClientServerSide(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.id = UUID.randomUUID();
        cart = new Ostukorv();
    }

    /**
     * Kliendi ostukorv on tühi
     * @param name
     * @param email
     * @param id
     * @param password
     */
    public ClientServerSide(String name, String email, String password, UUID id) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.id = id;
        cart = new Ostukorv();
    }


    /**
     * Klient on registreeritud ning tema ostukorv ei ole tühi
     * @param name
     * @param email
     * @param password
     * @param cart
     * @param id
     */
    public ClientServerSide(String name, String email, String password, Ostukorv cart, UUID id) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.cart = cart;
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Vaatamaks, kas meil on sellise nime ja emailiga klient juba olemas
     * @param email
     * @return
     */
    public boolean checkMatch(String email) {
        return this.email.equalsIgnoreCase(email);
    }

    /**
     * Kontrollime, kas andmed on õiged, pigem tuleviku jaoks, kui emailid ja paroolid on turvalisemalt teises kohas
     * hashib parooli ja võrdleb räsisid
     * @param email email
     * @param password hashimata sisend
     * @return kas andmed on õiged
     */
    public boolean checkCredentials(String email, String password) {
        return this.email.equals(email) && EncryptionManager.verifyPassword(this.password, password);
    }

    public void addToode(Toode toode, int kogus, JsonManagerClient jsonManager) throws IOException {
        cart.addToode(toode, kogus);
        jsonManager.updateCartJson(new ClientServerSide(name, email, password, cart, id)); // võiksime anda ka parameetrid, mitte uue objekti, aga see läheks kohmakamaks, eriti kui tulevikus paramaatreid rohkem tuleb
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
