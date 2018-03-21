package ca.polymtl.inf8480.tp2.shared;

import java.io.*;

public class Config {
    private final String configFileName = "config.txt";
    private String loadBalancerIP;
    private String loadBalancerPort;
    private String nameRepositoryIP;
    private String nameRepositoryPort;
    private boolean loadBalancerIsSecure;
    private String loadBalancerUsername;
    private String loadBalancerPassword;

    public Config() {
        String rawConfig = this.getRawConfig();
        String[] lines = rawConfig.split(System.lineSeparator());
        this.loadBalancerIP = lines[3].split(":")[0];
        this.loadBalancerPort = lines[3].split(":")[1];
        this.nameRepositoryIP = lines[5].split(":")[0];
        this.nameRepositoryPort = lines[5].split(":")[1];
        this.loadBalancerIsSecure = lines[7].equalsIgnoreCase("true");
        this.loadBalancerUsername = lines[9];
        this.loadBalancerPassword = lines[11];
    }

    public String getLoadBalancerIP() {
        return this.loadBalancerIP;
    }

    public int getLoadBalancerPort() {
        return Integer.parseInt(this.loadBalancerPort);
    }

    public String getNameRepositoryIP() {
        return this.nameRepositoryIP;
    }

    public int getNameRepositoryPort() {
        return Integer.parseInt(this.nameRepositoryPort);
    }

    public boolean isLoadBalancerSecure() {
        return this.loadBalancerIsSecure;
    }

    public String getLoadBalancerUsername() {
        return this.loadBalancerUsername;
    }

    public String getLoadBalancerPassword() {
        return this.loadBalancerPassword;
    }

    private String getRawConfig() {
        String content = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.configFileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            content = sb.toString();

            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Exception");
            e.printStackTrace();
        }

        return content;
    }
}
