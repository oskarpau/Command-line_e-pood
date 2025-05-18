package failisuhtlus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ostukorv klass võimaldab toodete ajutist hoidmist enne tellimuse esitamist
 * Iga toode on seotud kogusega
 */
public class Ostukorv {
    @JsonProperty
    private Map<Toode, Integer> tooted;

    public Map<Toode, Integer> getTooted() {
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
    @JsonIgnore
    public Map<Toode, Integer> getItems() {
        return tooted;
    }

    /**
     * Arvutab kogu ostukorvi hinna
     * @return tagastab BigDecimal tüüpi kogusumma
     */
    @JsonIgnore
    public BigDecimal getKoguHind() {
        BigDecimal total = BigDecimal.valueOf(0.0);
        for (Map.Entry<Toode, Integer> entry : tooted.entrySet()) {
            total = total.add(entry.getKey().getHind().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return total;
    }

    /**
     * Toote koguse vähendamiseks
     * @param toode
     * @param kogus
     */
    public void addLowerQuantity(Toode toode, int kogus) {
        tooted.put(toode, kogus);
    }

    /**
     * Prindib ostukorvi sisu ja info
     */
    public void printOstukorv() {
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

    /**
     * Kiiresti debugimiseks, et saaks .sout-i kasutada
     * @return
     */
    @Override
    public String toString() {
        return "Ostukorv{" +
                "tooted=" + tooted +
                '}';
    }

    /**
     * loetava info saamiseks. tabeli kujul {arv}x {nimi}:   {koguhind}€, kus koguhind on hind * arv
     * @return
     */
    public String clientToString() {
        return tooted.entrySet().stream()
                .map(entry ->
                    entry.getValue() +
                            "x "+ entry.getKey().getNimi() +
                            ":\t"+ entry.getKey().getHind()
                                .multiply(BigDecimal.valueOf(entry.getValue())
                                        .setScale(2, RoundingMode.HALF_UP))+
                        "€")
                .collect(Collectors.joining("\n"));
    }
}
