package epood;

import failisuhtlus.EncryptionManager;

public class EmployeeServerSide {
    private String email;
    private String password; // praegu kõikidel töötajatel sama parool
    //salvestame hashitud kujul


    public EmployeeServerSide(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Kontrollime, kas andmed on õiged, pigem tuleviku jaoks, kui emailid ja paroolid on turvalisemalt teises kohas
     * hashib parooli ja võrdleb räsisid
     * @param email email
     * @param password hashimata sisend
     * @return kas andmed on õiged
     */
    public boolean checkCredentials(String email, String password) {
        return this.email.equals(email) && EncryptionManager.verifyPassword(this.password, password);
    }

    public String getEmail() {
        return email;
    }

}
