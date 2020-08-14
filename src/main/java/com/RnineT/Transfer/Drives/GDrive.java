package com.RnineT.Transfer.Drives;

import com.RnineT.Transfer.Transfer;
import com.RnineT.Transfer.Storage.Directory;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import com.google.api.services.drive.model.File;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GDrive extends RnineTDrive<Drive> {
    public GDrive(String token){
        super(token);
    }

    public GDrive(String token, String jobID){
        super(token, jobID);
    }

    protected Drive initDriveClient(){
        String token = getToken();
        try {
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(token);
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

            return new Drive.Builder(httpTransport, jacksonFactory, credential)
                    .build();
        } catch (Exception e){
            System.out.println("Error creating a Drive client");
            System.out.println(e.getMessage());

            return null;
        }
    }

    @Override
    protected void fetchTotalSizeInBytesAndTotalItemsCount(String directoryID, Map<String, Long> output){
        Drive drive = getDrive();
        try {
            File file = drive
                    .files()
                    .get(directoryID)
                    .setFields("id, name, mimeType, size")
                    .execute();

            if(file.getMimeType().contains("folder")){
                output.replace("totalItemsCount", output.get("totalItemsCount") + 1);
                ArrayList<String> subDirectories = new ArrayList<>();
                drive
                    .files()
                    .list()
                    .setQ(String.format("'%s' in parents", directoryID))
                    .execute()
                    .getFiles()
                    .forEach(f -> {
                        this.fetchTotalSizeInBytesAndTotalItemsCount(f.getId(), output);
                    });
            } else if(!file.getMimeType().contains("google-apps")){
                output.replace("totalItemsCount", output.get("totalItemsCount") + 1);
                output.replace("totalSizeInBytes", output.get("totalSizeInBytes") + file.getSize());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String uploadFile(String directoryPath, String fileName, String uploadDirectoryID){
        try {
            Drive drive = getDrive();
            File file = new File();

            file.setParents(Collections.singletonList(uploadDirectoryID));
            file.setName(fileName);

            String filePath = directoryPath + "/" + fileName;
            java.io.File jFile = new java.io.File(filePath);
            String type = Files.probeContentType(jFile.toPath());
            FileContent fileContent = new FileContent(type, jFile);

            File f = drive
                    .files()
                    .create(file, fileContent)
                    .execute();

            return f.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean upload(String localDirectoryID, String directoryPath, String directoryName, String gDriveUploadDirectoryID, Transfer.Callback callback){
        String filePath = directoryPath + "/" + directoryName;
        java.io.File jFile = new java.io.File(filePath);
        Drive drive = getDrive();

        try{
            if(jFile.isDirectory()){
                File gDriveFile = new File()
                        .setName(directoryName)
                        .setParents(Collections.singletonList(gDriveUploadDirectoryID))
                        .setMimeType("application/vnd.google-apps.folder");

                String gDriveDirectoryID = drive
                        .files()
                        .create(gDriveFile)
                        .execute()
                        .getId();

                callback.onUploadComplete("", localDirectoryID, gDriveDirectoryID);
            } else {
                CompletableFuture.supplyAsync(() -> {
                    return this.uploadFile(directoryPath, directoryName, gDriveUploadDirectoryID);
                })
                .thenAccept((gDriveDirectoryID) -> {
                    if(gDriveDirectoryID.equals("")){
                        callback.onUploadComplete("Error uploading " + directoryPath + "/" + directoryName, "", "");
                    } else {
                        callback.onUploadComplete("", localDirectoryID, gDriveDirectoryID);
                    }
                });
            }
        } catch (Exception e){
            callback.onUploadComplete("Error uploading " + directoryPath + "/" + directoryName, "", "");
            e.printStackTrace();
        }

        return  true;
    }

    @Override
    public boolean download(String directoryID, String downloadDirectoryPath, Transfer.Callback callback){
        try {
            Drive drive = getDrive();
            Directory directory = Directory.getDirectoryInstance();
            Drive.Files files = drive.files();

            File file = files
                    .get(directoryID)
                    .setFields("id, name, mimeType, size")
                    .execute();

            String subDirectoryDownloadPath = downloadDirectoryPath + "/" + file.getName();

            String mimeType = file.getMimeType();
            if(mimeType.contains("folder")){
                directory.makeDir(subDirectoryDownloadPath);
                callback.onDownloadComplete(
                        new Transfer.OnDownloadCompleteResponse("", getJobID(), downloadDirectoryPath, file.getName(), 0L)
                );

                List<File> subdirectories = drive
                        .files()
                        .list()
                        .setQ(String.format("'%s' in parents", directoryID))
                        .execute()
                        .getFiles();

                for(int i = 0; i < subdirectories.size(); i++){
                    this.download(subdirectories.get(i).getId(), subDirectoryDownloadPath, callback);
                }
            } else if(!mimeType.contains("google-apps")) {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(subDirectoryDownloadPath);
                        drive
                                .files()
                                .get(directoryID)
                                .executeMediaAndDownloadTo(outputStream);
                        return true;
                    }catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .thenAccept((isDownloaded) -> {
                    if(isDownloaded == true) {
                        callback.onDownloadComplete(
                                new Transfer.OnDownloadCompleteResponse("", getJobID(), downloadDirectoryPath, file.getName(), file.getSize())
                        );
                    } else {
                        callback.onDownloadComplete(
                                Transfer.OnDownloadCompleteResponse.makeErrorResponseObject("Error downloading file async")
                        );
                    }
                });
            }

            return true;
        } catch (Exception e){
            e.printStackTrace();
            callback.onDownloadComplete(
                    Transfer.OnDownloadCompleteResponse.makeErrorResponseObject("Error downloading directories: " + directoryID + ". " + e)
            );

            return false;
        }
    }
}