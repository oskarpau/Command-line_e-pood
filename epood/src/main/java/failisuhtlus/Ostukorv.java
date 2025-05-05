package failisuhtlus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.*;

/**
 * Ostukorv klass võimaldab toodete ajutist hoidmist enne tellimuse esitamist
 * Iga toode on seotud kogusega
 */
public class Ostukorv {
    @JsonProperty
    private static Map<Toode, Integer> tooted;

    @JsonProperty
    public static Map<Toode, Integer> getTooted() {
        return tooted;
    }

    public Ostukorv() {
        this.tooted = new HashMap<>();
    }


    /**
     * Lisab toote ostukorvi või kui on juba olemas, siis suurendab kogust
     * @param toode lisatav toode
     * @param kogus lisatava toote kogus
     */
    public void addToode(Toode toode, int kogus) {
        tooted.put(toode, tooted.getOrDefault(toode, 0) + kogus);
    }

    /**
     * Eemaldab konkreetse toote
     * @param toode eemaldatav toode
     */
    public void removeToode(Toode toode) {
        tooted.remove(toode);
    }

    /**
     * tühjendab kogu ostukorvi
     */
    public void tyhjendaOstukorv() {
        tooted.clear();
    }

    /**
     * Tagastab kogu ostukorvi, mis hetkel seal on
     * @return tagastab ostukorvis olevate toodete nimede ja koguste mapi
     */
    public static Map<Toode, Integer> getItems() {
        return tooted;
    }

    /**
     * Arvutab kogu ostukorvi hinna
     * @return tagastab BigDecimal tüüpi kogusumma
     */
    public static BigDecimal getKoguHind() {
        BigDecimal total = BigDecimal.valueOf(0.0);
        for (Map.Entry<Toode, Integer> entry : tooted.entrySet()) {
            total = total.add(entry.getKey().getHind().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return total;
    }

    /**
     * Prindib ostukorvi sisu ja info
     */
    public static void printOstukorv() {
        if (tooted.isEmpty()) {
            System.out.println("Ostukorv on tühi.");
        } else {
            System.out.println("Ostukorvis olevad tooted:");
            for (Map.Entry<Toode, Integer> entry : tooted.entrySet()) {
                System.out.println(entry.getKey().getNimi() + " x" + entry.getValue() +
                        " = " + entry.getKey().getHind().multiply(BigDecimal.valueOf(entry.getValue()))  + " EUR");
            }
            System.out.println("Kokku: " + getKoguHind() + " EUR");
        }
    }
}
