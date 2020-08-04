package com.RnineT.Status.Db.Downloads;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Download {
    @Id
    private String jobID;
    private String directoryPath;
    private int size;

    public String getJobID() {
        return jobID;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public int getSize() {
        return size;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
