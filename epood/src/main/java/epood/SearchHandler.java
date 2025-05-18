package epood;

import failisuhtlus.JsonManagerClient;
import failisuhtlus.JsonReader;
import failisuhtlus.Ostukorv;
import failisuhtlus.Toode; // Eeldame, et see on korrektne import

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Käitleb toodete otsimist ja ostukorvi lisamist e-poe serveri poolel.
 * See klass võimaldab kasutajatel otsida tooteid nime järgi, kuvada otsingu tulemusi,
 * valida tooteid ja lisada neid ostukorvi. Klass haldab sisemist olekut
 * otsinguprotsessi erinevate etappide jaoks: otsing, toote valimine ja koguse sisestamine.
 */
public class SearchHandler {

    /** Toodete andmebaasi lugemiseks, initsialiseeritakse kohe. */
    private final JsonReader jsonReader = new JsonReader("andmebaas.json");

    /** Alamekraani konstant: toodete otsingu faas. */
    private static final byte PRODUCT_SEARCH = 1;
    /** Alamekraani konstant: toote valimise faas otsingutulemustest. */
    private static final byte SELECT_PRODUCT = 2;
    /** Alamekraani konstant: koguse valimise faas. */
    private static final byte SELECT_QUANTITY = 3;

    /** Jälgib, millises otsingufaasis (alamekraanil) kasutaja on. */
    private byte currentSubScreen;
    /** Valitud toode, kui kasutaja on jõudnud toote valimise faasi. */
    private Toode selectedProduct;
    /** Otsingutulemused, mida kuvatakse kasutajale. */
    private List<Toode> searchResults;
    /** Kliendi andmete (sh ostukorvi) haldamiseks JSON formaadis. */
    private JsonManagerClient jsonManagerClient;

    /**
     * Klassi konstruktor.
     * Initsialiseerib otsingu algoleku ja {@link JsonManagerClient}.
     * Eeldab, et {@link JsonManagerClient} klassil on argumentideta konstruktor.
     */
    public SearchHandler() {
        this.currentSubScreen = PRODUCT_SEARCH;
        this.jsonManagerClient = new JsonManagerClient();
    }

    /**
     * Kuvab esialgse toodete otsingu viiba kliendile.
     * Seab sisemise oleku toodete otsingu faasi.
     *
     * @param dout DataOutputStream, mille kaudu kliendile andmeid saata.
     * @throws IOException Kui andmete saatmisel tekib I/O viga.
     */
    public void show(DataOutputStream dout) throws IOException {
        currentSubScreen = PRODUCT_SEARCH;
        dout.writeInt(1); // Sõnumi tüüp/kood kliendile
        dout.writeUTF("Toodete otsing\n\nSisestage otsitava toote nimi: \nsisestage 'back', et naasta peamenüüsse");
    }

    /**
     * Peamine käskude käsitleja, mis töötab {@link ClientServerSide} objektiga.
     * Juhib kasutaja interaktsiooni vastavalt praegusele alamekraanile (otsing, valik, kogus).
     *
     * @param dout DataOutputStream kliendile vastuste saatmiseks.
     * @param cmd Kasutaja sisestatud käsk (nt otsingusõna, toote nimi, kogus).
     * @param args Käsu täiendavad argumendid (praegu ei kasutata).
     * @param client Kliendi serveripoolne esindus, kelle ostukorviga tegeletakse.
     * @throws IOException Kui andmete saatmisel tekib I/O viga.
     */
    public void handler(DataOutputStream dout, String cmd, String[] args, ClientServerSide client) throws IOException {
        switch (currentSubScreen) {
            case PRODUCT_SEARCH:
                handleProductSearch(dout, cmd, client);
                break;
            case SELECT_PRODUCT:
                handleProductSelection(dout, cmd, client);
                break;
            case SELECT_QUANTITY:
                handleQuantitySelection(dout, cmd, client);
                break;
            default:
                // Tundmatu oleku korral suuna tagasi algusesse
                show(dout);
        }
    }

