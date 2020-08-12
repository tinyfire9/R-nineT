package com.RnineT.Transfer.Drives;

import com.RnineT.Transfer.Transfer.*;
import com.RnineT.Transfer.Storage.Directory;

import java.util.UUID;

abstract public class RnineTDrive<DriveClient> {
    private String token;
    private String jobID;
    private DriveClient drive;

    public RnineTDrive(String token){
        this.token = token;
        this.jobID = UUID.randomUUID().toString();
        this.drive = this.initDriveClient();

        Directory directory  = Directory.getDirectoryInstance();
        directory.makeDirByJobID(jobID);
    }

    public RnineTDrive(String token, String jobID){
        this.token = token;
        this.jobID = jobID;
        this.drive = this.initDriveClient();
    }

    public String getJobID(){
        return jobID;
    }

    public DriveClient getDrive(){
        return drive;
    }

    public String getToken(){
        return token;
    }

    abstract protected DriveClient initDriveClient();
    abstract public boolean download(String directoryID, String downloadDirectoryPath, Callback callback);
    abstract public boolean upload(String localDirectoryID, String directoryPath, String directoryName, String uploadDirectoryID, Callback callback);
}
