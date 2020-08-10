package com.RnineT.Controller;

import com.RnineT.Controller.TransferRequest.*;
import com.RnineT.Drives.GDrive;
import com.RnineT.Drives.OneDrive;
import com.RnineT.Drives.RnineTDrive;
import com.RnineT.Status.Db.Directories.DirectoryRepository;
import com.RnineT.Status.Db.Status;
import com.RnineT.Storage.Directory;

import java.util.ArrayList;
import java.util.Map;

public class RnineT {
    private final String ONE_DRIVE = "one_drive";
    private final String GOOGLE_DRIVE = "google_drive";

    private RnineTDrive sourceDrive;
    private RnineTDrive destDrive;

    private Status status;

    private String jobID = "";
    private String uploadDirectoryID = "";
    private ArrayList<String> selectedItems = new ArrayList<String>();
    private Directory directory = Directory.getDirectoryInstance();

    public RnineT(TransferRequest request, DirectoryRepository directoryRepository) {
        SourceDrive sourceDrive = (SourceDrive) request.getSourceDrive();
        DestDrive destDrive = (DestDrive) request.getDestDrive();
        this.selectedItems = sourceDrive.getSelectedItems();
        this.uploadDirectoryID = destDrive.getUploadDirectoryID();
        this.status = new Status(directoryRepository);

        switch (sourceDrive.getName()){
            case ONE_DRIVE: {
                this.sourceDrive = new OneDrive(sourceDrive.getToken());
                break;
            }
            case GOOGLE_DRIVE: {
                this.sourceDrive = new GDrive(sourceDrive.getToken());
                break;
            }
        }

        jobID = this.sourceDrive.getJobID();
        switch (destDrive.getName()){
            case ONE_DRIVE: {
                this.destDrive = new OneDrive(destDrive.getToken(), jobID);
                break;
            }
            case GOOGLE_DRIVE: {
                this.destDrive = new GDrive(destDrive.getToken(), jobID);
                break;
            }
        }
    }

    public class Callback {
        public void onDownloadComplete(String error, String jobID, String directoryPath, String directoryName, Long size){
            if(!error.equals("")){

                return;
            }

            status.onDirectoryDownload(jobID, directoryPath, directoryName, size);

            String cloudDirectoryID = "";
            if(directory.getDirectoryPath(jobID).equals(directoryPath)) {
                cloudDirectoryID = uploadDirectoryID;
            } else {
                for(int i = 0; i < 10; i++){
                    try {
                        if(cloudDirectoryID.equals("")){
                            Thread.sleep(5000);
                            cloudDirectoryID = status.fetchCloudDirectoryID(jobID, directoryPath);
                            System.out.println("polling for uploadDirectoryID for "+ directoryPath);
                        } else {
                            break;
                        }
                    } catch (Exception e){}
                }
            }

            destDrive.upload(directoryPath, directoryName, cloudDirectoryID);
        }
    }

    public String getJobID(){
        return jobID;
    }

    public void startTransfer(){
        Callback callback = new Callback();
        selectedItems.forEach(selectedDirectoryID -> {
            String jobDirectoryPath = directory.getDirectoryPath(sourceDrive.getJobID());
            this.sourceDrive.download(selectedDirectoryID, jobDirectoryPath, callback);
        });
    }
}