package failisuhtlus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import epood.ClientServerSide;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JsonManagerHistory {
    private static final String FILE = "ostudeAjalugu.json";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static SimpleModule module = new SimpleModule();

    static {
        module.addKeyDeserializer(Toode.class, new ToodeKeyDeserializer());
        module.addKeySerializer(Toode.class, new ToodeKeySerializer());
        objectMapper.registerModule(module);
    }

    /**
     * Tehtud tellimuste lugemiseks
     * @return tagastab listi kõikidest tellimustest
     * @throws IOException
     */
    public List<Tellimus> readJson() throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        lock.readLock().lock();
        try {
            File file = new File(FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<Tellimus>>() {});
        } finally {
            lock.readLock().unlock();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        }
    }

    /**
     * Tellimuste lisamiseks
     * @param tellimus uus klient
     * @throws IOException
     */
    public void addTellimusJson(Tellimus tellimus) throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // FAIL_ON_EMPTY_BEANS, et töötaks ka tühja ostukorviga
        lock.writeLock().lock();
        try {
            List<Tellimus> clients = readJson();
            clients.add(tellimus);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE), clients);
        } finally {
            lock.writeLock().unlock();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        }
    }
}
