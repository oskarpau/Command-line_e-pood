package failisuhtlus;

public class Toode {
    private int number;
    private String nimi;
    private double hind;
    private int lao_seis;

    public Toode(int number, String nimi, double hind, int lao_seis) {
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

    public double getHind() {
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
