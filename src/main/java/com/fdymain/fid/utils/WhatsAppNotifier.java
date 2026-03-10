package com.fdymain.fid.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WhatsAppNotifier {

    /**
     * Envía el resultado del robot usando el template aprobado en Meta
     */
    public static void enviarResultadoRobot(
            String fecha,
            int aprobadas,
            int fallidas,
            String duracion) {

        try {

            String endpoint =
                    WhatsAppConfig.getApiUrl() +
                            WhatsAppConfig.getPhoneNumberId() +
                            "/messages";

            URL url = new URL(endpoint);

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");

            conn.setRequestProperty(
                    "Authorization",
                    "Bearer " + WhatsAppConfig.getAccessToken()
            );

            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            conn.setDoOutput(true);

            String body =
                    "{"
                            + "\"messaging_product\":\"whatsapp\","
                            + "\"to\":\"" + WhatsAppConfig.getToNumber() + "\","
                            + "\"type\":\"template\","
                            + "\"template\":{"
                            + "\"name\":\"resultado_robot_bpm\","
                            + "\"language\":{"
                            + "\"code\":\"es_419\""
                            + "},"
                            + "\"components\":["
                            + "{"
                            + "\"type\":\"body\","
                            + "\"parameters\":["
                            + "{ \"type\":\"text\", \"text\":\"" + fecha + "\" },"
                            + "{ \"type\":\"text\", \"text\":\"" + aprobadas + "\" },"
                            + "{ \"type\":\"text\", \"text\":\"" + fallidas + "\" },"
                            + "{ \"type\":\"text\", \"text\":\"" + duracion + "\" }"
                            + "]"
                            + "}"
                            + "]"
                            + "}"
                            + "}";

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            BufferedReader reader;

            if (responseCode >= 400) {

                reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));

            } else {

                reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
            }

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            System.out.println("WhatsApp API response: " + response);

            if (responseCode == 200) {

                System.out.println("✅ Notificación WhatsApp enviada correctamente");

            } else {

                System.out.println("⚠️ Error enviando WhatsApp. Código: " + responseCode);
            }

        } catch (Exception e) {

            System.out.println("❌ Error enviando notificación WhatsApp: "
                    + e.getMessage());
        }
    }
}