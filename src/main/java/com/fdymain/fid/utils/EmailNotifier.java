package com.fdymain.fid.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class EmailNotifier {

    private static final String TEMPLATE_PATH = ".github/email-template.html";

    public static void enviarReporte(
            String fechaInicio,
            String fechaFin,
            int aprobadas,
            int fallidas,
            int procesadas,
            String duracion) throws Exception {

        // En GitHub Actions el pipeline gestiona el email → evitar duplicado
        if ("true".equals(System.getenv("GITHUB_ACTIONS"))) {
            System.out.println("GitHub Actions detectado — email gestionado por el pipeline.");
            return;
        }

        String mailUsername = System.getenv("MAIL_USERNAME");
        String mailPassword = System.getenv("MAIL_PASSWORD");
        String mailTo1      = System.getenv("MAIL_TO_1");
        String mailTo2      = System.getenv("MAIL_TO_2");

        if (mailUsername == null || mailPassword == null || mailTo1 == null) {
            System.out.println("Variables MAIL_USERNAME / MAIL_PASSWORD / MAIL_TO_1 no configuradas — se omite email.");
            return;
        }

        String html = Files.readString(Paths.get(TEMPLATE_PATH));

        String estado      = (fallidas == 0) ? "EXITOSO" : "CON ERRORES";
        String estadoColor = (fallidas == 0) ? "#16a34a" : "#dc2626";

        html = html
                .replace("{{APROBADAS}}",   String.valueOf(aprobadas))
                .replace("{{FALLIDAS}}",     String.valueOf(fallidas))
                .replace("{{PROCESADAS}}",   String.valueOf(procesadas))
                .replace("{{DURACION}}",     duracion)
                .replace("{{FECHA_INICIO}}", fechaInicio)
                .replace("{{FECHA_FIN}}",    fechaFin)
                .replace("{{ESTADO}}",       estado)
                .replace("{{ESTADO_COLOR}}", estadoColor)
                .replace("{{RUN_NUMBER}}",   "LOCAL")
                .replace("{{BRANCH}}",       "local")
                .replace("{{COMMIT}}",       "N/A")
                .replace("{{RUN_URL}}",      "#");

        Properties props = new Properties();
        props.put("mail.smtp.host",                 "smtp.gmail.com");
        props.put("mail.smtp.port",                 "465");
        props.put("mail.smtp.auth",                 "true");
        props.put("mail.smtp.socketFactory.port",   "465");
        props.put("mail.smtp.socketFactory.class",  "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailUsername, "Robot BPM"));

        String destinatarios = mailTo1;
        if (mailTo2 != null && !mailTo2.isBlank()) {
            destinatarios += "," + mailTo2;
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarios));
        message.setSubject("Robot BPM LOCAL — " + estado);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        Transport.send(message);

        System.out.println("Email enviado correctamente a: " + destinatarios);
    }
}
