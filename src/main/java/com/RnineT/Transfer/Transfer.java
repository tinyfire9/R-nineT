package com.RnineT.Transfer;

import com.RnineT.Controller.TransferRequest;
import com.RnineT.Status.Database.Jobs.JobRepository;
import com.RnineT.Status.Status;
import com.RnineT.Controller.TransferRequest.*;
import com.RnineT.Transfer.Drives.Box.Box;
import com.RnineT.Transfer.Drives.Dropbox.Dropbox;
import com.RnineT.Transfer.Drives.GDrive;
import com.RnineT.Transfer.Drives.OneDrive;
import com.RnineT.Transfer.Drives.RnineTDrive;
import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Transfer.Storage.Directory;
import com.RnineT.Transfer.Response.*;

import java.util.ArrayList;

public class Transfer {
    private final String ONE_DRIVE = "one_drive";
    private final String GOOGLE_DRIVE = "google_drive";
    private final String BOX = "box";
    private final String DROPBOX = "dropbox";

    private RnineTDrive sourceDrive;
    private RnineTDrive destDrive;

    private Status status;
    private Callback callback;

    private String jobID = "";
    private String uploadDirectoryID = "";
    private ArrayList<String> selectedItems = new ArrayList<String>();
    private Directory directory = Directory.getDirectoryInstance();

    public Transfer(TransferRequest request, JobRepository jobRepository, DirectoryRepository directoryRepository) {
        SourceDrive sourceDrive = (SourceDrive) request.getSourceDrive();
        DestDrive destDrive = (DestDrive) request.getDestDrive();
        this.selectedItems = sourceDrive.getSelectedItems();
        this.uploadDirectoryID = destDrive.getUploadDirectoryID();
        this.status = new Status(jobRepository, directoryRepository);
        this.callback = new Callback();

        switch (sourceDrive.getName()){
            case ONE_DRIVE: {
                this.sourceDrive = new OneDrive(sourceDrive.getToken());
                break;
            }
            case GOOGLE_DRIVE: {
                this.sourceDrive = new GDrive(sourceDrive.getToken());
                break;
            }
            case BOX: {
                this.sourceDrive = new Box(sourceDrive.getToken());
                break;
            }
            case DROPBOX: {
                this.sourceDrive = new Dropbox(sourceDrive.getToken());
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
            case BOX: {
                this.destDrive = new Box(destDrive.getToken(), jobID);
                break;
            }
            case DROPBOX: {
                this.destDrive = new Dropbox(destDrive.getToken(), jobID);
            }
        }
    }

    public class Callback {
        public void onDownloadComplete(OnDownloadCompleteResponse onDownloadCompleteResponse){
            if(!onDownloadCompleteResponse.error.equals("")){
                System.out.println(onDownloadCompleteResponse.error);
                return;
            }

            String localDirectoryID = status.onDirectoryDownload(
                    jobID,
                    onDownloadCompleteResponse.directoryPath,
                    onDownloadCompleteResponse.directoryName,
                    onDownloadCompleteResponse.size
            );

            String cloudDirectoryID = "";
            if(directory.getDirectoryPath(jobID).equals(onDownloadCompleteResponse.directoryPath)) {
                cloudDirectoryID = uploadDirectoryID;
            } else {
                for(int i = 0; i < 10; i++){
                    try {
                        if(cloudDirectoryID.equals("")){
                            Thread.sleep(5000);
                            cloudDirectoryID = status.fetchCloudDirectoryID(jobID, onDownloadCompleteResponse.directoryPath);
                            System.out.println("polling on uploadDirectoryID for "+ onDownloadCompleteResponse.directoryPath + "/" + onDownloadCompleteResponse.directoryName);
                        } else {
                            break;
                        }
                    } catch (Exception e){}
                }
            }

            destDrive.upload(
                    localDirectoryID,
                    onDownloadCompleteResponse.directoryPath,
                    onDownloadCompleteResponse.directoryName,
                    cloudDirectoryID,
                    callback
            );
        }

        public void onUploadComplete(OnUploadCompleteResponse onUploadCompleteResponse){
            if(!onUploadCompleteResponse.error.equals("")){
                return;
            }

            String localDirectoryID = onUploadCompleteResponse.localDirectoryID;
            String localDirectoryPathAndName = status.getDirectoryPathAndNameByID(localDirectoryID);
            directory.removeDirectoryByPath(localDirectoryPathAndName);
            status.onDirectoryUpload(localDirectoryID, onUploadCompleteResponse.cloudDirectoryID);
        }
    }

    public String getJobID(){
        return jobID;
    }

    public Long getTotalSizeInBytes(){
        return this.sourceDrive.getTotalSizeInBytes(selectedItems);
    }

    public Long getTotalItemsCount(){
        return this.sourceDrive.getTotalItemsCount(selectedItems);
    }

    public void startTransfer(){
        selectedItems.forEach(selectedDirectoryID -> {
            String jobDirectoryPath = directory.getDirectoryPath(sourceDrive.getJobID());
            this.sourceDrive.download(selectedDirectoryID, jobDirectoryPath, callback);
        });
    }
}