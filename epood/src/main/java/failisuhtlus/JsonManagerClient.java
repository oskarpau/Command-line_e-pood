package failisuhtlus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import epood.ClientServerSide;
import failisuhtlus.Toode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class JsonManagerClient {
    private static final String FILE = "klientideAndmebaas.json";
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static SimpleModule module = new SimpleModule();

    static {
        module.addKeyDeserializer(Toode.class, new ToodeKeyDeserializer());
        module.addKeySerializer(Toode.class, new ToodeKeySerializer());
        objectMapper.registerModule(module);
    }

    /**
     * Praeguste kliendide lugemiseks
     * @return tagastab listi kõikidest klientidest
     * @throws IOException
     */
    public List<ClientServerSide> readJson() throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        lock.readLock().lock();
        try {
            File file = new File(FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<ClientServerSide>>() {});
        } finally {
            lock.readLock().unlock();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        }
    }

    /**
     * Klientide lisamiseks
     * @param client uus klient
     * @throws IOException
     */
    public void addClientJson(ClientServerSide client) throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // FAIL_ON_EMPTY_BEANS, et töötaks ka tühja ostukorviga
        lock.writeLock().lock();
        try {
            List<ClientServerSide> clients = readJson();
            clients.add(client);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE), clients);
        } finally {
            lock.writeLock().unlock();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        }
    }

    /**
     * Põhiline funktsioon cart'i uuendamiseks
     * @param updatedClient
     * @throws IOException
     */
    public void updateCartJson(ClientServerSide updatedClient) throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // FAIL_ON_EMPTY_BEANS, et töötaks ka tühja ostukorviga
        lock.writeLock().lock();
        try {
            List<ClientServerSide> clients = readJson();
            List<ClientServerSide> updatedClients = new ArrayList<>();

            // lisame uuendatud clienti clientide listi
            for (ClientServerSide existingClient : clients) {
                if (existingClient.getId().equals(updatedClient.getId())) {
                    updatedClients.add(updatedClient);
                } else {
                    updatedClients.add(existingClient);
                }
            }

            for (ClientServerSide client : updatedClients) {
                System.out.println(client);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE), updatedClients);
        } finally {
            lock.writeLock().unlock();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        }
    }

}
