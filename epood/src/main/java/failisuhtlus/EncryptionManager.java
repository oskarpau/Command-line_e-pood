package failisuhtlus;


//abimeetodid räsimiseks

import com.kosprov.jargon2.api.Jargon2;


public class EncryptionManager {

    /**
     * kasutada parooli lisamisel
     * @param password hashimata sisend
     * @return hashitud parool, string
     */
    public static String passwordToArgon(String password) {
        return Jargon2.jargon2Hasher()
                .type(Jargon2.Type.ARGON2id)
                .memoryCost(65536)
                .timeCost(3)
                .parallelism(1)
                .saltLength(16)
                .hashLength(16)
                .password(password.getBytes())
                .encodedHash();

    }

    /**
     * kontrolli parooli ja salvestatud räsi vastavust
     * @param hash hashitud parool
     * @param password hashimata sisend
     * @return kas tegu õige parooliga
     */
    public static boolean verifyPassword(String hash, String password) {
        return Jargon2.jargon2Verifier()
                .hash(hash)
                .password(password.getBytes())
                .verifyEncoded();
    }

}
