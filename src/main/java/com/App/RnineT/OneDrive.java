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

import java.util.List;

public class OneDrive extends RnineTDrive<IDriveRequestBuilder>{
    OneDrive(String token){
        super(token);
    }

    OneDrive(String token, String jobID){
        super(token, jobID);
    }

    protected IDriveRequestBuilder initDriveClient(){
        String token = getToken();
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
    public boolean download(String directoryID, String downloadDirectoryPath) {
        IDriveRequestBuilder drive = getDrive();
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
                                    public void success(IDriveItemCollectionPage driveItemCollectionPage) {
                                        downloadSubDirectories(driveItemCollectionPage, subDirectoryPath);
                                    }

                                    @Override
                                    public void failure(ClientException e) {
                                        System.out.println("Error fetching sub-directories of " + driveItem.name + ".");
                                        e.printStackTrace();
                                    }
                                });
                    } else if(driveItem.file != null){
                        downloadFile(driveItem, downloadDirectoryPath);
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

    private void downloadFile(DriveItem driveItem, String downloadDirectoryPath){
        IDriveRequestBuilder drive = getDrive();
        drive
            .items(driveItem.id)
            .content()
            .buildRequest()
            .get(new ICallback<InputStream>() {
                @Override
                public void success(InputStream inputStream) {
                    try {
                        Files.copy(inputStream, Paths.get(downloadDirectoryPath + "/" + driveItem.name));
                        System.out.println("Downloaded (" + driveItem.size + " bytes)" + downloadDirectoryPath + "/" + driveItem.name );
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

    private void downloadSubDirectories(IDriveItemCollectionPage driveItemCollectionPage, String subDirectoryPath){
        IDriveRequestBuilder drive = getDrive();
        List<DriveItem> subDirectories = driveItemCollectionPage.getCurrentPage();

        for(int i = 0; i < subDirectories.size(); i++){
            if (i % 25 == 0 && i != 0){
                try {
                    Thread.sleep(15000);
                } catch (Exception e){
                    System.out.println("error on 15 sec delay");
                }
            }

            download(subDirectories.get(i).id, subDirectoryPath);
        }

        if(driveItemCollectionPage.getNextPage() == null){
            return;
        }

        driveItemCollectionPage
            .getNextPage()
            .buildRequest()
            .get(new ICallback<IDriveItemCollectionPage>() {
                @Override
                public void success(IDriveItemCollectionPage nextPageIDriveItemCollectionPage) {
                    downloadSubDirectories(nextPageIDriveItemCollectionPage, subDirectoryPath);
                }

                @Override
                public void failure(ClientException e) {
                    System.out.println("Error requesting next page. " );
                    e.printStackTrace();
                }
            });
    }

    @Override
    public boolean upload( String directoryPath, String directoryName, String uploadDirectoryID) {
        return false;
    }
}