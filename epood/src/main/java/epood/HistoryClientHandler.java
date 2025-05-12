package epood;

import failisuhtlus.JsonManagerHistory;
import failisuhtlus.Tellimus;
import failisuhtlus.Toode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class HistoryClientHandler {
    private final JsonManagerHistory jsonManagerHistory = new JsonManagerHistory();
    public void show(DataOutputStream dout, ClientServerSide client) throws IOException {
        displayOrders(dout, client);
    }

    public void handler(DataOutputStream dout, String cmd, String[] args, ClientServerSide client) throws IOException {
        displayOrders(dout, client);
    }

    private void displayOrders(DataOutputStream dout, ClientServerSide client) throws IOException {
        List<Tellimus> tellimused = jsonManagerHistory.readJson();
        StringBuilder sb = new StringBuilder("\n----Tellimused----\n");
        if (tellimused.isEmpty()) {
            sb.append("Tellimusi veel pole!\nSisetsage 'back', et naasta menüüsse");
            dout.writeInt(1);
            dout.writeUTF(sb.toString());
        } else {
            Map<Toode, Integer> tooted = new HashMap<>();
            UUID kliendiId =  client.getId();
            for (Tellimus tellimus : tellimused) {
                if (tellimus.getKliendiID().equals(kliendiId)) {
                    sb.append(tellimus + "\n");
                    for (Map.Entry<Toode, Integer> entry : tellimus.getOstukorv().getItems().entrySet()) {
                        Toode toode = entry.getKey();
                        tooted.put(toode, tooted.getOrDefault(toode, 0) + entry.getValue());
                    }
                }
            }
            Toode populaarseimToode = Collections.max(tooted.entrySet(), Map.Entry.comparingByValue()).getKey();
            sb.append("Kõige rohkem olete tellinud toodet: " + populaarseimToode.getNimi() +
                    ", kogus: " + tooted.get(populaarseimToode) + "\nSisestage 'back', et naasta menüüsse");
            dout.writeInt(1);
            dout.writeUTF(sb.toString());
        }
    }
}
