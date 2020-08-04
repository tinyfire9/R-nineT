package com.RnineT.Status.Db.Jobs;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Job {
    @Id
    private String id;
    private String source;
    private String dest;

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getDest() {
        return dest;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }
}