    /**
     * Käitleb toodete otsimist vastavalt kasutaja sisestatud otsingusõnale.
     * Otsib tooteid andmebaasist (case-insensitive) ja kuvab tulemused.
     *
     * @param dout DataOutputStream kliendile vastuste saatmiseks.
     * @param searchTerm Kasutaja sisestatud otsingusõna.
     * @param client Kliendi serveripoolne esindus (selles meetodis hetkel otseselt ei kasutata,
     * kuid on olemas järjepidevuse huvides teiste käsitlejatega).
     * @throws IOException Kui andmete saatmisel tekib I/O viga.
     */
    private void handleProductSearch(DataOutputStream dout, String searchTerm, ClientServerSide client) throws IOException {
        searchResults = jsonReader.getTooted().stream()
                .filter(t -> t.getNimi() != null && t.getNimi().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());

        if (!searchResults.isEmpty()) {
            StringBuilder resultOutput = new StringBuilder("Leitud tooted:\n\n");
            for (Toode product : searchResults) {
                resultOutput.append(product.toString()).append("\n");
            }
            resultOutput.append("\nSisestage toote nimi, mida soovite lisada ostukorvi: \nsisestage 'search', et teha uus otsing\nsisestage 'back', et naasta peamenüüsse");
            dout.writeInt(1);
            dout.writeUTF(resultOutput.toString());
            currentSubScreen = SELECT_PRODUCT;
        } else {
            dout.writeInt(1);
            dout.writeUTF("Ühtegi toodet ei leitud.\n\nProovige teist otsingusõna: \nsisestage 'back', et naasta peamenüüsse");
            // Jää otsingurežiimi uueks katseks
        }
    }

    /**
     * Käitleb toote valimist otsingutulemuste hulgast.
     * Kontrollib, kas valitud toode on laos ja liigub koguse sisestamise faasi.
     *
     * @param dout DataOutputStream kliendile vastuste saatmiseks.
     * @param productName Kasutaja valitud toote nimi.
     * @param client Kliendi serveripoolne esindus (selles meetodis hetkel otseselt ei kasutata).
     * @throws IOException Kui andmete saatmisel tekib I/O viga.
     */
    private void handleProductSelection(DataOutputStream dout, String productName, ClientServerSide client) throws IOException {
        if (productName.equalsIgnoreCase("search")) {
            show(dout); // Naase otsingu algusesse
            return;
        }

        selectedProduct = searchResults.stream()
                .filter(t -> t.getNimi().equalsIgnoreCase(productName))
                .findFirst()
                .orElse(null);

        if (selectedProduct != null) {
            if (selectedProduct.getLao_seis() <= 0) {
                dout.writeInt(1);
                dout.writeUTF("Kahjuks on valitud toode \"" + selectedProduct.getNimi() + "\" laost otsas.\n\n" +
                        "Valige teine toode või otsige midagi muud: \nsisestage 'search', et teha uus otsing\nsisestage 'back', et naasta peamenüüsse");
                currentSubScreen = SELECT_PRODUCT; // Jää toote valimise faasi
            } else {
                dout.writeInt(1);
                dout.writeUTF("Valitud toode: " + selectedProduct.getNimi() + "\n" +
                        "Laos: " + selectedProduct.getLao_seis() + "\n\n" +
                        "Sisestage soovitud kogus: ");
                currentSubScreen = SELECT_QUANTITY; // Liigu koguse valimise faasi
            }
        } else {
            dout.writeInt(1);
            dout.writeUTF("Toodet nimega \"" + productName + "\" ei leitud otsingutulemuste hulgast.\n" +
                    "Palun sisestage toote täpne nimi otsingutulemuste hulgast: \nsisestage 'search', et teha uus otsing\nsisestage 'back', et naasta peamenüüsse");
            currentSubScreen = SELECT_PRODUCT; // Jää toote valimise faasi
        }
    }

