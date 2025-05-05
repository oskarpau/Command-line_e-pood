package failisuhtlus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        lock.readLock().lock();
        try {
            File file = new File(FILE);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<ClientServerSide>>() {});
        } finally {
            lock.readLock().unlock();
        }
    }

    public void writeJson(ClientServerSide client) throws IOException {
        lock.writeLock().lock();
        try {
            List<ClientServerSide> clients = readJson();
            clients.add(client);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE), clients);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
