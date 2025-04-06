package failisuhtlus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
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
                tooted.add(new Toode(toode.getInt("tootenumber"), toode.getString("nimi"), toode.getDouble("hind"), toode.getInt("lao seis")));
            }
            return tooted;
        } catch (NoSuchFileException e) {
            Toode õun = new Toode(1, "õun", 0.39, 54);
            Toode pirn = new Toode(2, "pirn", 0.59, 32);
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

    private JSONObject getJSON() throws IOException {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get("andmebaas.json")));
        } catch (NoSuchFileException e) {
            content = "{\n" +
                    "  \"kommentaarid\":\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"aeg\":\"2025-03-26 11:08:53\",\n" +
                    "      \"toode\":1,\n" +
                    "      \"sisu\":\"Väga head õunad\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"tooted\":\n" +
                    "  [\n" +
                    "    {\n" +
                    "      \"nimi\":\"õun\",\n" +
                    "      \"tootenumber\":1,\n" +
                    "      \"hind\":0.39,\n" +
                    "      \"lao seis\":54\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"nimi\":\"pirn\",\n" +
                    "      \"tootenumber\":2,\n" +
                    "      \"hind\":0.59,\n" +
                    "      \"lao seis\":32\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }
        return new JSONObject(content);
    }

    private void updateJson(JSONObject json) throws IOException {
        try (FileWriter fileWriter = new FileWriter(fail)) {
            fileWriter.write(json.toString());
        }
    }
}
