package com.RnineT.Transfer.Drives;

import com.RnineT.Transfer.Transfer.*;
import com.RnineT.Transfer.Storage.Directory;

import java.util.*;

abstract public class RnineTDrive<DriveClient> {
    private Long totalItemsCount;
    private Long totalSizeInBytes;
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

    public Long getTotalItemsCount(ArrayList<String> selectedItems) {
        if(this.totalItemsCount == null){
            Map<String, Long> sizeAndItemsCount =  new HashMap<String, Long>();
            sizeAndItemsCount.put("totalSizeInBytes", (long) 0);
            sizeAndItemsCount.put("totalItemsCount", (long) 0);

            this.fetchTotalSizeInBytesAndTotalItemsCount(selectedItems, sizeAndItemsCount);

            this.totalSizeInBytes = sizeAndItemsCount.get("totalSizeInBytes");
            this.totalItemsCount = sizeAndItemsCount.get("totalItemsCount");
        }
        return this.totalItemsCount;
    }

    public Long getTotalSizeInBytes(ArrayList<String> selectedItems){
        if(this.totalSizeInBytes == null){
            Map<String, Long> sizeAndItemsCount = new HashMap<String, Long>();
            sizeAndItemsCount.put("totalSizeInBytes", (long) 0);
            sizeAndItemsCount.put("totalItemsCount", (long) 0);

            this.fetchTotalSizeInBytesAndTotalItemsCount(selectedItems, sizeAndItemsCount);

            System.out.println(sizeAndItemsCount.toString());

            this.totalSizeInBytes = sizeAndItemsCount.get("totalSizeInBytes");
            this.totalItemsCount = sizeAndItemsCount.get("totalItemsCount");
        }
        return this.totalSizeInBytes;
    }

    abstract protected DriveClient initDriveClient();
    abstract protected void fetchTotalSizeInBytesAndTotalItemsCount(ArrayList<String> selectedItems, Map<String, Long> output);
    abstract public boolean download(String directoryID, String downloadDirectoryPath, Callback callback);
    abstract public boolean upload(String localDirectoryID, String directoryPath, String directoryName, String uploadDirectoryID, Callback callback);
}
