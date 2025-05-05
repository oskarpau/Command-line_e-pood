package failisuhtlus;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ToodeKeySerializer extends JsonSerializer<Toode> {
    /**
     * Jacksoniga Toode objekti faili kirjutamiseks
     * @param toode
     * @param gen
     * @param serializers
     * @throws IOException
     */
    @Override
    public void serialize(Toode toode, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String key = toode.getNumber() + "|" + toode.getNimi() + "|" + toode.getHind() + "|" + toode.getLao_seis();
        gen.writeFieldName(key);
    }
}
