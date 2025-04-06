package epood;

import failisuhtlus.JsonReader;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class SearchHandler implements Screen {

    // peaks olema tegelikult kõikide klientide peale ainult 1 reader, aga panin hetkel siia, et debugida
    private final JsonReader jsonReader = new JsonReader("andmebaas.json");

    public void show(DataOutputStream dout, String search) throws IOException {
        System.out.println("Searching " + search);
        List<Toode> tooted = jsonReader.getTooted();
        if (!tooted.isEmpty()) {
            dout.writeInt(tooted.size());
            for (Toode t : tooted) {
                dout.writeUTF(t.getNimi());
            }
        }
    }
    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException {
        // todo
    }

}
