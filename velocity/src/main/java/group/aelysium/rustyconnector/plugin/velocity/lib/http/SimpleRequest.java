package group.aelysium.rustyconnector.plugin.velocity.lib.http;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleRequest {
    /**
     * Makes a basic POST request to a URL
     * @param url The url to send the request to.
     * @param contents The JSON body of the request.
     */
    public static void post(URL url, String contents) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(contents.length()));
        connection.setUseCaches(false);
        VelocityRustyConnector.getAPI().getLogger().log(contents);

        try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
            dataOutputStream.writeBytes(contents);
        }

        try (BufferedReader bf = new BufferedReader(new InputStreamReader(
                connection.getInputStream())))
        {
            String line;
            while ((line = bf.readLine()) != null) {
                VelocityRustyConnector.getAPI().getLogger().log(line);
            }
        }
        VelocityRustyConnector.getAPI().getLogger().log(connection.getResponseMessage());

    }
}