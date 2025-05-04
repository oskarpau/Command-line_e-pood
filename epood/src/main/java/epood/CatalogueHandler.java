package epood;

import failisuhtlus.JsonManagerEmployee;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;
import failisuhtlus.JsonReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class CatalogueHandler {
    private byte currentSubScreen;
    private Toode toode;
    private static final byte SELECT_PRODUCT = 1;
    private static final byte SELECT_QUANTITY = 2;
    private static final byte CHANGE_QUANTITY = 3;

    // peaks olema tegelikult kõikide klientide peale ainult 1 reader, aga panin hetkel siia, et debugida
    private final JsonReader jsonReader = new JsonReader("andmebaas.json");

    public CatalogueHandler() {
    }

    public void show(DataOutputStream dout, Byte type) throws IOException {
        showAll(dout, type);
    }

    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart, Byte type) throws IOException {
        if (currentSubScreen == SELECT_PRODUCT) {
            if (jsonReader.getTooted().stream()
                .anyMatch(t -> cmd.equalsIgnoreCase(t.getNimi()))) {
                toode = jsonReader.getTooted().stream()
                        .filter(t -> cmd.equalsIgnoreCase(t.getNimi()))
                        .findFirst()
                        .orElse(null);

                // Küsime mitu tükki soovib kasutaja antud toodet osta
                dout.writeInt(1);
                if (type == Config.CLIENT) {
                    dout.writeUTF("Enter quantity: ");
                    currentSubScreen = SELECT_QUANTITY;
                } else if (type == Config.EMPLOYEE) {
                    dout.writeUTF("Enter new quantity: ");
                    currentSubScreen = CHANGE_QUANTITY;
                }
        } else {
                dout.writeInt(1);
                dout.writeUTF("Invalid name:");
        }
        } else if (currentSubScreen == SELECT_QUANTITY) {
            // vaatame, kas on meil piisavalt
            try {
                int quantity = Integer.parseInt(cmd);
                // vaatame, kui palju meil antud toodet juba ostukorvis on
                Toode inCart = null;
                Integer quantityInCart = 0;
                for (Map.Entry<Toode, Integer> entry : cart.getItems().entrySet()) {
                    String name = entry.getKey().getNimi();
                    if (name.equals(toode.getNimi())) {
                        quantityInCart = entry.getValue();
                    }
                }

                if (toode.getLao_seis() >= (quantity + quantityInCart)) {
                    if (quantity <= 0) {
                        dout.writeInt(1);
                        dout.writeUTF("Please enter positive quantity.\nNo product added to the cart.");
                    } else {
                        cart.addToode(toode, quantity);
                        showAll(dout, type);
                    }
                    currentSubScreen = SELECT_PRODUCT;
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Please enter lower quantity: ");
                    currentSubScreen = SELECT_QUANTITY;
                }
            }
            catch (NumberFormatException e) {
                dout.writeInt(1);
                dout.writeUTF("Invalid quantity. Please enter a valid integer.");
            }
        } else if (currentSubScreen == CHANGE_QUANTITY) {
            try {
                int quantity = Integer.parseInt(cmd);
                if (quantity < 0) {
                    dout.writeInt(1);
                    dout.writeUTF("Please enter positive quantity.");
                    return;
                }
                // muudame kogust failis
                // teeme praegu nii, et kui muudame koguses 0-ks, siis jääb toode ikka alles, et ei peaks arenduse käigus
                // iga kord hakkama uut toodet lisama, kui varem sai toode kustutatud
                jsonReader.muudaKogust(toode.getNumber(), quantity);
                showAll(dout, type);
            }
            catch (NumberFormatException e) {
                dout.writeInt(1);
                dout.writeUTF("Invalid quantity. Please enter a valid integer.");
            }
        }
    }

    private void showAll(DataOutputStream dout, Byte type) throws IOException {
        // näitame kõiki tooteid
        currentSubScreen = SELECT_PRODUCT;
        StringBuilder tooted = new StringBuilder();
        jsonReader.getTooted().forEach(t -> tooted.append(t.toString()).append("\n"));
        dout.writeInt(1);
        if (type == Config.CLIENT) {
            dout.writeUTF("Products: \n" +
                    tooted + "\n" +
                    "Enter name of the product:\nsisestage 'back', et naasta peamenüüsse");
        } else if (type == Config.EMPLOYEE) {
            dout.writeUTF("Products: \n" +
                    tooted + "\n" +
                    "Enter name of the product to change quantity:\nsisestage 'back', et naasta peamenüüsse ");
        }
    }
}
