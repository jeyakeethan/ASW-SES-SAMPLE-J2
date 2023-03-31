package org.example;

import software.amazon.awssdk.services.ses.model.SesException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

public class AmazonSESSampleSMPTAuthenticationWithAttachment {

    // Amazon SES SMTP IAM User credentials.
    static final String SMTP_USERNAME = "AKIAQNHULAXJWXYSRYGG";
    static final String SMTP_PASSWORD = "BFtOAsMghpv42h9uyajjJtI3DRfrOGRCuPNHnJPNIrdA";

    // The name of the Configuration Set to use for this message.
    // You can create a new configuration in the Amazon AWS SES Console
    static final String CONFIGSET = "SimpleConfiguration";

    // Amazon SES SMTP host name (US West 1 - California region) and Port
    // See https://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html#region-endpoints
    static final String HOST = "email-smtp.us-west-1.amazonaws.com";
    static final int PORT = 587;

    public static void main(String[] args) throws Exception {

        // Get required arguments from executor
        String SUBJECT = "Amazon SES test (SMTP interface accessed using Java)";
        String sender = args[0];
        String recipient = args[1];
        String fileLocation = args[2];

        // The email body for recipients with non-HTML email clients.
        String bodyText = "Hello,\r\n" + "Please see the attached file for a list "
                + "of customers to contact.";

        // The HTML body of the email.
        String bodyHTML = "<html>" + "<head></head>" + "<body>" + "<h1>Hello!</h1>"
                + "<p>Please see the attached file for a " + "list of customers to contact.</p>" + "</body>" + "</html>";

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a new MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Add a configuration set header. Comment or delete the
        // next line if you are not using a configuration set
        message.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);

        // Add subject, from and to lines.
        message.setSubject(SUBJECT, "UTF-8");
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

        // Create a multipart/alternative child container.
        MimeMultipart msgBody = new MimeMultipart("alternative");

        // Create a wrapper for the HTML and text parts.
        MimeBodyPart wrap = new MimeBodyPart();

        // Define the text part.
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(bodyText, "text/plain; charset=UTF-8");

        // Define the HTML part.
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");

        // Add the text and HTML parts to the child container.
        msgBody.addBodyPart(textPart);
        msgBody.addBodyPart(htmlPart);

        // Add the child container to the wrapper object.
        wrap.setContent(msgBody);

        // Create a multipart/mixed parent container.
        MimeMultipart content = new MimeMultipart("mixed");

        // Add the parent container to the message.
        message.setContent(content);
        content.addBodyPart(wrap);

        // Define the attachment.
        File theFile = new java.io.File(fileLocation);
        byte[] fileContent = Files.readAllBytes(theFile.toPath());

        MimeBodyPart attachment = new MimeBodyPart();
        DataSource fds = new ByteArrayDataSource(fileContent, "text/csv");
        attachment.setDataHandler(new DataHandler(fds));
        attachment.setFileName(theFile.getName());

        // Add the attachment to the message.
        content.addBodyPart(attachment);

        // Create a transport.
        Transport transport = session.getTransport();

        try {
            System.out.println("Attempting to send an email through Amazon SES " + "using the AWS SDK for Java...");
            System.out.println("Sending...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(message, message.getAllRecipients());
            System.out.println("Email sent!");

        } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        } finally {
            // Close and terminate the connection.
            transport.close();
        }
    }
}