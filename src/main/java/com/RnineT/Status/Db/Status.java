package com.RnineT.Status.Db;

import com.RnineT.Status.Db.Directories.Directory;
import com.RnineT.Status.Db.Directories.DirectoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

public class Status {
    @Autowired
    private DirectoryRepository directoryRepository;

    public Status(DirectoryRepository directoryRepository){
        this.directoryRepository = directoryRepository;
    }

    public String onDirectoryDownload(String jobID, String directoryPath, String directoryName, long size){
        Directory directory = new Directory();
        String localDirectoryID = UUID.randomUUID().toString();

        directory.setLocalDirectoryID(localDirectoryID);
        directory.setCloudDirectoryID("");
        directory.setJobID(jobID);
        directory.setDirectoryPath(directoryPath);
        directory.setDirectoryName(directoryName);
        directory.setSize(size);

        this.directoryRepository.save(directory);

        return localDirectoryID;
    }

    public void onDirectoryUpload(String localDirectoryID, String cloudDirectoryID){
        Optional<Directory> directory = this.directoryRepository.findById(localDirectoryID);
        directory.get().setCloudDirectoryID(cloudDirectoryID);

        this.directoryRepository.save(directory.get());
    }

    public String fetchCloudDirectoryID(String jobID, String directoryPath){
        Iterable<Directory> directories = this.directoryRepository.findAll();
        int size = (int) this.directoryRepository.count();

        for(int i = 0; i < size; i++){
            Directory directory = directories.iterator().next();
            String parentDirectoryPath = directory.getDirectoryPath() + "/" + directory.getDirectoryName();
            if(directory.getJobID().equals(jobID) && parentDirectoryPath.equals(directoryPath)){
                return directory.getCloudDirectoryID();
            }
        }

        return "";
    }
}
