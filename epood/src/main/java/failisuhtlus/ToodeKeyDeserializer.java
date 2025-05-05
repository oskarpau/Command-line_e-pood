package failisuhtlus;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class ToodeKeyDeserializer extends KeyDeserializer {
    /**
     * Jacksoniga Toode objketi failist lugemiseks
     * @param key
     * @param ctxt
     * @return
     * @throws IOException
     */
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        String[] parts = key.split("\\|");
        int number = Integer.parseInt(parts[0]);
        String nimi = parts[1];
        BigDecimal hind = new BigDecimal(parts[2]);
        int lao_seis = Integer.parseInt(parts[3]);

        return new Toode(number, nimi, hind, lao_seis);

    }
}
