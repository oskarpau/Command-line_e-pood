package epood;

public class EmployeeServerSide {
    private String email;
    private String password; // praegu kõikidel töötajatel sama parool

    public EmployeeServerSide(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Kontrollime, kas andmed on õiged, pigem tuleviku jaoks, kui emailid ja paroolid on turvalisemalt teises kohas
     * @param email
     * @param password
     * @return kas andmed on õiged
     */
    public boolean checkCredentials(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

}
