package epood;

import failisuhtlus.JsonManagerHistory;
import failisuhtlus.Tellimus;
import failisuhtlus.Toode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryEmployeeHandler {
    private final JsonManagerHistory jsonManagerHistory = new JsonManagerHistory();

    public void show(DataOutputStream dout) throws IOException {
        displayOrders(dout);
    }

    /**
     * Siia võimalik hiljem funktsionaalsust lisada
     * @param dout
     * @param cmd
     * @param args
     * @throws IOException
     */
    public void handler(DataOutputStream dout, String cmd, String[] args) throws IOException {
        displayOrders(dout);
    }

    private void displayOrders(DataOutputStream dout) throws IOException {
        List<Tellimus> tellimused = jsonManagerHistory.readJson();
        StringBuilder sb = new StringBuilder("\n----Tellimused----\n");
        if (tellimused.isEmpty()) {
            sb.append("Tellimusi veel pole! :(\nSisetsage 'back', et naasta menüüsse");
            dout.writeInt(1);
            dout.writeUTF(sb.toString());
        } else {
            Map<Toode, Integer> tooted = new HashMap<>();
            for (Tellimus tellimus : tellimused) {
                sb.append(tellimus + "\n");
                for (Map.Entry<Toode, Integer> entry : tellimus.getOstukorv().getItems().entrySet()) {
                    Toode toode = entry.getKey();
                    tooted.put(toode, tooted.getOrDefault(toode, 0) + entry.getValue());
                }
            }
            Toode populaarseimToode = Collections.max(tooted.entrySet(), Map.Entry.comparingByValue()).getKey();
            sb.append("Kõige rohkem telliti toodet: " + populaarseimToode.getNimi() +
                    ", kogus: " + tooted.get(populaarseimToode) + "\nSisestage 'back', et naasta menüüsse");
            dout.writeInt(1);
            dout.writeUTF(sb.toString());
        }
    }
}
