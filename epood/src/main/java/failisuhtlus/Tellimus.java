package failisuhtlus;

import java.util.List;
import java.util.Map;

public class Tellimus {
    private String nimi;
    private String email;
    private Ostukorv ostukorv;

    public Tellimus(String nimi, String email, Ostukorv ostukorv) {
        this.nimi = nimi;
        this.email = email;
        this.ostukorv = ostukorv;
    }

    public String getNimi() {
        return nimi;
    }

    public String getEmail() {
        return email;
    }

    public Ostukorv getOstukorv() {
        return ostukorv;
    }


}
