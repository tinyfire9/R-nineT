package com.RnineT.Controller;

import com.RnineT.Controller.TransferRequest.*;
import com.RnineT.Drives.GDrive;
import com.RnineT.Drives.OneDrive;
import com.RnineT.Drives.RnineTDrive;
import com.RnineT.Storage.Directory;

import java.util.ArrayList;

public class RnineT {
    private final String ONE_DRIVE = "one_drive";
    private final String GOOGLE_DRIVE = "google_drive";

    private RnineTDrive sourceDrive;
    private RnineTDrive destDrive;

    private String jobID = "";
    private String uploadDirectoryID = "";
    private ArrayList<String> selectedItems = new ArrayList<String>();
    private Directory directory = Directory.getDirectoryInstance();

    public RnineT(TransferRequest request) {
        SourceDrive sourceDrive = (SourceDrive) request.getSourceDrive();
        DestDrive destDrive = (DestDrive) request.getDestDrive();
        this.selectedItems = sourceDrive.getSelectedItems();
        this.uploadDirectoryID = destDrive.getUploadDirectoryID();

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

    public String getJobID(){
        return jobID;
    }

    public void startTransfer(){
        selectedItems.forEach(selectedDirectoryID -> {
            String jobDirectoryPath = directory.getDirectoryPath(sourceDrive.getJobID());
            this.sourceDrive.download(selectedDirectoryID, jobDirectoryPath);
        });
    }
}