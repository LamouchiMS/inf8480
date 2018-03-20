package ca.polymtl.inf8480.tp2.shared;

import java.io.*;

public class Config {
    private final String configFileName = "config.txt";
    private String loadBalancerIP;
    private String nameRepositoryIP;
    private boolean loadBalancerIsSecure;
    private String loadBalancerUsername;
    private String loadBalancerPassword;

    public Config() {
        String rawConfig = this.getRawConfig();
        String[] lines = rawConfig.split(System.lineSeparator());
        this.loadBalancerIP = lines[1].split(" ")[1];
        this.nameRepositoryIP = lines[2].split(" ")[1];
        this.loadBalancerIsSecure = lines[3].split(" ")[1].equalsIgnoreCase("true");
        this.loadBalancerUsername = lines[4].split(" ")[1];
        this.loadBalancerPassword = lines[5].split(" ")[1];
    }

    public String getLoadBalancerIP() {
        return this.loadBalancerIP;
    }

    public String getNameRepositoryIP() {
        return this.nameRepositoryIP;
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }
}