    /**
     * Käitleb valitud tootele koguse sisestamist ja ostukorvi lisamist.
     * Kontrollib koguse korrektsust ja toote saadavust laos.
     *
     * @param dout DataOutputStream kliendile vastuste saatmiseks.
     * @param quantityStr Kasutaja sisestatud kogus sõnena.
     * @param client Kliendi serveripoolne esindus, kelle ostukorvi toode lisatakse.
     * @throws IOException Kui andmete saatmisel tekib I/O viga.
     */
    private void handleQuantitySelection(DataOutputStream dout, String quantityStr, ClientServerSide client) throws IOException {
        try {
            int quantity = Integer.parseInt(quantityStr);
            Ostukorv cart = client.getCart();
            int currentInCart = 0;

            for (Map.Entry<Toode, Integer> entry : cart.getItems().entrySet()) {
                if (entry.getKey().getNumber() == selectedProduct.getNumber()) {
                    currentInCart = entry.getValue();
                    break;
                }
            }

            if (quantity <= 0) {
                dout.writeInt(1);
                dout.writeUTF("Palun sisestage positiivne kogus.\nÜhtegi toodet ostukorvi ei lisatud.\n\n" +
                        "Sisestage soovitud kogus: ");
                return; // Jää koguse valimise faasi
            }

            if (selectedProduct.getLao_seis() >= (quantity + currentInCart)) {
                client.addToode(selectedProduct, quantity, jsonManagerClient); // Lisa toode ostukorvi
                dout.writeInt(1);
                dout.writeUTF("Toode \"" + selectedProduct.getNimi() + "\" lisatud ostukorvi koguses " + quantity + ".\n\n" +
                        "Valige mõni muu toode otsingutulemuste hulgast või\nsisestage 'search', et teha uus otsing\nsisestage 'back', et naasta peamenüüsse");
                currentSubScreen = SELECT_PRODUCT; // Naase toodete valikusse (praegustest otsingutulemustest)
            } else {
                dout.writeInt(1);
                dout.writeUTF("Laos pole piisavalt tooteid. Maksimaalne kogus, mida saate lisada: " +
                        (selectedProduct.getLao_seis() - currentInCart) + ".\n\n" +
                        "Sisestage väiksem kogus: ");
                // Jää koguse valimise faasi
            }
        } catch (NumberFormatException e) {
            dout.writeInt(1);
            dout.writeUTF("Vigane kogus. Palun sisestage number: ");
            // Jää koguse valimise faasi
        }
    }

    /*
    /**
     * Vanem käsitleja meetod, säilitatud tagasiühilduvuse jaoks.
     * See meetod on mõeldud olukordadeks, kus {@link ClientServerSide} objekti asemel
     * kasutatakse otse {@link Ostukorv} objekti.
     * <p>
     * Kui see meetod kutsutakse, täidab see pärandloogikat toodete otsimiseks.
     * Toote valimise (SELECT_PRODUCT) faasis pärandkäsitleja teatab, et funktsionaalsus
     * pole täielikult toetatud ja soovitab kasutada {@code ClientServerSide} versiooni.
     * </p>
     * @param dout DataOutputStream kliendile vastuste saatmiseks.
     * @param cmd Kasutaja sisestatud käsk.
     * @param args Käsu täiendavad argumendid.
     * @param cart Ostukorv, mida kasutatakse pärandrežiimis.
     * @throws IOException Kui andmete saatmisel tekib I/O viga.
     */
    /*
    @Deprecated
    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException {
        // Pärandkäitumine: See meetod ei kasuta this.client ega delegeeri uuele handlerile.
        // See on eraldiseisev pärandloogika.

        if (currentSubScreen == PRODUCT_SEARCH) {
            // Pärandotsingu käitumine
            List<Toode> tooted = jsonReader.getTooted().stream()
                    .filter(t -> t.getNimi() != null && t.getNimi().toLowerCase().contains(cmd.toLowerCase()))
                    .collect(Collectors.toList());
            if (!tooted.isEmpty()) {
                // Salvesta otsingutulemused, et neid saaks potentsiaalselt pärand-SELECT_PRODUCT faasis kasutada
                this.searchResults = tooted;
                dout.writeInt(tooted.size() + 2); // Protokolli osa: mitu rida saadetakse
                dout.writeUTF("Products found (legacy):");
                for (Toode t : tooted) {
                    dout.writeUTF(t.getNimi()); // Saadab iga toote nime eraldi reana
                }
                dout.writeUTF("Sisestage toote nimi, mida soovite lisada ostukorvi (legacy): ");
                currentSubScreen = SELECT_PRODUCT;
            } else {
                dout.writeInt(1);
                dout.writeUTF("Ühtki toodet ei leitud (legacy). Proovige uut otsingut: ");
            }
        } else if (currentSubScreen == SELECT_PRODUCT) {
            dout.writeInt(1);
            dout.writeUTF("Legacy SELECT_PRODUCT: Funktsionaalsus on piiratud. " +
                    "Toote lisamiseks kasutage uuemat ClientServerSide handlerit või täiendage seda pärandmeetodit.");
        }
    }
    */
}