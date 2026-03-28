package com.fdymain.fid.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WhatsAppNotifier {

    public static void enviarResultadoRobot(
            String fecha,
            int procesadas,
            int reintentos,
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

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            conn.setDoOutput(true);

            String body =
                    "{"
                            + "\"messaging_product\":\"whatsapp\","
                            + "\"to\":\"" + WhatsAppConfig.getToNumber() + "\","
                            + "\"type\":\"template\","
                            + "\"template\":{"
                            + "\"name\":\"resultado_robot_bpm\","
                            + "\"language\":{"
                            + "\"code\":\"es\""
                            + "},"
                            + "\"components\":["
                            + "{"
                            + "\"type\":\"body\","
                            + "\"parameters\":["
                            + "{ \"type\":\"text\", \"parameter_name\":\"fecha\", \"text\":\"" + fecha + "\" },"
                            + "{ \"type\":\"text\", \"parameter_name\":\"procesadas\", \"text\":\"" + procesadas + "\" },"
                            + "{ \"type\":\"text\", \"parameter_name\":\"reintentos\", \"text\":\"" + reintentos + "\" },"
                            + "{ \"type\":\"text\", \"parameter_name\":\"duracion\", \"text\":\"" + duracion + "\" }"
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

            if (responseCode >= 200 && responseCode < 300) {

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