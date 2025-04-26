package epood;

import failisuhtlus.JsonReader;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SearchHandler implements Screen {

    // peaks olema tegelikult kõikide klientide peale ainult 1 reader, aga panin hetkel siia, et debugida
    private final JsonReader jsonReader = new JsonReader("andmebaas.json");
    private String currentSubScreen;

    public void show(DataOutputStream dout) throws IOException {
        currentSubScreen = "product search";
        dout.writeInt(1);
        dout.writeUTF("Enter product name: ");
    }

    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException {
        // todo
        if (currentSubScreen.equals("product search")) {
            // otsib sisestatud toodet ja tagastab
            List<Toode> tooted = jsonReader.getTooted().stream()
                    .filter(t -> t.getNimi() != null && t.getNimi().toLowerCase().contains(cmd.toLowerCase()))
                    .collect(Collectors.toList());
            if (!tooted.isEmpty()) {
                dout.writeInt(tooted.size() + 2);
                dout.writeUTF("Products found:");

                for (Toode t : tooted) {
                    dout.writeUTF(t.getNimi());
                }
                dout.writeUTF("Sisestage toote nimi, mida soovite lisada ostukorvi: ");
                currentSubScreen = "select product";
            } else {
                dout.writeInt(1);
                dout.writeUTF("Ühtki toodet ei leitud: ");
            }

        } else if (currentSubScreen.equals("select product")) {
            // todo
            System.out.println("Select product");
        }
    }
}
