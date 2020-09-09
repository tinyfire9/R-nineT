package com.RnineT.Transfer.Drives;

import com.RnineT.Transfer.Response;
import com.RnineT.Transfer.Transfer;
import com.RnineT.Transfer.Storage.Directory;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;

import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.requests.extensions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    protected void fetchTotalSizeInBytesAndTotalItemsCount(String directoryID, Map<String, Long> output) {
        IDriveRequestBuilder drive = getDrive();
        DriveItem driveItem = drive.items(directoryID).buildRequest().get();

        if(driveItem.folder != null){
            output.replace("totalItemsCount", output.get("totalItemsCount") + 1);
            IDriveItemCollectionPage collectionPage = drive.items(directoryID).children().buildRequest().get();

            List<DriveItem> driveItems = collectionPage.getCurrentPage();
            while(driveItems != null) {
                driveItems.forEach(item -> {
                    this.fetchTotalSizeInBytesAndTotalItemsCount(item.id, output);
                });

                if(collectionPage.getNextPage() == null){
                    driveItems = null;
                } else {
                    driveItems = collectionPage.getNextPage().buildRequest().get().getCurrentPage();
                }
            }

        } else if(driveItem.file != null){
            output.replace("totalItemsCount", output.get("totalItemsCount") + 1);
            output.replace("totalSizeInBytes", output.get("totalSizeInBytes") + driveItem.size);
        }
    }

    @Override
    public boolean download(String directoryID, String downloadDirectoryPath, Transfer.Callback callback) {
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
                        System.out.println("Folder Type size: " + downloadDirectoryPath + ", size = " + driveItem.size);
                        callback.onDownloadComplete(
                                new Response.OnDownloadCompleteResponse("", getJobID(), directoryID, downloadDirectoryPath, driveItem.name, 0L)
                        );
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
                                    e.printStackTrace();
                                    callback.onDownloadComplete(
                                        new Response.OnDownloadCompleteResponse(
                                            "Error fetching sub-directories of " + driveItem.name + "  " + e,
                                            getJobID(),
                                            directoryID,
                                            downloadDirectoryPath,
                                            driveItem.name,
                                            0L
                                        )
                                    );
                                }
                            });
                    } else if(driveItem.file != null){
                        downloadFile(driveItem, directoryID, downloadDirectoryPath, callback);
                    }
                }

                @Override
                public void failure(ClientException e) {
                    e.printStackTrace();
                    callback.onDownloadComplete(
                        new Response.OnDownloadCompleteResponse(
                            "Error fetching directory info of " + directoryID + ": ",
                            getJobID(),
                            directoryID,
                            downloadDirectoryPath,
                            "",
                            0L
                        )
                    );
                }
            });

        return false;
    }

    private void downloadFile(DriveItem driveItem, String directoryID, String downloadDirectoryPath, Transfer.Callback callback){
        try{
            Thread.sleep(5000);
        } catch (Exception e){ }

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
                        callback.onDownloadComplete(
                                new Response.OnDownloadCompleteResponse("", getJobID(), directoryID, downloadDirectoryPath, driveItem.name, driveItem.size)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onDownloadComplete(
                            new Response.OnDownloadCompleteResponse(
                                "Error downloading " + driveItem.name + ". " + e,
                                getJobID(),
                                directoryID,
                                downloadDirectoryPath,
                                driveItem.name,
                                0L
                            )
                        );
                    }
                }

                @Override
                public void failure(ClientException e) {
                    e.printStackTrace();
                    callback.onDownloadComplete(
                        new Response.OnDownloadCompleteResponse(
                            "Error requesting to download " + driveItem.name + ". " + e,
                            getJobID(),
                            directoryID,
                            downloadDirectoryPath,
                            driveItem.name,
                            0L
                        )
                    );
                }
            });
    }

    private void downloadSubDirectories(IDriveItemCollectionPage driveItemCollectionPage, String subDirectoryPath, Transfer.Callback callback){
        List<DriveItem> subDirectories = driveItemCollectionPage.getCurrentPage();

        for(int i = 0; i < subDirectories.size(); i++){
            try {
                Thread.sleep(5000);
            } catch (Exception e){ }

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
                    e.printStackTrace();
                    callback.onDownloadComplete(Response.OnDownloadCompleteResponse.makeErrorResponseObject("Error requesting next page. " + e));
                }
            });
    }

    @Override
    public boolean upload(String localDirectoryID, String directoryPath, String directoryName, String uploadDirectoryID, Transfer.Callback callback) {
        IDriveRequestBuilder drive = getDrive();
        String path = directoryPath + "/" + directoryName;
        File file = new File(path);

        if(file.isFile()){
            this.uploadFile(localDirectoryID, directoryPath, directoryName, uploadDirectoryID, callback);
        } else if(file.isDirectory()){
            DriveItem driveItem = new DriveItem();
            driveItem.name = directoryName;
            driveItem.folder = new Folder();

            String oneDriveDirectoryID = drive
                .items(uploadDirectoryID)
                .children()
                .buildRequest()
                .post(driveItem).id;

            callback.onUploadComplete(new Response.OnUploadCompleteResponse("", localDirectoryID, oneDriveDirectoryID));

            return true;
        }

        return false;
    }

    public void uploadFile(String localDirectoryID, String directoryPath, String directoryName, String uploadDirectoryID, Transfer.Callback callback) {
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

        IProgressCallback<DriveItem> progressCallback = new IProgressCallback<DriveItem>() {
            @Override
            public void progress(long uploaded, long total) {}

            @Override
            public void success(DriveItem driveItem) {
                System.out.println("Uploaded " + driveItem.name + ", ID: " + driveItem.id);
                callback.onUploadComplete(new Response.OnUploadCompleteResponse("", localDirectoryID, driveItem.id));
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
            chunkedUploadProvider.upload(progressCallback, config);
        } catch (Exception e){
            System.out.println("Error calling upload for " + path );
        }
    }

}