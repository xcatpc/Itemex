package sh.ome.itemex.shedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import sh.ome.itemex.Itemex;
import sh.ome.itemex.database.createDatabase;

import static org.bukkit.Bukkit.getLogger;

public class DataDifferenceSender {


    public static void sendDataDifferencesToServer() {
        String responseIds = sendLatestIdsAndGetResponseAsync();
        if(responseIds != null && !responseIds.isEmpty()) {
            //getLogger().info("# DEBUG - from server: " + responseIds);
        }
    }

    private static String sendLatestIdsAndGetResponseAsync() {
        // Führt den Code asynchron zum Hauptthread aus
        Bukkit.getScheduler().runTaskAsynchronously(Itemex.getPlugin(Itemex.class), () -> {
            String ids = null;
            try {
                URL url = new URL(Itemex.server_url + "/itemex");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                // Timeout festlegen
                con.setConnectTimeout(5000); // 5 Sekunden Timeout für den Verbindungsaufbau
                con.setReadTimeout(5000);    // 5 Sekunden Timeout für das Lesen von Daten

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                // Karte erstellen, um Zähler und ID zu speichern
                Map<String, Object> data = new HashMap<>();
                data.put("jwt_token", Itemex.jwt_token);
                data.put("latest_ids", getLatestIdsString());

                // Karte in JSON-String konvertieren und in den Ausgabestrom schreiben
                String json = new Gson().toJson(data);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }

                // Antwortcode überprüfen und Verbindung schließen
                int responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    // Jetzt enthält content den gesamten Antwortinhalt.
                    //getLogger().info("# DEBUG - server reply: " + content.toString());
                    sendDataBasedOnDifference(content.toString());
                } else {
                    // Fehler bei der Anfrage
                }


                con.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return "string";
    }


    private static String getLatestIdsString() throws Exception {
        String[] tables = {
                "SELLORDERS", "BUYORDERS", "FULFILLEDORDERS",
                "PAYOUTS", "SETTINGS", "SELL_NOTIFICATION"
        };

        StringBuilder idsStringBuilder = new StringBuilder();
        idsStringBuilder.append("IDS:");

        try (Connection conn = createDatabase.createConnection()) {
            if (conn != null) {
                for (int i = 0; i < tables.length; i++) {
                    String table = tables[i];
                    try (Statement statement = conn.createStatement()) {
                        ResultSet rs = statement.executeQuery("SELECT MAX(id) FROM " + table);
                        if (rs.next()) {
                            idsStringBuilder.append(rs.getInt(1));

                            // Add ':' if it's not the last item
                            if (i != tables.length - 1) {
                                idsStringBuilder.append(":");
                            }
                            //getLogger().info(table + " " + rs.getInt(1));
                        }
                    }
                }
            }
        }

        //getLogger().info("# DEBUG - send to server: " + idsStringBuilder);
        return idsStringBuilder.toString();
    }


    public static void sendDataBasedOnDifference(String responseIds) {
        Connection conn = Itemex.c;
        Statement statement = null;

        try {
            if (Itemex.c == null) {
                Itemex.c = createDatabase.createConnection();
                //getLogger().info("# WARN - reopen Database");
            }
            statement = conn.createStatement();

            String[] latestIds = getLatestIds(conn).split(":");
            String[] serverIds = responseIds.split(":");

            if (latestIds.length != serverIds.length) {
                System.err.println("Ungültige Serverantwort!");
                return;
            }

            String[] tables = {
                    "SELLORDERS", "BUYORDERS", "FULFILLEDORDERS",
                    "PAYOUTS", "SETTINGS", "SELL_NOTIFICATION"
            };

            for (int i = 0; i < tables.length; i++) {
                int localId = Integer.parseInt(latestIds[i]);
                int serverId = Integer.parseInt(serverIds[i]);

                if (localId > serverId) {
                    ResultSet rs = statement.executeQuery(
                            "SELECT * FROM " + tables[i] + " WHERE id > " + serverId
                    );
                    sendDataToServer(tables[i], rs);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLatestIds(Connection conn) throws Exception {
        String[] tables = {
                "SELLORDERS", "BUYORDERS", "FULFILLEDORDERS",
                "PAYOUTS", "SETTINGS", "SELL_NOTIFICATION"
        };

        StringBuilder ids = new StringBuilder();

        for (String table : tables) {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT MAX(id) FROM " + table);
            if (rs.next()) {
                ids.append(rs.getInt(1)).append(":");
            }
            statement.close();
        }

        return ids.substring(0, ids.length() - 1); // Removes the trailing colon
    }

    private static void sendDataToServer(String table, ResultSet rs) throws Exception {
        List<Map<String, Object>> dataList = new ArrayList<>();

        ResultSetMetaData rsmd = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> data = new HashMap<>();
            int columnCount = rsmd.getColumnCount();
            data.put("table", table);
            for (int i = 1; i <= columnCount; i++) {
                String name = rsmd.getColumnName(i);
                Object value = rs.getObject(i);
                data.put(name, value);
            }
            dataList.add(data);
        }

        // Convert list to JSON
        String json = new Gson().toJson(dataList);

        postToServer(json);
    }

    private static void postToServer(String inputJson) {
        // Führt den Code asynchron zum Hauptthread aus
        Bukkit.getScheduler().runTaskAsynchronously(Itemex.getPlugin(Itemex.class), () -> {
            try {
                URL url = new URL(Itemex.server_url + "/itemex/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                // Timeout festlegen
                con.setConnectTimeout(15000); // 15 Sekunden Timeout für den Verbindungsaufbau
                con.setReadTimeout(15000);    // 15 Sekunden Timeout für das Lesen von Daten

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);

                // Karte erstellen und das eingegebene JSON einfügen
                Map<String, Object> payload = new HashMap<>();
                payload.put("jwt_token", Itemex.jwt_token);
                payload.put("data", new Gson().fromJson(inputJson, Object.class));

                // Karte in JSON-String konvertieren und in den Ausgabestrom schreiben
                String jsonPayload = new Gson().toJson(payload);
                try (OutputStream os = con.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                }

                // Antwortcode überprüfen und Verbindung schließen
                int responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    // Jetzt enthält content den gesamten Antwortinhalt.
                    //getLogger().info(content.toString());
                    // Eventuell weitere Verarbeitung von `content` hier hinzufügen

                } else {
                    // Fehler bei der Anfrage
                }

                con.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
