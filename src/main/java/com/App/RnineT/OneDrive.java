package com.App.RnineT;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;

import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveRequestBuilder;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OneDrive implements RnineTDrive{
    private IDriveRequestBuilder getDriveClient(String token){
        IAuthenticationProvider authenticationProvider = new IAuthenticationProvider() {
            @Override
            public void authenticateRequest(IHttpRequest iHttpRequest) {
                iHttpRequest.addHeader("Authorization", "bearer " + token);
            }
        };

        IGraphServiceClient graphClient =
            GraphServiceClient
                .builder()
                .authenticationProvider(authenticationProvider)
                .buildClient();

        return graphClient
                .me()
                .drive();
    }

    @Override
    public boolean download(String token, String directoryID, String downloadDirectoryPath) {
        IDriveRequestBuilder drive = getDriveClient(token);

        drive
            .items(directoryID)
            .buildRequest()
            .get(new ICallback<DriveItem>() {
                @Override
                public void success(DriveItem driveItem) {
                    Directory directory = Directory.getDirectoryInstance();
                    String subDirectoryPath = downloadDirectoryPath + "/" + driveItem.name;

                    if(driveItem.folder != null){
                        directory.makeDir(subDirectoryPath);
                        drive
                                .items(directoryID)
                                .children()
                                .buildRequest()
                                .get(new ICallback<IDriveItemCollectionPage>() {
                                    @Override
                                    public void success(IDriveItemCollectionPage iDriveItemCollectionPage) {
                                        downloadSubDirectories(drive, token, iDriveItemCollectionPage, subDirectoryPath);
                                    }

                                    @Override
                                    public void failure(ClientException e) {
                                        System.out.println("Error fetching sub-directories of " + driveItem.name + ".");
                                        e.printStackTrace();
                                    }
                                });
                    } else if(driveItem.file != null){
                        downloadFile(drive, driveItem, downloadDirectoryPath);
                    }
                }

                @Override
                public void failure(ClientException e) {
                    System.out.println("Error fetching directory info of " + directoryID);
                    e.printStackTrace();
                }
            });

        return false;
    }

    private void downloadFile(IDriveRequestBuilder drive, DriveItem driveItem, String downloadDirectoryPath){
        drive
            .items(driveItem.id)
            .content()
            .buildRequest()
            .get(new ICallback<InputStream>() {
                @Override
                public void success(InputStream inputStream) {
                    try {
                        Files.copy(inputStream, Paths.get(downloadDirectoryPath + "/" + driveItem.name));
                    } catch (Exception e) {
                        System.out.println("Error downloading " + driveItem.name + ". " + e);
                    }
                }

                @Override
                public void failure(ClientException e) {
                    System.out.println("Error requesting to download " + driveItem.name + ". " + e);
                }
            });
    }

    private void downloadSubDirectories(IDriveRequestBuilder drive, String token, IDriveItemCollectionPage iDriveItemCollectionPage, String subDirectoryPath){
        List<DriveItem> subDirectories = iDriveItemCollectionPage.getCurrentPage();

        for(int i = 0; i < subDirectories.size(); i++){
            if (i % 25 == 0 && i != 0){
                try {
                    Thread.sleep(15000);
                } catch (Exception e){}
            }

            download(token, subDirectories.get(i).id, subDirectoryPath);
        }

        if(iDriveItemCollectionPage.getNextPage() == null){
            return;
        }

        iDriveItemCollectionPage
            .getNextPage()
            .buildRequest()
            .get(new ICallback<IDriveItemCollectionPage>() {
                @Override
                public void success(IDriveItemCollectionPage nextPageIDriveItemCollectionPage) {
                    downloadSubDirectories(drive, token, nextPageIDriveItemCollectionPage, subDirectoryPath);
                }

                @Override
                public void failure(ClientException e) {
                    System.out.println("Error requesting next page. " );
                    e.printStackTrace();
                }
            });
    }

    @Override
    public boolean upload(String token, String directoryPath, String directoryName, String uploadDirectoryID) {
        return false;
    }
}