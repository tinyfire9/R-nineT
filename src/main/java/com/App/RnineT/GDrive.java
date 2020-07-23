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

public class GDrive implements RnineTDrive {
    private static GDrive gDriveInstance;

    public static GDrive getGDriveInstance(){
        if(gDriveInstance == null){
            gDriveInstance = new GDrive();
        }

        return gDriveInstance;
    }

    private Drive getDriveClient(String token){
        try {
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(token);
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

            Drive driveClient = new Drive.Builder(httpTransport, jacksonFactory, credential)
                    .build();

            return driveClient;
        } catch (Exception e){
            System.out.println("Error creating a Drive client");
            System.out.println(e.getMessage());

            return null;
        }
    }

    public boolean download(String token, String directoryID, String downloadDirectoryPath){
        try {
            Directory directory = Directory.getDirectoryInstance();
            Drive driveClient = getDriveClient(token);
            Drive.Files files = driveClient.files();

            File file = files.get(directoryID).execute();
            String subDirectoryDownloadPath = downloadDirectoryPath + "/" + file.getName();

            String mimeType = file.getMimeType();
            if(mimeType.contains("folder")){
                directory.makeDir(subDirectoryDownloadPath);

                List<File> subdirectories = driveClient
                        .files()
                        .list()
                        .setQ(String.format("'%s' in parents", directoryID))
                        .execute()
                        .getFiles();

                for(int i = 0; i < subdirectories.size(); i++){
                    this.download(token, subdirectories.get(i).getId(), subDirectoryDownloadPath);
                }

            } else if(!mimeType.contains("google-apps")) {
                FileOutputStream outputStream = new FileOutputStream(subDirectoryDownloadPath);
                driveClient
                        .files()
                        .get(directoryID)
                        .executeMediaAndDownloadTo(outputStream);
            }

            return true;
        } catch (Exception e){
            System.out.println("Error downloading directories: " + directoryID);
            System.out.println(e);

            return false;
        }
    }

    private boolean uploadFile(String token, String directoryPath, String fileName, String uploadDirectoryID){
        try {
            File file = new File();

            file.setParents(Collections.singletonList(uploadDirectoryID));
            file.setName(fileName);

            String filePath = directoryPath + "/" + fileName;
            java.io.File jFile = new java.io.File(filePath);
            String type = Files.probeContentType(jFile.toPath());
            FileContent fileContent = new FileContent(type, jFile);

            getDriveClient(token)
                    .files()
                    .create(file, fileContent)
                    .execute();

            return true;
        } catch (Exception e) {
            System.out.println("Error uploading " + e.getMessage());
            return false;
        }
    }

    public boolean upload(String token, String directoryPath, String directoryName, String gDriveUploadDirectoryID){
        String filePath = directoryPath + "/" + directoryName;
        java.io.File jFile = new java.io.File(filePath);

        try{
            if(jFile.isDirectory()){
                File gDriveFile = new File()
                        .setName(directoryName)
                        .setParents(Collections.singletonList(gDriveUploadDirectoryID))
                        .setMimeType("application/vnd.google-apps.folder");

                String gDriveSubDirectoryID = getDriveClient(token)
                        .files()
                        .create(gDriveFile)
                        .execute()
                        .getId();

                String[] subDirectories = jFile.list();
                for(int i = 0; i < subDirectories.length; i++){
                    this.upload(token, filePath, subDirectories[i], gDriveSubDirectoryID);
                }
            } else {
                this.uploadFile(token, directoryPath, directoryName, gDriveUploadDirectoryID);
            }
        } catch (Exception e){
            System.out.println("Error uploading " + e.getMessage());
        }

        return  true;
    }
}