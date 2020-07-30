package com.App.RnineT;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Map;

public class TransferRequest {
    private SourceDrive sourceDrive;
    private DestDrive destDrive;

    @JsonProperty("src")
    private void mapSourceDrive(Map<String, Object> src){
        String name = (String) src.get("drive");
        Map<String, String> authData = (Map<String, String>)src.get("authData");
        Map<String, ArrayList<String>> transferData = (Map<String, ArrayList<String>>)src.get("transferData");

        String token = authData.get("token");
        ArrayList<String> selectedItems = transferData.get("selectedItems");

        sourceDrive = new SourceDrive(name, token, selectedItems);
    }

    @JsonProperty("dest")
    private void mapDestDrive(Map<String, Object> dest){
        String name = (String) dest.get("drive");
        Map<String, String> authData = (Map<String, String>) dest.get("authData");
        Map<String, String> transferData = (Map<String, String>) dest.get("transferData");
        String token = authData.get("token");
        String uploadDirectoryID = transferData.get("uploadDirectoryID");

        this.destDrive = new DestDrive(name, token, uploadDirectoryID);
    }

    public Drive getDestDrive() {
        return destDrive;
    }

    public Drive getSourceDrive(){
        return sourceDrive;
    }

    private class Drive {
        private String name;
        private String token;

        Drive(String name, String token){
            this.name = name;
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public String getName() {
            return name;
        }
    }

    public class SourceDrive extends Drive {
        private ArrayList<String> selectedItems;

        public SourceDrive(String name, String token, ArrayList<String> selectedItems){
            super(name, token);
            this.selectedItems = selectedItems;
        }

        public ArrayList<String> getSelectedItems() {
            return selectedItems;
        }

    }

    public class DestDrive extends Drive {
        private String uploadDirectoryID;

        DestDrive(String name, String token, String uploadDirectoryID){
            super(name, token);
            this.uploadDirectoryID = uploadDirectoryID;
        }

        public String getUploadDirectoryID() {
            return uploadDirectoryID;
        }
    }
}
