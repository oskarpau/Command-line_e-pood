package failisuhtlus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class Toode {
    private int number;
    private String nimi;
    private BigDecimal hind; // BigDecimal, sest double andis kahe hinna liitmisel ümardamise erroreid
    private int lao_seis;

    /**
     * Vajalik Jacksoni jaoks
     */
    public Toode() {}

    public Toode(int number, String nimi, BigDecimal hind, int lao_seis) {
        this.number = number;
        this.nimi = nimi;
        this.hind = hind;
        this.lao_seis = lao_seis;
    }
    @JsonProperty
    public int getNumber() {
        return number;
    }
    @JsonProperty
    public String getNimi() {
        return nimi;
    }
    @JsonProperty
    public BigDecimal getHind() {
        return hind;
    }
    @JsonProperty
    public int getLao_seis() {
        return lao_seis;
    }

    public void setLao_seis(int lao_seis) {
        this.lao_seis = lao_seis;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public void setHind(BigDecimal hind) {
        this.hind = hind;
    }

    /**
     * Jacksoni jaoks
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // sama pointer
        if (o == null || getClass() != o.getClass()) return false;

        Toode toode = (Toode) o;
        return Objects.equals(number, toode.number);
    }

    /**
     * Jacksoni jaoks
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "Toode{" +
                "number=" + number +
                ", nimi='" + nimi + '\'' +
                ", hind=" + hind +
                ", lao_seis=" + lao_seis +
                '}';
    }

}
