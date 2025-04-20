package epood;

import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;
import failisuhtlus.JsonReader;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class CatalogueHandler implements Screen {
    private String currentSubScreen;
    private Toode toode;

    // peaks olema tegelikult kõikide klientide peale ainult 1 reader, aga panin hetkel siia, et debugida
    private final JsonReader jsonReader = new JsonReader("andmebaas.json");

    public CatalogueHandler() {
    }

    public void show(DataOutputStream dout) throws IOException {
        // näitame kõiki tooteid
        currentSubScreen = "select product";
        StringBuilder tooted = new StringBuilder();
        jsonReader.getTooted().forEach(t -> tooted.append(t.toString()).append("\n"));
        dout.writeInt(1);
        dout.writeUTF("Products: \n" +
                tooted + "\n" +
                "Enter name of the product: ");
    }

    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException {
        if (currentSubScreen.equals("select product")) {
            if (jsonReader.getTooted().stream()
                .anyMatch(t -> cmd.equalsIgnoreCase(t.getNimi()))) {
                toode = jsonReader.getTooted().stream()
                        .filter(t -> cmd.equalsIgnoreCase(t.getNimi()))
                        .findFirst()
                        .orElse(null);

                // Küsime mitu tükki soovib kasutaja antud toodet osta
                dout.writeInt(1);
                dout.writeUTF("Enter quantity: ");
                currentSubScreen = "select quantity";
        } else {
                dout.writeInt(1);
                dout.writeUTF("Invalid name:");
        }
        } else if (currentSubScreen.equals("select quantity")) {
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
                        dout.writeInt(1);
                        dout.writeUTF("Product added to the cart. \n" +
                                "Enter name of the product: ");
                    }
                    currentSubScreen = "select product";
                } else {
                    dout.writeInt(1);
                    dout.writeUTF("Please enter lower quantity: ");
                    currentSubScreen = "select quantity";
                }
            }
            catch (NumberFormatException e) {
                dout.writeInt(1);
                dout.writeUTF("Invalid quantity. Please enter a valid number.");
            }
        }
    }


}
