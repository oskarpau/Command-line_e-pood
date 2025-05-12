package failisuhtlus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Tellimus {
    private String nimi;
    private String email;
    private Ostukorv ostukorv;

    /**
     * Vajalik Jacksoni jaoks
     */
    public Tellimus() {}

    public Tellimus(String nimi, String email, Ostukorv ostukorv) {
        this.nimi = nimi;
        this.email = email;
        this.ostukorv = ostukorv;
    }
    @JsonProperty
    public String getNimi() {
        return nimi;
    }
    @JsonProperty
    public String getEmail() {
        return email;
    }
    @JsonProperty
    public Ostukorv getOstukorv() {
        return ostukorv;
    }


}
