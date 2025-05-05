package failisuhtlus;

import java.math.BigDecimal;

public class Toode {
    private int number;
    private String nimi;
    private BigDecimal hind; // BigDecimal, sest double andis kahe hinna liitmisel ümardamise erroreid
    private int lao_seis;

    public Toode(int number, String nimi, BigDecimal hind, int lao_seis) {
        this.number = number;
        this.nimi = nimi;
        this.hind = hind;
        this.lao_seis = lao_seis;
    }

    public int getNumber() {
        return number;
    }

    public String getNimi() {
        return nimi;
    }

    public BigDecimal getHind() {
        return hind;
    }

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
