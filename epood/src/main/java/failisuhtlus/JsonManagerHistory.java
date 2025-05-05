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
import java.util.Map;

public class JsonManagerHistory {
    private static final String FAIL = "ostudeAjalugu.json";

    public List<Tellimus> getTellimused() throws IOException {

        List<Tellimus> tellimused = new ArrayList<>();

        JSONObject json = getJSON();

        JSONArray tellimusedArray = json.getJSONArray("tellimused");

        for (int i = 0; i < tellimusedArray.length(); i++) {
            JSONObject tellimus = tellimusedArray.getJSONObject(i);
            JSONArray tootedArray = tellimus.getJSONArray("tooted");
            Ostukorv ostukorv = new Ostukorv();
            for (int j = 0; j < tootedArray.length(); j++) {
                JSONObject toode = tootedArray.getJSONObject(j);
                ostukorv.addToode(new Toode(toode.getInt("tootenumber"), toode.getString("nimi"), BigDecimal.valueOf(toode.getDouble("hind")), toode.getInt("lao seis")), toode.getInt("kogus"));
            }

            tellimused.add(new Tellimus(tellimus.getString("nimi"), tellimus.getString("email"), ostukorv));

        }
        return tellimused;

    }

    public void addTellimus(Tellimus tellimus) throws IOException {
        JSONObject json = getJSON();

        JSONArray tellimusedArray = json.getJSONArray("tellimused");
        JSONObject tellimusJson = new JSONObject();
        tellimusJson.put("nimi", tellimus.getNimi());
        tellimusJson.put("email", tellimus.getEmail());
        for (Map.Entry<Toode, Integer> entry : tellimus.getOstukorv().getItems().entrySet()) {
            JSONArray tootedArray = json.getJSONArray("tooted");

            JSONObject toodeJson = new JSONObject();
            toodeJson.put("tootenumber", entry.getKey().getNumber());
            toodeJson.put("nimi", entry.getKey().getNimi());
            toodeJson.put("hind", entry.getKey().getHind());
            toodeJson.put("lao seis", entry.getKey().getLao_seis());
            toodeJson.put("kogus", entry.getValue());
            tootedArray.put(toodeJson);
        }

        tellimusedArray.put(tellimusJson);
        updateJson(json);
    }

    private JSONObject getJSON() throws IOException {
        String content;
        content = new String(Files.readAllBytes(Paths.get("andmebaas.json")));

        return new JSONObject(content);
    }

    private void updateJson(JSONObject json) throws IOException {
        try (FileWriter fileWriter = new FileWriter(FAIL)) {
            fileWriter.write(json.toString());
        }
    }
}
