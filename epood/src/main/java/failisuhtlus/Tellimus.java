package failisuhtlus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Tellimus {
    private String nimi;
    private String email;
    private UUID kliendiID;
    private UUID tellimuseID;
    private Ostukorv ostukorv;

    /**
     * Vajalik Jacksoni jaoks
     */
    public Tellimus() {}

    public Tellimus(String nimi, String email, Ostukorv ostukorv, UUID kliendiID) {
        this.nimi = nimi;
        this.email = email;
        this.kliendiID = kliendiID;
        this.tellimuseID = UUID.randomUUID();
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
    @JsonProperty
    public UUID getKliendiID() {
        return kliendiID;
    }

    @Override
    public String toString() {
        return "Tellimus{" +
                "nimi='" + nimi + '\'' +
                ", email='" + email + '\'' +
                ", kliendiID=" + kliendiID +
                ", tellimuseID=" + tellimuseID +
                ", ostukorv=" + ostukorv +
                '}';
    }

    @JsonProperty
    public UUID getTellimuseID() {
        return tellimuseID;
    }
}
