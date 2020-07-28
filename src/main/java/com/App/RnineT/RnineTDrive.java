package com.App.RnineT;

import com.google.api.services.drive.Drive;

import java.util.List;

abstract public class RnineTDrive<DriveClient> {
    protected String token;
    protected String jobID;
    protected DriveClient drive;
    public RnineTDrive(String token, String jobID){
        this.token = token;
        this.jobID = jobID;
        this.drive = this.initDriveClient();
    }

    abstract protected DriveClient initDriveClient();
    abstract public boolean download(String directoryID, String downloadDirectoryPath);
    abstract public boolean upload(String directoryPath, String directoryName, String uploadDirectoryID);
}
