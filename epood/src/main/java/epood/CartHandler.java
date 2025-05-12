package epood;

import failisuhtlus.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /** Toodete andmebaasi uuendamiseks **/
    private final JsonReader jsonReader = new JsonReader("andmebaas.json");

    /** Tellimuste lisamiseks **/
    private final JsonManagerHistory jsonManagerHistory = new JsonManagerHistory();

    /**
     * Kuvab ostukorvi sisu ja saadaval olevad tegevused kasutajale.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    public void show(DataOutputStream dout, ClientServerSide client) throws IOException {
        // Kuvab ostukorvi sisu ilma täiendava sõnumita
        System.out.println(client.getCart());
        displayCart(dout, null, client);
    }

    /**
     * Käitleb ostukorviga seotud kasutaja käske.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param cmd Kasutaja sisestatud käsk
     * @param args Käsu täiendavad argumendid
     * @param client Kasutaja
     * @throws IOException kui väljundi kirjutamisel tekib viga
     */
    public void handler(DataOutputStream dout, String cmd, String[] args, ClientServerSide client) throws IOException {
        switch (currentSubScreen) {
            case "view":
                handleViewCommands(dout, cmd, args, client);
                break;
            case "remove":
                handleRemoveProduct(dout, cmd, client);
                break;
            case "update_select":
                handleUpdateProductSelection(dout, cmd, client);
                break;
            case "update_quantity":
                handleUpdateQuantity(dout, cmd, client);
                break;
            case "checkout":
                handleCheckout(dout, cmd, client);
                break;
            default:
                // Tundmatu oleku korral lähtestame põhivaatele
                currentSubScreen = "view";
                displayCart(dout, "Tundmatu ostukorvi olek. Tagasi põhivaatesse.", client);
        }
    }

    /**
     * Käitleb ostukorvi põhivaates saadaolevaid käske.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param cmd Kasutaja sisestatud käsk
     * @param args Käsu täiendavad argumendid
     * @param client Klient
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleViewCommands(DataOutputStream dout, String cmd, String[] args, ClientServerSide client) throws IOException {
        switch (cmd.toLowerCase()) {
            case "update":
                if (client.getCart().getItems().isEmpty()) {
                    displayCart(dout, "Ostukorv on tühi. Pole midagi muuta.", client);
                } else {
                    currentSubScreen = "update_select";
                    dout.writeInt(1);
                    dout.writeUTF("Sisesta toote nimi, mida soovid muuta: ");
                }
                break;
            case "remove":
                if (client.getCart().getItems().isEmpty()) {
                    displayCart(dout, "Ostukorv on tühi. Pole midagi eemaldada.", client);
                } else {
                    currentSubScreen = "remove";
                    dout.writeInt(1);
                    dout.writeUTF("Sisesta toote nimi, mida soovid eemaldada: ");
                }
                break;
            case "clear":
                client.getCart().tyhjendaOstukorv();
                jsonManagerClient.updateCartJson(client);
                displayCart(dout, "Ostukorv on tühjendatud.", client);
                break;
            case "checkout":
                if (client.getCart().getItems().isEmpty()) {
                    displayCart(dout, "Ostukorv on tühi. Lisa kõigepealt tooteid!", client);
                } else {
                    /*// Tellimuse ekraanile liikumise signaal - käideldakse ClientHandleris
                    dout.writeInt(1);
                    dout.writeUTF("Liikumine tellimuse vormistamisele.");*/
                    // Lisasin checkout funktsionaalsuse, hetkel aadressi, telefoninumbriga jne ei arvesta,
                    // sest selle rakendamine on üsna sarnane nime ja emaili küsimisega ning suurt õpimomenti selles pole

                    currentSubScreen = "checkout";
                    dout.writeInt(1);
                    dout.writeUTF("Kas oled kindel, et soovid ostu sooritada? (sisetage 'jah' või 'ei')");
                }
                break;
            default:
                displayCart(dout, "Käsud: update, remove, clear, checkout", client);
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
    private void handleRemoveProduct(DataOutputStream dout, String productName, ClientServerSide client) throws IOException {
        Toode productToRemove = findProductInCart(client.getCart(), productName);

        if (productToRemove != null) {
            client.getCart().removeToode(productToRemove);
            jsonManagerClient.updateCartJson(client);
            displayCart(dout, "\"" + productName + "\" eemaldatud ostukorvist.", client);
        } else {
            displayCart(dout, "Toodet \"" + productName + "\" ei leitud ostukorvist.", client);
        }

        // Tagasi ostukorvi põhivaatesse
        currentSubScreen = "view";
    }

    /**
     * Käitleb toote valimist koguse muutmiseks.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param productName Muudetava toote nimi
     * @param client Klient
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleUpdateProductSelection(DataOutputStream dout, String productName, ClientServerSide client) throws IOException {
        productToUpdate = findProductInCart(client.getCart(), productName);

        if (productToUpdate != null) {
            // Leiame praeguse koguse
            Integer currentQuantity = 0;
            for (Map.Entry<Toode, Integer> entry : client.getCart().getItems().entrySet()) {
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
            displayCart(dout, "Toodet \"" + productName + "\" ei leitud ostukorvist.", client);
            currentSubScreen = "view";
        }
    }

    /**
     * Käitleb toote koguse uuendamist.
     *
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param quantityStr Uue koguse string
     * @param client Klient
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void handleUpdateQuantity(DataOutputStream dout, String quantityStr, ClientServerSide client) throws IOException {
        try {
            int newQuantity = Integer.parseInt(quantityStr);

            if (newQuantity <= 0) {
                // Kui kogus on 0 või negatiivne, eemaldame toote
                client.getCart().removeToode(productToUpdate);
                jsonManagerClient.updateCartJson(client);
                displayCart(dout, "Toode eemaldatud ostukorvist, kuna uus kogus oli " + newQuantity + ".", client);
            } else {
                // Kontrollime, kas laos on piisavalt
                if (productToUpdate.getLao_seis() >= newQuantity) {
                    // Eemaldame esmalt toote, et vältida koguste liitmist
                    client.getCart().removeToode(productToUpdate);
                    // Seejärel lisame uue kogusega
                    client.getCart().addToode(productToUpdate, newQuantity);
                    jsonManagerClient.updateCartJson(client);
                    displayCart(dout, "Toote \"" + productToUpdate.getNimi() + "\" kogus muudetud: " + newQuantity + ".", client);
                } else {
                    displayCart(dout, "Viga: laos on ainult " + productToUpdate.getLao_seis() +
                            " toodet. Palun valige väiksem kogus.", client);
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
     * Väljakutsel kontrollitakse, kas laos on vähemalt sama palju toodeid, kui kasutajal korvis.
     * Kui ei, siis kas vähendatakse kogust ning kasutajat teavitatakse sellest.
     * @param dout DataOutputStream kliendile andmete saatmiseks
     * @param message Lisasõnum, mis kuvatakse (võib olla null)
     * @throws IOException kui väljundvoolu kirjutamisel tekib viga
     */
    private void displayCart(DataOutputStream dout, String message, ClientServerSide client) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("----- OSTUKORV -----\n");

        if (message != null && !message.isEmpty()) {
            sb.append(message).append("\n\n");
        }

        Map<Toode, Integer> items = client.getCart().getItems();
        if (items.isEmpty()) {
            sb.append("Ostukorv on tühi.\n");
        } else {
            List<Toode> tooted = jsonReader.getTooted();

            sb.append("Tooted ostukorvis:\n");
            for (Map.Entry<Toode, Integer> entry : items.entrySet()) {
                Toode product = entry.getKey();
                Integer quantity = entry.getValue();

                Optional<Toode> result = tooted.stream()
                        .filter(r -> r.getNumber() == product.getNumber())
                        .findFirst();

                if (!result.isPresent() || result.get().getLao_seis() <= 0) {// toode on kustutatud või otsas
                    sb.append("Kahjuks on antud toode otsa saanud: " + product.getNimi() + "\n");
                    client.getCart().removeToode(product);
                    jsonManagerClient.updateCartJson(client);
                    continue;
                } else {
                    int laoSeis = result.get().getLao_seis();
                    if (laoSeis < quantity) { // vähendame kogust
                        client.getCart().addLowerQuantity(product, laoSeis);
                        quantity = laoSeis;
                        sb.append("Kogus vähenenud! - ");
                        jsonManagerClient.updateCartJson(client);
                        System.out.println(client.getCart());
                    }
                }

                BigDecimal itemTotal = product.getHind().multiply(BigDecimal.valueOf(quantity));

                sb.append(String.format("%s x%d = %.2f EUR\n",
                        product.getNimi(), quantity, itemTotal));
            }
            sb.append(String.format("\nKokku: %.2f EUR\n", client.getCart().getKoguHind()));
        }

        sb.append("\nSaadaval käsud: update (muuda kogust), remove (eemalda toode), clear (tühjenda ostukorv), checkout (vormista tellimus)");

        dout.writeInt(1);
        dout.writeUTF(sb.toString());
    }

    private void handleCheckout(DataOutputStream dout, String cmd, ClientServerSide client) throws IOException {
        if (cmd.equals("ei")) {
            displayCart(dout, "Tellimust ei sooritatud", client );
            currentSubScreen = "view";
        } else if (cmd.equals("jah")) {
            // täname ostjat, uuendame laoseisu, lisame tellimuse andmebaasi, tühjendame ostukorvi, saadame emaili

            for (Map.Entry<Toode, Integer> entry : client.getCart().getItems().entrySet()) {
                jsonReader.vahendaKogust(entry.getKey().getNumber(), entry.getValue());
            }

            jsonManagerHistory.addTellimusJson(new Tellimus(client.getName(), client.getEmail(), client.getCart(), client.getId()));
            client.getCart().tyhjendaOstukorv();
            jsonManagerClient.updateCartJson(client);


            /**
            Siia tuleb kliendile ja ühele töötajatest emaili saatmine.
             Töötajate emailid on kirjas tootajateAndmebaasis
            **/

            currentSubScreen = "view";
            dout.writeInt(1);
            dout.writeUTF("Aitäh ostu eest! Arve saadetakse emailile!" +
                    "\nsisestage 'back', et naasta peamenüüsse ");
        } else {
            dout.writeInt(1);
            dout.writeUTF("Palun sisestage 'jah' või 'ei'.");
            currentSubScreen = "checkout";
        }

    }
}