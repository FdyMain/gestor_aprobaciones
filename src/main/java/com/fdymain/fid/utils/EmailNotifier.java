package com.fdymain.fid.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class EmailNotifier {

    private static final String TEMPLATE_PATH = ".github/email-template.html";

    public static void enviarReporte(
            String fechaInicio,
            String fechaFin,
            int aprobadas,
            int reintentos,
            int procesadas,
            String duracion,
            String estado,
            Map<String, Integer> otrosCodigos) throws Exception {

        // En GitHub Actions el pipeline gestiona el email → evitar duplicado
        if ("true".equals(System.getenv("GITHUB_ACTIONS"))) {
            System.out.println("GitHub Actions detectado — email gestionado por el pipeline.");
            return;
        }

        String mailUsername = System.getenv("MAIL_USERNAME");
        String mailPassword = System.getenv("MAIL_PASSWORD");
        String mailTo1      = System.getenv("MAIL_TO_1");
        String mailTo2      = System.getenv("MAIL_TO_2");
        String mailTo3      = System.getenv("MAIL_TO_3");

        if (mailUsername == null || mailPassword == null || mailTo1 == null) {
            System.out.println("Variables MAIL_USERNAME / MAIL_PASSWORD / MAIL_TO_1 no configuradas — se omite email.");
            return;
        }

        String html = Files.readString(Paths.get(TEMPLATE_PATH));

        String estadoColor = "EXITOSO".equals(estado) ? "#16a34a" : "#dc2626";

        String otrosSection = buildOtrosCodigosSection(otrosCodigos);

        html = html
                .replace("{{REINTENTOS}}",          String.valueOf(reintentos))
                .replace("{{PROCESADAS}}",           String.valueOf(procesadas))
                .replace("{{DURACION}}",             duracion)
                .replace("{{FECHA_INICIO}}",         fechaInicio)
                .replace("{{FECHA_FIN}}",            fechaFin)
                .replace("{{ESTADO}}",               estado)
                .replace("{{ESTADO_COLOR}}",         estadoColor)
                .replace("{{RUN_NUMBER}}",           "LOCAL")
                .replace("{{BRANCH}}",               "local")
                .replace("{{COMMIT}}",               "N/A")
                .replace("{{RUN_URL}}",              "#")
                .replace("{{OTROS_CODIGOS_SECTION}}", otrosSection);

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

        if (mailTo3 != null && !mailTo3.isBlank()) {
            destinatarios += "," + mailTo3;
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarios));
        message.setSubject("Robot BPM LOCAL — " + estado);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);

        int maxIntentos = 3;
        Exception ultimo = null;
        for (int intento = 1; intento <= maxIntentos; intento++) {
            try {
                Transport.send(message);
                System.out.println("Email enviado correctamente a: " + destinatarios + " (intento " + intento + ")");
                return;
            } catch (Exception ex) {
                ultimo = ex;
                System.out.println("Intento " + intento + " fallido: " + ex.getMessage());
                if (intento < maxIntentos) Thread.sleep(5000);
            }
        }
        throw new MessagingException("Email no enviado tras " + maxIntentos + " intentos", ultimo);
    }

    private static String buildOtrosCodigosSection(Map<String, Integer> otrosCodigos) {
        if (otrosCodigos == null || otrosCodigos.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td style=\"padding:24px 32px 0;\">");
        sb.append("<p style=\"margin:0 0 12px;font-size:11px;font-weight:700;color:#64748b;")
          .append("text-transform:uppercase;letter-spacing:1.5px;\">")
          .append("Otros c\u00f3digos pendientes por revisar</p>");
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"")
          .append(" style=\"background-color:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;\">")
          .append("<tr style=\"background-color:#f1f5f9;\">")
          .append("<th style=\"padding:10px 20px;font-size:12px;color:#64748b;text-align:left;border-bottom:1px solid #e2e8f0;\">C\u00f3digo</th>")
          .append("<th style=\"padding:10px 20px;font-size:12px;color:#64748b;text-align:left;border-bottom:1px solid #e2e8f0;\">Cantidad</th>")
          .append("</tr>");

        otrosCodigos.forEach((codigo, cantidad) ->
            sb.append("<tr>")
              .append("<td style=\"padding:10px 20px;font-size:13px;color:#0f172a;border-bottom:1px solid #e2e8f0;\">")
              .append(codigo).append("</td>")
              .append("<td style=\"padding:10px 20px;font-size:13px;color:#0f172a;font-weight:600;border-bottom:1px solid #e2e8f0;\">")
              .append(cantidad).append("</td>")
              .append("</tr>")
        );

        sb.append("</table></td></tr>");
        return sb.toString();
    }
}
