package failisuhtlus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import epood.ClientServerSide;
import epood.EmployeeServerSide;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class JsonManagerClient {
    private static final String FILE_PATH = "klientideAndmebaas.json";
    private static final Gson gson = new Gson();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public List<ClientServerSide> readJson() {
        lock.readLock().lock();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type clientListType = new TypeToken<ArrayList<ClientServerSide>>() {}.getType();
            List<ClientServerSide> result = gson.fromJson(reader, clientListType);
            return result != null ? result : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void writeJson(ClientServerSide client) {
        lock.writeLock().lock();
        try {
            // Loeme praegused kliendid
            List<ClientServerSide> clients = readJson();
            if (clients == null) {
                clients = new ArrayList<>();
            }

            // lisame uue
            clients.add(client);

            // kirjutame faili
            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                gson.toJson(clients, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }
}

