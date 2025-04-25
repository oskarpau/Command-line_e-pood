package failisuhtlus;

import java.math.BigDecimal;

public class Toode {
    private int number;
    private String nimi;
    private String kategooria;
    private BigDecimal hind; // BigDecimal, sest double andis kahe hinna liitmisel ümardamise erroreid
    private int lao_seis;

    public Toode(int number, String nimi, String kategooria, BigDecimal hind, int lao_seis) {
        this.number = number;
        this.nimi = nimi;
        this.kategooria = kategooria;
        this.hind = hind;
        this.lao_seis = lao_seis;
    }

    public String getKategooria() {
        return kategooria;
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
