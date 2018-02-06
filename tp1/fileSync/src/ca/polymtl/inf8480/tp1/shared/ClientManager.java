package ca.polymtl.inf8480.tp1.shared;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public class ClientManager {
    private ArrayList<String> clients = new ArrayList<String>();
    private final static String CLIENT_MANAGER_NAME = "clientsMetadata.txt";

    public ClientManager() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(CLIENT_MANAGER_NAME));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String content = sb.toString();

            String[] contentLines = content.split(System.lineSeparator());
            for (String contentLine : contentLines) {
                this.clients.add(contentLine);
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createClientID() {
        String clientID = UUID.randomUUID().toString();
        try {
            FileWriter fw = new FileWriter(CLIENT_MANAGER_NAME, true);
            while (clients.contains(clientID)) {
                clientID = UUID.randomUUID().toString();
            }
            fw.write(clientID + System.lineSeparator());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clientID;
    }
}