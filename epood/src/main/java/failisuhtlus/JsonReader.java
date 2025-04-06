package failisuhtlus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {
    private String fail;

    public JsonReader(String fail) {
        this.fail = fail;
    }

    public List<Toode> getTooted() throws IOException {

        List<Toode> tooted = new ArrayList<>();

        JSONObject json = getJSON();

        JSONArray tootedArray = json.getJSONArray("tooted");

        for (int i = 0; i < tootedArray.length(); i++) {
            JSONObject toode = tootedArray.getJSONObject(i);
            tooted.add(new Toode(toode.getInt("tootenumber"), toode.getString("nimi"), toode.getDouble("hind"), toode.getInt("lao seis")));
        }

        return tooted;
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

    private JSONObject getJSON() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("andmebaas.json")));
        return new JSONObject(content);
    }

    private void updateJson(JSONObject json) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fail)) {
            fileWriter.write(json.toString());
        }
    }
}
