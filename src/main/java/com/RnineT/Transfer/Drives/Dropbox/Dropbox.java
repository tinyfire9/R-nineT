package com.RnineT.Transfer.Drives.Dropbox;

import com.RnineT.Transfer.Drives.RnineTDrive;
import com.RnineT.Transfer.Response;
import com.RnineT.Transfer.Storage.Directory;
import com.RnineT.Transfer.Transfer;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Dropbox extends RnineTDrive<DbxClientV2> {
    private final String ITEM_TYPE_FILE = "file";
    private final String ITEM_TYPE_FOLDER = "folder";

    public Dropbox(String token){
        super(token);
    }

    public Dropbox(String token, String jobID){
        super(token, jobID);
    }

    @Override
    protected DbxClientV2 initDriveClient() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("R nineT").build();
        DbxClientV2 client = new DbxClientV2(config, getToken());

        return client;
    }

    private Map<String, String> convertMetadataToMap(Metadata metadata){
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> fileMetadataMap = mapper.readValue(metadata.toString(), new TypeReference<Map<String, String>>() {});

            return fileMetadataMap;
        } catch (Exception e){
            e.printStackTrace();
        }

        return Collections.emptyMap();
    }

    private String getItemType(Map<String, String> metadataMap){
        String tag = metadataMap.get(".tag");
        if(tag.equals(ITEM_TYPE_FOLDER)){
            return ITEM_TYPE_FOLDER;
        } else if(tag.equals(ITEM_TYPE_FILE)){
            return ITEM_TYPE_FILE;
        }

        return "";
    }

    @Override
    public boolean download(String directoryPath, String downloadDirectoryPath, Transfer.Callback callback) {
        Directory directory = Directory.getDirectoryInstance();
        directoryPath = directoryPath.equals("root") ? "" : directoryPath;
        try{
            DbxUserFilesRequests files = getDrive().files();
            Metadata metadata = files.getMetadata(directoryPath);
            Map<String, String> metadataMap = convertMetadataToMap(metadata);
            String itemType = metadataMap.get(".tag");

            if(itemType.equals(ITEM_TYPE_FILE)){
                String _directoryPath = directoryPath;
                CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            DbxDownloader<FileMetadata> downloader = files.download(_directoryPath);
                            FileOutputStream fileOutputStream = new FileOutputStream(downloadDirectoryPath + "/" + metadata.getName());
                            downloader.download(fileOutputStream);
                            fileOutputStream.close();

                            return true;
                        } catch (Exception exception){
                            exception.printStackTrace();
                            return false;
                        }
                    })
                    .thenAccept((isDownloaded) -> {
                        if(isDownloaded){
                            Long size = Long.parseLong(metadataMap.get("size"));
                            callback.onDownloadComplete(
                                new Response.OnDownloadCompleteResponse("", getJobID(), downloadDirectoryPath, metadata.getName(), size)
                            );
                        }
                    });
            } else if(itemType.equals(ITEM_TYPE_FOLDER)){
                directory.makeDir(downloadDirectoryPath + "/" + metadata.getName());
                callback.onDownloadComplete(
                    new Response.OnDownloadCompleteResponse("", getJobID(), downloadDirectoryPath, metadata.getName(), 0L)
                );

                String _directoryPath = directoryPath;
                CompletableFuture
                    .runAsync(() -> {
                        try {
                            ListFolderResult listFolderResult = files.listFolder(_directoryPath);
                            while(true){
                                listFolderResult
                                    .getEntries()
                                    .forEach(subDirectory -> {
                                        this.download(subDirectory.getPathLower(), downloadDirectoryPath + "/" + metadata.getName(), callback);
                                    });

                                if(listFolderResult.getHasMore()){
                                    listFolderResult = files.listFolderContinue(listFolderResult.getCursor());
                                } else {
                                    break;
                                }
                            }
                        } catch (Exception exception){
                            exception.printStackTrace();
                        }
                    });
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean upload(String localDirectoryID, String directoryPath, String directoryName, String uploadDirectoryPath, Transfer.Callback callback) {
        String path = directoryPath + "/" + directoryName;
        File file = new File(path);
        if(file.isFile()){
            CompletableFuture.
                supplyAsync(() -> {
                    try {
                        FileInputStream fileOutputStream = new FileInputStream(path);
                        return getDrive()
                            .files()
                            .uploadBuilder(uploadDirectoryPath + "/" + directoryName)
                            .uploadAndFinish(fileOutputStream)
                            .getPathLower();
                    } catch (Exception e){
                        e.printStackTrace();
                        return "";
                    }
                })
                .thenAccept((newFileID) -> {
                    if(newFileID.equals("")) {
                        callback.onUploadComplete(
                            Response.OnUploadCompleteResponse.makeErrorResponseObject("Error uploading " + path)
                        );
                    } else {
                        callback.onUploadComplete(
                            new Response.OnUploadCompleteResponse("", localDirectoryID, newFileID)
                        );
                    }
                });
        } else if(file.isDirectory()) {
            try {
                String id = getDrive()
                    .files()
                    .createFolderV2(uploadDirectoryPath + "/" + directoryName)
                    .getMetadata()
                    .getPathLower();
                callback.onUploadComplete(
                    new Response.OnUploadCompleteResponse("", localDirectoryID, id)
                );
            } catch (Exception e){
                e.printStackTrace();
                callback.onUploadComplete(
                    Response.OnUploadCompleteResponse.makeErrorResponseObject("Error uploading " + path)
                );
            }
        }

        return false;
    }

    @Override
    public void fetchTotalSizeInBytesAndTotalItemsCount(String directoryPath, Map<String, Long> output) {
        try {
            Metadata metadata = getDrive()
                .files()
                .getMetadata(directoryPath);
            Map<String, String> metadataMap = convertMetadataToMap(metadata);
            String itemType = getItemType(metadataMap);
            if(itemType.equals(ITEM_TYPE_FILE)){
                long size = Long.parseLong(metadataMap.get("size"));
                output.replace("totalSizeInBytes", output.get("totalSizeInBytes") + size);
                output.replace("totalItemsCount", output.get("totalItemsCount") + 1);
            } else if(itemType.equals(ITEM_TYPE_FOLDER)){
                output.replace("totalItemsCount", output.get("totalItemsCount") + 1);
                while(true){
                    ListFolderResult listFolderResult = getDrive().files().listFolder(directoryPath);
                    listFolderResult
                        .getEntries()
                        .forEach(subDir -> {
                            this.fetchTotalSizeInBytesAndTotalItemsCount(subDir.getPathLower(), output);
                        });

                    if(listFolderResult.getHasMore()){
                        String cursor = listFolderResult.getCursor();
                        listFolderResult = getDrive().files().listFolderContinue(cursor);
                    } else {
                        break;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}