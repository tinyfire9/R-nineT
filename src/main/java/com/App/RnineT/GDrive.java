package com.App.RnineT;

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
import java.util.Collections;
import java.util.List;

public class GDrive extends RnineTDrive<Drive> {
    GDrive(String token){
        super(token);
    }

    GDrive(String token, String jobID){
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

    private boolean uploadFile(String directoryPath, String fileName, String uploadDirectoryID){
        try {
            Drive drive = getDrive();
            File file = new File();

            file.setParents(Collections.singletonList(uploadDirectoryID));
            file.setName(fileName);

            String filePath = directoryPath + "/" + fileName;
            java.io.File jFile = new java.io.File(filePath);
            String type = Files.probeContentType(jFile.toPath());
            FileContent fileContent = new FileContent(type, jFile);

            drive
                    .files()
                    .create(file, fileContent)
                    .execute();

            return true;
        } catch (Exception e) {
            System.out.println("Error uploading " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean upload(String directoryPath, String directoryName, String gDriveUploadDirectoryID){
        String filePath = directoryPath + "/" + directoryName;
        java.io.File jFile = new java.io.File(filePath);
        Drive drive = getDrive();

        try{
            if(jFile.isDirectory()){
                File gDriveFile = new File()
                        .setName(directoryName)
                        .setParents(Collections.singletonList(gDriveUploadDirectoryID))
                        .setMimeType("application/vnd.google-apps.folder");

                String gDriveSubDirectoryID = drive
                        .files()
                        .create(gDriveFile)
                        .execute()
                        .getId();

                String[] subDirectories = jFile.list();
                for(int i = 0; i < subDirectories.length; i++){
                    this.upload(filePath, subDirectories[i], gDriveSubDirectoryID);
                }
            } else {
                this.uploadFile(directoryPath, directoryName, gDriveUploadDirectoryID);
            }
        } catch (Exception e){
            System.out.println("Error uploading " + e.getMessage());
        }

        return  true;
    }

    @Override
    public boolean download(String directoryID, String downloadDirectoryPath){
        try {
            Drive drive = getDrive();
            Directory directory = Directory.getDirectoryInstance();
            Drive.Files files = drive.files();

            File file = files.get(directoryID).execute();
            String subDirectoryDownloadPath = downloadDirectoryPath + "/" + file.getName();

            String mimeType = file.getMimeType();
            if(mimeType.contains("folder")){
                directory.makeDir(subDirectoryDownloadPath);

                List<File> subdirectories = drive
                        .files()
                        .list()
                        .setQ(String.format("'%s' in parents", directoryID))
                        .execute()
                        .getFiles();

                for(int i = 0; i < subdirectories.size(); i++){
                    this.download(subdirectories.get(i).getId(), subDirectoryDownloadPath);
                }

            } else if(!mimeType.contains("google-apps")) {
                FileOutputStream outputStream = new FileOutputStream(subDirectoryDownloadPath);
                drive
                        .files()
                        .get(directoryID)
                        .executeMediaAndDownloadTo(outputStream);
                System.out.println("Downloaded " + subDirectoryDownloadPath);
            }

            return true;
        } catch (Exception e){
            System.out.println("Error downloading directories: " + directoryID);
            System.out.println(e.getMessage());

            return false;
        }
    }
}