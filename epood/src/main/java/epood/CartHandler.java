package epood;

import failisuhtlus.Ostukorv;
import failisuhtlus.Toode;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;

public class CartHandler implements Screen{
    private String currentSubScreen;
    private String name;
    private String email;
    private Toode toodeKogusMuudetud;

    public CartHandler() {
        this.name = "";
        this.email = "";
        this.toodeKogusMuudetud = null;
    }

    // kasutajale kuvatakse valikud
    // order; change quantity; remove product; back
    public void show(DataOutputStream dout, Ostukorv cart) throws IOException {
        currentSubScreen = "select action";
        // näitame ostukorvis olevaid tooteid
        showCart(dout, cart);

    }
    public void handler(DataOutputStream dout, String cmd, String[] args, Ostukorv cart) throws IOException{
        if (cmd.equals("order") && currentSubScreen.equals("select action")) {
            // hetkel küsime nime ja emaili, hiljem võime asju lisada
            dout.writeInt(1);
            dout.writeUTF("Enter your name:");
            currentSubScreen = "enter email";
        } else if (currentSubScreen.equals("enter email")) {
            name = cmd;
            dout.writeInt(1);
            dout.writeUTF("Enter your email:");
            // vaatame, kas sobib
            currentSubScreen = "validating email";
        } else if (currentSubScreen.equals("validating email")) {
            if (isValid(cmd)) {
                email = cmd;
                finishOrder(dout, cart, name, email);
            } else {
                dout.writeInt(1);
                dout.writeUTF("Invalid email");
                currentSubScreen = "enter email";
            }
        }

        else if (cmd.equals("change quantity")) {
            dout.writeInt(1);
            dout.writeUTF("Enter the name of the product:");
            currentSubScreen = "quantity - enter name";
        } else if (currentSubScreen.equals("quantity - enter name")) {
            toodeKogusMuudetud = null;
            Toode toodeKogusMuudetud = cart.getItems().keySet().stream()
                    .filter(toode -> toode.getNimi().equals(cmd))
                    .findFirst()
                    .orElse(null);
            if (toodeKogusMuudetud != null) {
                dout.writeInt(1);
                dout.writeUTF("Enter quantity");
                currentSubScreen = "quantity - enter quantity";
            } else {
                dout.writeInt(1);
                dout.writeUTF("This product is not in the cart\nEnter valid name of the product:");
                currentSubScreen = "quantity - enter name";
            }
        } else if (currentSubScreen.equals("quantity - enter quantity")) {
            if (cmd != null && cmd.matches("-?\\d+")) {
                cart.addToode(toodeKogusMuudetud, Integer.parseInt(cmd));
                currentSubScreen = "select action";
                showCart(dout, cart);
            } else {
                dout.writeInt(1);
                dout.writeUTF("Enter valid quantity");
                currentSubScreen = "quantity - enter quantity";
            }
        }


        else if (cmd.equals("remove product")) {

        }
    }

    private void showCart(DataOutputStream dout, Ostukorv cart) throws IOException {
        Map<Toode, Integer> tooted = cart.getItems();
        String outStr = "";
        if (tooted.isEmpty()) {
            outStr = outStr.concat("Shopping cart is empty.Please add items and come back.\nType 'back' to return to the main menu.");
        } else {
            outStr = outStr.concat("Products in cart:\n");
            for (Map.Entry<Toode, Integer> entry : tooted.entrySet()) {
                outStr = outStr.concat(entry.getKey().getNimi() + " x" + entry.getValue() +
                        " = " + entry.getKey().getHind().multiply(BigDecimal.valueOf(entry.getValue())) + " EUR\n");
            }
            outStr = outStr.concat("Sum: " + cart.getKoguHind() + " EUR\n");
            outStr = outStr.concat("Choose one of the following actions:\norder; change quantity; remove product; back");
        }
        dout.writeInt(1);
        dout.writeUTF(outStr);
    }

    // https://www.geeksforgeeks.org/check-email-address-valid-not-java/
    // Method to check if the email is valid
    public boolean isValid(String email) {

        // Regular expression to match valid email formats
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Compile the regex
        Pattern p = Pattern.compile(emailRegex);

        // Check if email matches the pattern
        return email != null && p.matcher(email).matches();
    }

    private void finishOrder(DataOutputStream dout, Ostukorv cart, String name, String email) throws IOException {
        // todo
        // send email
        // change quantity of products in json file
        //cart.getItems().forEach((k, v) -> {cart.removeToode(k);});
        cart.tyhjendaOstukorv();
        dout.writeInt(1);
        dout.writeUTF("Order successful for " + name + "\nConfirmation sent to: " + email + "\nType 'back' to return to the main menu\n(CHECK TODO IN 'CartHandler'!!)");
    }
}
