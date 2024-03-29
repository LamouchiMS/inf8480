package ca.polymtl.inf8480.tp1.shared;

import java.io.Serializable;

public class MyFile implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String lockClientID;
    private String content;

    public MyFile(String name, String lockClientID) {
        this.name = name;
        this.lockClientID = lockClientID;
        this.content = null;
    }

    public MyFile(String name, String lockClientID, String content) {
        this.name = name;
        this.lockClientID = lockClientID;
        this.content = content;
    }

    public String getName() {
        return this.name;
    }

    public String getLockClientID() {
        return this.lockClientID;
    }

    public String getContent() {
        return this.content;
    }

    public void setLockClientID(String clientID) {
        this.lockClientID = clientID;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return name;
    }
}