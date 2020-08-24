package com.RnineT.Transfer.Drives.Box;

import com.RnineT.Transfer.Drives.RnineTDrive;
import com.RnineT.Transfer.Response;
import com.RnineT.Transfer.Storage.Directory;
import com.RnineT.Transfer.Transfer;
import com.box.sdk.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Box extends RnineTDrive<BoxAPIConnection> {
    private ArrayList<String> scopes = new ArrayList<String>();
    public Box(String token){
        super(token);
        this.addScopes();
    }

    public Box(String token, String jobID){
        super(token, jobID);
        this.addScopes();
    }

    private void addScopes(){
        scopes.add("item_upload");
        scopes.add("item_download");
        scopes.add("item_delete");
        scopes.add("item_rename");
    }

    @Override
    protected BoxAPIConnection initDriveClient() {
        BoxAPIConnection apiConnection = new BoxAPIConnection("GL4AR7kUAgGY5nJxKFJHfXHuxsoFunyB");

        return apiConnection;
    }

    @Override
    public boolean download(String directoryID, String downloadDirectoryPath, Transfer.Callback callback) {
        BoxFile boxFile = null;
        BoxFolder boxFolder = null;
        String itemName = "";
        Long itemSize = 0L;

        Item boxItem = getItem(directoryID);
        Directory directory = Directory.getDirectoryInstance();

        if(boxItem.type.equals(Item.FILE)){
            boxFile = (BoxFile) boxItem.item;
            BoxFile.Info info = (BoxFile.Info)boxItem.itemInfo;
            itemName = info.getName();
            itemSize = info.getSize();
        } else if(boxItem.type.equals(Item.FOLDER)){
            boxFolder = (BoxFolder) boxItem.item;
            BoxFolder.Info info = (BoxFolder.Info) boxItem.itemInfo;
            itemName = info.getName();
        } else {
            return false;
        }

        String subDirectoryPath = downloadDirectoryPath + "/" + itemName;
        if(boxFile != null){
            BoxFile finalBoxFile = boxFile;
            String finalItemName = itemName;
            Long finalItemSize = itemSize;
            CompletableFuture
                .supplyAsync(() -> {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(subDirectoryPath);
                        finalBoxFile.download(outputStream);
                        outputStream.close();

                        return new Response.OnDownloadCompleteResponse(
                                "",
                                getJobID(),
                                downloadDirectoryPath,
                                finalItemName,
                                finalItemSize
                        );
                    } catch (Exception e){
                        e.printStackTrace();
                        return Response
                            .OnDownloadCompleteResponse
                            .makeErrorResponseObject("Error downloading " + subDirectoryPath + ": " + e);
                    }
                })
                .thenAccept((response) -> {
                    callback.onDownloadComplete(response);
                });
        } else if(boxFolder != null) {
            directory.makeDir(subDirectoryPath);
            callback.onDownloadComplete(
                    new Response.OnDownloadCompleteResponse(
                            "",
                            getJobID(),
                            downloadDirectoryPath,
                            itemName,
                            0L
                    )
            );
            boxFolder
                .getChildren()
                .forEach(item -> {
                    this.download(item.getID(), subDirectoryPath, callback);
                });
        }
        return false;
    }

    @Override
    public boolean upload(String localDirectoryID, String directoryPath, String directoryName, String uploadDirectoryID, Transfer.Callback callback) {
        BoxFolder boxFolder = uploadDirectoryID.equals("root") ? BoxFolder.getRootFolder(getDrive()) : new BoxFolder(getDrive(), uploadDirectoryID);
        File file = new File(directoryPath + "/" + directoryName);
        try{
            if(file.isFile()){
                FileInputStream fileInputStream = new FileInputStream(file);
                if(file.length() < 20000000) {
                    boxFolder.uploadFile(fileInputStream, directoryName);
                } else {
                    boxFolder.uploadLargeFile(fileInputStream, directoryName, file.length());
                }
            } else if(file.isDirectory()){
                boxFolder.createFolder(directoryName);
            } else {
                callback.onUploadComplete(
                    Response.OnUploadCompleteResponse.makeErrorResponseObject("Error: Item is neither a file or a folder")
                );
                return false;
            }

            callback.onUploadComplete(
                new Response.OnUploadCompleteResponse("", localDirectoryID, uploadDirectoryID)
            );

            return true;
        } catch (Exception e){
            e.printStackTrace();
            callback.onUploadComplete(
                Response.OnUploadCompleteResponse.makeErrorResponseObject("Error uploading " + e.getMessage())
            );
            return false;
        }
    }

    private Item getItem(String directoryID){
        try {
            BoxFile boxFile = new BoxFile(getDrive(), directoryID);
            BoxFile.Info boxFileInfo = boxFile.getInfo("name", "id", "size", "type");
            if(boxFileInfo instanceof BoxFile.Info){
                return new Item(Item.FILE, boxFile, boxFileInfo);
            }
        } catch (Exception e){
            try {
                BoxFolder boxFolder = new BoxFolder(getDrive(), directoryID);
                BoxFolder.Info boxFolderInfo = boxFolder.getInfo("name", "id", "size", "type");
                if(boxFolderInfo instanceof BoxFolder.Info){
                    return new Item(Item.FOLDER, boxFolder, boxFolderInfo);
                }
            } catch (Exception exception){
                return null;
            }
        }

        return null;
    }

    @Override
    protected void fetchTotalSizeInBytesAndTotalItemsCount(String directoryID, Map output) {
        Item item = getItem(directoryID);
        if(item.type.equals(Item.FILE)){
            BoxFile.Info boxFileInfo = (BoxFile.Info) item.itemInfo;
            output.replace("totalSizeInBytes", (Long) output.get("totalSizeInBytes") + boxFileInfo.getSize());
            output.replace("totalItemsCount", (Long) output.get("totalItemsCount") + 1);
        } else if(item.type.equals(Item.FOLDER)){
            BoxFolder boxFolder = (BoxFolder) item.item;
            output.replace("totalItemsCount", (Long) output.get("totalItemsCount") + 1);
            boxFolder
                .forEach(subDirectory -> {
                    this.fetchTotalSizeInBytesAndTotalItemsCount(subDirectory.getID(), output);
                });
        }
    }
}