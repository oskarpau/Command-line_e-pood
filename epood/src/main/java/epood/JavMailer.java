package epood;

import failisuhtlus.Tellimus;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.util.Properties;


public class JavMailer {

    static String confirmation = """
            Tere!
            
            Registreerumine toimus edukalt.
            Täname, et kasutatate meie epoodi!""";

    public static void sendEmail(String recipientEmail, String subject, String msg) {

        final String fromEmail = "oopepood@gmail.com";
        final String password = "pzjc zcqn gxws ccdd"; // Gmaili genetud

        //properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        //sisselogimine
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // message details
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "OOPnoReply"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(msg);

            // Send the email
            Transport.send(message);
            System.out.println("Confirmation email sent successfully!");

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendRegConfirmation(String recipientEmail) {
        sendEmail(recipientEmail, "Account Confirmation",confirmation);
    }

    public static void sendPurchaseNotification(String recipientEmail, String confirmationMessage) {
        sendEmail(recipientEmail, "Purchase from OOPepood",confirmationMessage);
    }


    public static void sendPurchaseConfirmation(String recipientEmail, Tellimus tellimus) {
        sendEmail(recipientEmail, "Purchase confirmatiom", tellimus.clientToString());
        //töötajale teavitus
        sendPurchaseNotification("oopepood@mail.ee",
                "Klient "+tellimus.getKliendiID()+
                "sooritas tellimuse arvele "+
                        tellimus.getOstukorv().getKoguHind().setScale(2, RoundingMode.HALF_UP)+
                "€\n tellimuse id: "+tellimus.getTellimuseID()); //töötajale teavitus
    }

/*    public static void main(String[] args) {
        String to = "kusti.sammul@gmail.com";
        String message = "Töötab!\nThank you for registering. Please confirm your email by clicking the link.";
        sendRegConfirmation(to, message);
    }*/
}

