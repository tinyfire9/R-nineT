package com.App.RnineT;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;

import com.google.api.services.drive.model.File;

import java.io.*;
import java.util.List;

public class GDrive implements RnineTDrive {
    private static GDrive gDriveInstance;

    public static GDrive getGDriveInstance(){
        if(gDriveInstance == null){
            gDriveInstance = new GDrive();
        }

        return gDriveInstance;
    }

    Drive driveClient;

    private Drive getDriveClient(String token){
        try {
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(token);
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

            driveClient = new Drive.Builder(httpTransport, jacksonFactory, credential)
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

    public boolean upload(String token, String path){

        return true;
    }
}
