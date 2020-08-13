package com.RnineT.Status.Database.Jobs;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Job {
    @Id
    private String id;
    private String source;
    private String dest;
    private String status;
    private Long totalItemsCount;
    private Long size;

    public static final class STATES {
        public static final String INITIATED = "INITIATED";
        public static final String TRANSFERRING = "TRANSFERRING";
        public static final String COMPLETED = "COMPLETED";
    }

    public Long getTotalItemsCount() {
        return totalItemsCount;
    }

    public void setTotalItemsCount(Long totalItemsCount) {
        this.totalItemsCount = totalItemsCount;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getDest() {
        return dest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
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
