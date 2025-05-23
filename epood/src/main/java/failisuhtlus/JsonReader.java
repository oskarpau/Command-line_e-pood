package failisuhtlus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {
    private String fail;

    public JsonReader(String fail) {
        this.fail = fail;
    }


    public List<Toode> getTooted() throws IOException {

        try {
            List<Toode> tooted = new ArrayList<>();

            JSONObject json = getJSON();

            JSONArray tootedArray = json.getJSONArray("tooted");

            for (int i = 0; i < tootedArray.length(); i++) {
                JSONObject toode = tootedArray.getJSONObject(i);
                tooted.add(new Toode(toode.getInt("tootenumber"), toode.getString("nimi"), BigDecimal.valueOf(toode.getDouble("hind")), toode.getInt("lao seis")));
            }
            return tooted;
        } catch (NoSuchFileException e) {
            System.out.println("FAILI EI LEITUD!! VÕTAME HARDCODED VÄÄRTUSED");
            Toode õun = new Toode(1, "õun", BigDecimal.valueOf(0.39), 54);
            Toode pirn = new Toode(2, "pirn", BigDecimal.valueOf(0.59), 32);
            List<Toode> tooted = new ArrayList<>();
            tooted.add(õun);
            tooted.add(pirn);
            return tooted;

        }


    }

    public void addToode(Toode toode) throws IOException {

        JSONObject json = getJSON();

        JSONArray tootedArray = json.getJSONArray("tooted");

        JSONObject toodeJson = new JSONObject();
        toodeJson.put("tootenumber", toode.getNumber());
        toodeJson.put("nimi", toode.getNimi());
        toodeJson.put("hind", toode.getHind());
        toodeJson.put("lao seis", toode.getLao_seis());

        tootedArray.put(toodeJson);

        updateJson(json);
    }

    public void muudaKogust(int tootenumber, int uusKogus) throws IOException {
        JSONObject json = getJSON();

        JSONArray tootedArray = json.getJSONArray("tooted");

        for (int i = 0; i < tootedArray.length(); i++) {
            JSONObject toode = tootedArray.getJSONObject(i);
            if (toode.getInt("tootenumber") == tootenumber) {
                tootedArray.getJSONObject(i).put("lao seis", uusKogus);
                updateJson(json);
                return;
            }
        }
    }

    private JSONObject getJSON() throws IOException {
        String content;
        content = new String(Files.readAllBytes(Paths.get("andmebaas.json")));

        return new JSONObject(content);
    }

    private void updateJson(JSONObject json) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fail)) {
            fileWriter.write(json.toString());
        }
    }

    public void vahendaKogust(int tootenumber, int vahendamiseKogus) throws IOException {
        JSONObject json = getJSON();

        JSONArray tootedArray = json.getJSONArray("tooted");

        for (int i = 0; i < tootedArray.length(); i++) {
            JSONObject toode = tootedArray.getJSONObject(i);
            if (toode.getInt("tootenumber") == tootenumber) {
                int vanaKogus = toode.getInt("lao seis");
                tootedArray.getJSONObject(i).put("lao seis", vanaKogus - vahendamiseKogus);
                updateJson(json);
                return;
            }
        }
    }
}
