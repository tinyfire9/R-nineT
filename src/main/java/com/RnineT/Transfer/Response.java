package com.RnineT.Transfer;

public class Response {
    public static class OnDownloadCompleteResponse{
        public String error;
        public String jobID;
        public String sourceDriveDirectoryID;
        public String directoryPath;
        public String directoryName;
        public Long size;

        public OnDownloadCompleteResponse(){};
        public OnDownloadCompleteResponse(String error, String jobID, String sourceDriveDirectoryID, String directoryPath, String directoryName, Long size){
            this.error = error;
            this.jobID = jobID;
            this.sourceDriveDirectoryID = sourceDriveDirectoryID;
            this.directoryPath = directoryPath;
            this.directoryName = directoryName;
            this.size = size;
        }

        public static OnDownloadCompleteResponse makeErrorResponseObject(String errorMessage){
            OnDownloadCompleteResponse response = new OnDownloadCompleteResponse();
            response.error = errorMessage;

            return response;
        }
    }

    public static class OnUploadCompleteResponse {
        public String error;
        public String localDirectoryID;
        public String cloudDirectoryID;

        public OnUploadCompleteResponse() {};
        public OnUploadCompleteResponse(String error, String localDirectoryID, String cloudDirectoryID) {
            this.error = error;
            this.localDirectoryID = localDirectoryID;
            this.cloudDirectoryID = cloudDirectoryID;
        }

        public static OnUploadCompleteResponse makeErrorResponseObject(String errorMessage, String localDirectoryID){
            OnUploadCompleteResponse response = new OnUploadCompleteResponse();
            response.error = errorMessage;
            response.localDirectoryID = localDirectoryID;

            return response;
        }
    }
}
