package com.RnineT.Status.Database.Directories;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Directory {
    public static final String STATE_UPLOADED = "UPLOADED";
    public static final String STATE_DOWNLOADED = "DOWNLOADED";
    public static final String STATE_ERROR = "ERROR";

    @Id
    private String localDirectoryID;

    private String destDriveDirectoryID;
    private String sourceDriveDirectoryID;
    private String jobID;
    private String directoryPath;
    private String directoryName;
    private String state;
    private long size;

    public String getLocalDirectoryID() {
        return localDirectoryID;
    }

    public void setLocalDirectoryID(String localDirectoryID) {
        this.localDirectoryID = localDirectoryID;
    }

    public String getDestDriveDirectoryID() {
        return destDriveDirectoryID;
    }

    public void setDestDriveDirectoryID(String destDriveDirectoryID) {
        this.destDriveDirectoryID = destDriveDirectoryID;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getJobID() {
        return jobID;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public long getSize() {
        return size;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setState(String state){
        this.state = state;
    }

    public String getState(){
        return state;
    }

    public void setSourceDriveDirectoryID(String id){
        this.sourceDriveDirectoryID = id;
    }

    public String getSourceDriveDirectoryID() {
        return sourceDriveDirectoryID;
    }
}
