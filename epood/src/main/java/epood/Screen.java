package epood;

import failisuhtlus.Ostukorv;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Screen {
    void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException;
}
