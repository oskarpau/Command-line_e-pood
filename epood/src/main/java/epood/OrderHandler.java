package epood;

import failisuhtlus.Ostukorv;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 DEPRECATED - tellimuste vormistamsiega tegeleme cartHandleris, sest enamus infot juba kliendi objektiga seotud
 */
public class OrderHandler implements Screen {


    /**
     * Näitame ostukorvi sisu ja kogumaksumust
     * @param dout
     * @throws IOException
     */
    public void show(DataOutputStream dout) throws IOException {
        // todo
        System.out.println("hello");
    }
    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException {
        // todo
        System.out.println("hello");
    }

}
