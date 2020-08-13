package com.RnineT.Transfer;

import com.RnineT.Controller.TransferRequest;
import com.RnineT.Status.Database.Jobs.JobRepository;
import com.RnineT.Status.Status;
import com.RnineT.Controller.TransferRequest.*;
import com.RnineT.Transfer.Drives.GDrive;
import com.RnineT.Transfer.Drives.OneDrive;
import com.RnineT.Transfer.Drives.RnineTDrive;
import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Transfer.Storage.Directory;

import java.util.ArrayList;

public class Transfer {
    private final String ONE_DRIVE = "one_drive";
    private final String GOOGLE_DRIVE = "google_drive";

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

    public static class OnDownloadCompleteResponse{
        public String error;
        public String jobID;
        public String directoryPath;
        public String directoryName;
        public Long size;

        public OnDownloadCompleteResponse(){};
        public OnDownloadCompleteResponse(String error, String jobID, String directoryPath, String directoryName, Long size){
            this.error = error;
            this.jobID = jobID;
            this.directoryPath = directoryPath;
            this.directoryName = directoryName;
            this.size = size;
        }

        public static OnDownloadCompleteResponse makeErrorResponseObject(String errorMessage){
            OnDownloadCompleteResponse response = new OnDownloadCompleteResponse();
            response.error = errorMessage;

            return response;
        }
    }

    public class Callback {
        public void onDownloadComplete(OnDownloadCompleteResponse onDownloadCompleteResponse){
            if(!onDownloadCompleteResponse.error.equals("")){

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

        public void onUploadComplete(String error, String localDirectoryID, String cloudDirectoryID){
            if(!error.equals("")){
                return;
            }

            status.onDirectoryUpload(localDirectoryID, cloudDirectoryID);
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