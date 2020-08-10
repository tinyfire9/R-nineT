package com.RnineT.Drives;

import com.RnineT.Controller.RnineT;
import com.RnineT.Storage.Directory;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;

import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveRequestBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

public class OneDrive extends RnineTDrive<IDriveRequestBuilder> {
    private IGraphServiceClient graphClient;
    public OneDrive(String token){
        super(token);
    }

    public OneDrive(String token, String jobID){
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

        graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(authenticationProvider)
                        .buildClient();

        return graphClient
                .me()
                .drive();
    }

    @Override
    public boolean download(String directoryID, String downloadDirectoryPath, RnineT.Callback callback) {
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
                        callback.onDownloadComplete("", getJobID(), downloadDirectoryPath, driveItem.name, driveItem.size);
                        drive
                                .items(directoryID)
                                .children()
                                .buildRequest()
                                .get(new ICallback<IDriveItemCollectionPage>() {
                                    @Override
                                    public void success(IDriveItemCollectionPage driveItemCollectionPage) {
                                        downloadSubDirectories(driveItemCollectionPage, subDirectoryPath, callback);
                                    }

                                    @Override
                                    public void failure(ClientException e) {
//                                        System.out.println("Error fetching sub-directories of " + driveItem.name + ".");
//                                        e.printStackTrace();
                                        callback.onDownloadComplete("Error fetching sub-directories of " + driveItem.name + "  " + e, null, null, null, null);
                                    }
                                });
                    } else if(driveItem.file != null){
                        downloadFile(driveItem, downloadDirectoryPath, callback);
                    }
                }

                @Override
                public void failure(ClientException e) {
//                    System.out.println("Error fetching directory info of " + directoryID + e);
                    callback.onDownloadComplete("Error fetching directory info of " + directoryID + ": " + e, null, null, null, null);
                    e.printStackTrace();
                }
            });

        return false;
    }

    private void downloadFile(DriveItem driveItem, String downloadDirectoryPath, RnineT.Callback callback){
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
                        callback.onDownloadComplete("", getJobID(), downloadDirectoryPath, driveItem.name, driveItem.size);
                    } catch (Exception e) {
//                        System.out.println("Error downloading " + driveItem.name + ". " + e);
                        callback.onDownloadComplete("Error downloading " + driveItem.name + ". " + e, null, null, null, null);
                    }
                }

                @Override
                public void failure(ClientException e) {
//                    System.out.println("Error requesting to download " + driveItem.name + ". " + e);
                    callback.onDownloadComplete("Error requesting to download " + driveItem.name + ". " + e, null, null, null, null);
                }
            });
    }

    private void downloadSubDirectories(IDriveItemCollectionPage driveItemCollectionPage, String subDirectoryPath, RnineT.Callback callback){
        IDriveRequestBuilder drive = getDrive();
        List<DriveItem> subDirectories = driveItemCollectionPage.getCurrentPage();

        for(int i = 0; i < subDirectories.size(); i++){
            if (i % 25 == 0 && i != 0){
                try {
                    Thread.sleep(15000);
                } catch (Exception e){
                    System.out.println("error on 15 sec delay");
                    callback.onDownloadComplete("error on 15 sec delay", null, null, null, null);
                }
            }

            download(subDirectories.get(i).id, subDirectoryPath, callback);
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
                    downloadSubDirectories(nextPageIDriveItemCollectionPage, subDirectoryPath, callback);
                }

                @Override
                public void failure(ClientException e) {
//                    System.out.println("Error requesting next page. " );
//                    e.printStackTrace();
                    callback.onDownloadComplete("Error requesting next page. " + e, null, null, null, null);
                }
            });
    }

    @Override
    public boolean upload(String directoryPath, String directoryName, String uploadDirectoryID) {
        IDriveRequestBuilder drive = getDrive();
        String path = directoryPath + "/" + directoryName;
        File file = new File(path);

        if(file.isFile()){
            this.uploadFile(directoryPath, directoryName, uploadDirectoryID);
        } else if(file.isDirectory()){
            DriveItem driveItem = new DriveItem();
            driveItem.name = directoryName;
            driveItem.folder = new Folder();

            drive
                .items(uploadDirectoryID)
                .children()
                .buildRequest()
                .post(driveItem);
            return true;
        }

        return false;
    }

    public void uploadFile(String directoryPath, String directoryName, String uploadDirectoryID) {
        IDriveRequestBuilder drive = getDrive();
        UploadSession uploadSession = drive
                .items(uploadDirectoryID)
                .children(directoryName)
                .createUploadSession(new DriveItemUploadableProperties())
                .buildRequest()
                .post();

        String path = directoryPath + "/" + directoryName;
        File file = new File(path);
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (Exception e){
            System.out.println("Error reading " + path );
            System.out.println(e);
            return;
        }

        IProgressCallback<DriveItem> callback = new IProgressCallback<DriveItem>() {
            @Override
            public void progress(long uploaded, long total) {
//                System.out.format("uploaded %d bytes out of %d bytes\n", uploaded, total);
            }

            @Override
            public void success(DriveItem driveItem) {
                System.out.println("Uploaded " + driveItem.name + ", ID: " + driveItem.id);
            }

            @Override
            public void failure(ClientException e) {
                System.out.println("Error uploading " + path);
                System.out.println(e.getMessage());
            }
        };

        long streamSize = file.length();
        ChunkedUploadProvider<DriveItem> chunkedUploadProvider =
                new ChunkedUploadProvider<DriveItem>(uploadSession, graphClient, fileInputStream, streamSize, DriveItem.class);

        int[] config = { 320 * 1024 };

        try {
            chunkedUploadProvider.upload(callback, config);
        } catch (Exception e){
            System.out.println("Error calling upload for " + path );
        }
    }

}