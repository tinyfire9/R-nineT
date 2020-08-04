package com.RnineT.Status.Db.Uploads;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Upload {
    @Id
    private String jobID;
    private String directoryID;

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getDirectoryID() {
        return directoryID;
    }

    public void setDirectoryID(String directoryID) {
        this.directoryID = directoryID;
    }
}
