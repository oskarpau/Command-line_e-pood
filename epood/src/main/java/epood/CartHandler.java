package epood;

import failisuhtlus.JsonManagerClient;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Käitleb kõiki ostukorviga seotud operatsioone e-poes.
 * Vastutab ostukorvi sisu kuvamise, toodete koguste muutmise,
 * toodete eemaldamise ja ostukorvi tühjendamise eest.
 */
public class CartHandler {
    /** Jälgib praegust alamekraani olekut ostukorvi liideses */
    private String currentSubScreen = "view";

    /** Salvestab toote, mille kogust muudetakse */
    private Toode productToUpdate;

    /** Kasutaja ostukorvi salvestamiseks */
    private final JsonManagerClient jsonManagerClient = new JsonManagerClient();

    /**
     * Kuvab ostukorvi sisu ja saadaval olevad tegevused kasutajale.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    public void show(DataOutputStream dout, Ostukorv korv) throws IOException {
        // Kuvab ostukorvi sisu ilma täiendava sõnumita
        displayCart(dout, null, korv);
    }

    /**
     * Käitleb ostukorviga seotud kasutaja käske.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param cmd Kasutaja sisestatud käsk
     * @param args Käsu täiendavad argumendid
     * @param korv Kasutaja ostukorv
     * @throws IOException kui väljundi kirjutamisel tekib viga
     */
    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv korv, ClientServerSide client) throws IOException {
        switch (currentSubScreen) {
            case "view":
                handleViewCommands(dout, cmd, args, korv, client);
                break;
            case "remove":
                handleRemoveProduct(dout, cmd, korv, client);
                break;
            case "update_select":
                handleUpdateProductSelection(dout, cmd, korv);
                break;
            case "update_quantity":
                handleUpdateQuantity(dout, cmd, korv, client);
                break;
            default:
                // Tundmatu oleku korral lähtestame põhivaatele
                currentSubScreen = "view";
                displayCart(dout, "Tundmatu ostukorvi olek. Tagasi põhivaatesse.", korv);
        }
    }

    /**
     * Käitleb ostukorvi põhivaates saadaolevaid käske.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param cmd Kasutaja sisestatud käsk
     * @param args Käsu täiendavad argumendid
     * @param korv Kasutaja ostukorv
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleViewCommands(DataOutputStream dout, String cmd, String[] args, Ostukorv korv, ClientServerSide client) throws IOException {
        switch (cmd.toLowerCase()) {
            case "update":
                if (korv.getItems().isEmpty()) {
                    displayCart(dout, "Ostukorv on tühi. Pole midagi muuta.", korv);
                } else {
                    currentSubScreen = "update_select";
                    dout.writeInt(1);
                    dout.writeUTF("Sisesta toote nimi, mida soovid muuta: ");
                }
                break;
            case "remove":
                if (korv.getItems().isEmpty()) {
                    displayCart(dout, "Ostukorv on tühi. Pole midagi eemaldada.", korv);
                } else {
                    currentSubScreen = "remove";
                    dout.writeInt(1);
                    dout.writeUTF("Sisesta toote nimi, mida soovid eemaldada: ");
                }
                break;
            case "clear":
                korv.tyhjendaOstukorv();
                jsonManagerClient.updateCartJson(client);
                displayCart(dout, "Ostukorv on tühjendatud.", korv);
                break;
            case "checkout":
                if (korv.getItems().isEmpty()) {
                    displayCart(dout, "Ostukorv on tühi. Lisa kõigepealt tooteid!", korv);
                } else {
                    // Tellimuse ekraanile liikumise signaal - käideldakse ClientHandleris
                    dout.writeInt(1);
                    dout.writeUTF("Liikumine tellimuse vormistamisele.");
                }
                break;
            default:
                displayCart(dout, "Käsud: update, remove, clear, checkout", korv);
        }
    }

    /**
     * Käitleb toote eemaldamist ostukorvist.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param productName Eemaldatava toote nimi
     * @param cart Kasutaja ostukorv
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleRemoveProduct(DataOutputStream dout, String productName, Ostukorv cart, ClientServerSide client) throws IOException {
        Toode productToRemove = findProductInCart(cart, productName);

        if (productToRemove != null) {
            cart.removeToode(productToRemove);
            jsonManagerClient.updateCartJson(client);
            displayCart(dout, "\"" + productName + "\" eemaldatud ostukorvist.", cart);
        } else {
            displayCart(dout, "Toodet \"" + productName + "\" ei leitud ostukorvist.", cart);
        }

        // Tagasi ostukorvi põhivaatesse
        currentSubScreen = "view";
    }

    /**
     * Käitleb toote valimist koguse muutmiseks.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param productName Muudetava toote nimi
     * @param korv Kasutaja ostukorv
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleUpdateProductSelection(DataOutputStream dout, String productName, Ostukorv korv) throws IOException {
        productToUpdate = findProductInCart(korv, productName);

        if (productToUpdate != null) {
            // Leiame praeguse koguse
            Integer currentQuantity = 0;
            for (Map.Entry<Toode, Integer> entry : korv.getItems().entrySet()) {
                if (entry.getKey().getNimi().equalsIgnoreCase(productName)) {
                    currentQuantity = entry.getValue();
                    break;
                }
            }

            currentSubScreen = "update_quantity";
            dout.writeInt(1);
            dout.writeUTF("Praegune kogus: " + currentQuantity + "\n" +
                    "Sisesta uus kogus tootele \"" + productName + "\": ");
        } else {
            displayCart(dout, "Toodet \"" + productName + "\" ei leitud ostukorvist.", korv);
            currentSubScreen = "view";
        }
    }

    /**
     * Käitleb toote koguse uuendamist.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param quantityStr Uue koguse string
     * @param korv Kasutaja ostukorv
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleUpdateQuantity(DataOutputStream dout, String quantityStr, Ostukorv korv, ClientServerSide client) throws IOException {
        try {
            int newQuantity = Integer.parseInt(quantityStr);

            if (newQuantity <= 0) {
                // Kui kogus on 0 või negatiivne, eemaldame toote
                korv.removeToode(productToUpdate);
                jsonManagerClient.updateCartJson(client);
                displayCart(dout, "Toode eemaldatud ostukorvist, kuna uus kogus oli " + newQuantity + ".", korv);
            } else {
                // Kontrollime, kas laos on piisavalt
                if (productToUpdate.getLao_seis() >= newQuantity) {
                    // Eemaldame esmalt toote, et vältida koguste liitmist
                    korv.removeToode(productToUpdate);
                    // Seejärel lisame uue kogusega
                    korv.addToode(productToUpdate, newQuantity);
                    jsonManagerClient.updateCartJson(client);
                    displayCart(dout, "Toote \"" + productToUpdate.getNimi() + "\" kogus muudetud: " + newQuantity + ".", korv);
                } else {
                    displayCart(dout, "Viga: laos on ainult " + productToUpdate.getLao_seis() +
                            " toodet. Palun valige väiksem kogus.", korv);
                }
            }
        } catch (NumberFormatException e) {
            dout.writeInt(1);
            dout.writeUTF("Vigane kogus. Palun sisestage number: ");
            return; // Jääme samasse alamolekusse, et kasutaja saaks uuesti proovida
        }

        // Lähtestame muutuja ja naaseme põhivaatesse
        productToUpdate = null;
        currentSubScreen = "view";
    }

    /**
     * Otsib toote ostukorvist nime järgi.
     *
     * @param korv Kasutaja ostukorv
     * @param productName Otsitava toote nimi
     * @return Leitud toode või null, kui toodet ei leitud
     */
    private Toode findProductInCart(Ostukorv korv, String productName) {
        for (Map.Entry<Toode, Integer> entry : korv.getItems().entrySet()) {
            if (entry.getKey().getNimi().equalsIgnoreCase(productName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Kuvab ostukorvi sisu ja lisasõnumi.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param message Lisasõnum, mis kuvatakse (võib olla null)
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void displayCart(DataOutputStream dout, String message, Ostukorv korv) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("----- OSTUKORV -----\n");

        if (message != null && !message.isEmpty()) {
            sb.append(message).append("\n\n");
        }

        Map<Toode, Integer> items = korv.getItems();
        if (items.isEmpty()) {
            sb.append("Ostukorv on tühi.\n");
        } else {
            sb.append("Tooted ostukorvis:\n");
            for (Map.Entry<Toode, Integer> entry : items.entrySet()) {
                Toode product = entry.getKey();
                Integer quantity = entry.getValue();
                BigDecimal itemTotal = product.getHind().multiply(BigDecimal.valueOf(quantity));

                sb.append(String.format("%s x%d = %.2f EUR\n",
                        product.getNimi(), quantity, itemTotal));
            }
            sb.append(String.format("\nKokku: %.2f EUR\n", korv.getKoguHind()));
        }

        sb.append("\nSaadaval käsud: update (muuda kogust), remove (eemalda toode), clear (tühjenda ostukorv), checkout (vormista tellimus)");

        dout.writeInt(1);
        dout.writeUTF(sb.toString());
    }
}