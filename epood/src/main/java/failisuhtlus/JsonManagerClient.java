package failisuhtlus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import epood.ClientServerSide;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JsonManagerClient {
    private static final String FILE = "klientideAndmebaas.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


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

    public void writeJson(ClientServerSide client) throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
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

    public void updateCartJson(ClientServerSide client) throws IOException {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        lock.writeLock().lock();
        try {
            List<ClientServerSide> clients = readJson();
            for (ClientServerSide c : clients) {
                if (c.checkMatch(client.getName(), client.getEmail())) {
                    c.setCart(client.getCart());
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE), clients);
        } finally {
            lock.writeLock().unlock();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        }
    }
}
