package com.RnineT.Status;

import com.RnineT.Status.Database.Directories.Directory;
import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Status.Database.Jobs.Job;
import com.RnineT.Status.Database.Jobs.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Status {
    @Autowired
    private DirectoryRepository directoryRepository;
    private JobRepository jobRepository;

    public Status(JobRepository jobRepository, DirectoryRepository directoryRepository){
        this.jobRepository = jobRepository;
        this.directoryRepository = directoryRepository;
    }

    public void onDownloadError(String jobID, String sourceDriveDirectoryID, String directoryPath, String directoryName){
        Directory directory = new Directory();

        directory.setJobID(jobID);
        directory.setSourceDriveDirectoryID(sourceDriveDirectoryID);
        directory.setDirectoryPath(directoryPath);
        directory.setDirectoryName(directoryName);
        directory.setState(Directory.STATE_ERROR);

        directoryRepository.save(directory);
    }

    public void onUploadError(String localDirectoryID){
        Directory directory = directoryRepository.findById(localDirectoryID).get();

        directory.setState(Directory.STATE_ERROR);

        directoryRepository.save(directory);
    }

    public String onDirectoryDownload(String jobID, String directoryPath, String directoryName, long size){
        Directory directory = new Directory();
        String localDirectoryID = UUID.randomUUID().toString();

        directory.setState(Directory.STATE_DOWNLOADED);
        directory.setLocalDirectoryID(localDirectoryID);
        directory.setDestDriveDirectoryID("");
        directory.setJobID(jobID);
        directory.setDirectoryPath(directoryPath);
        directory.setDirectoryName(directoryName);
        directory.setSize(size);

        this.directoryRepository.save(directory);

        return localDirectoryID;
    }

    public void onDirectoryUpload(String localDirectoryID, String destDriveDirectoryID){
        Directory directory = this.directoryRepository.findById(localDirectoryID).get();

        directory.setDestDriveDirectoryID(destDriveDirectoryID);
        directory.setState(Directory.STATE_UPLOADED);

        this.directoryRepository.save(directory);
    }

    public String getDirectoryPathAndNameByID(String directoryID){
        Directory directory = this.directoryRepository
                .findById(directoryID)
                .get();

        return directory.getDirectoryPath() + "/" + directory.getDirectoryName();
    }

    /**
     * Fetches the corresponding directory-id for the given path in the dest drive
     * @param jobID
     * @param localDirectoryPath
     * @return
     */
    public String fetchDestDriveDirectoryID(String jobID, String localDirectoryPath){
        Iterator<Directory> directoryIterator = this.directoryRepository.findAll().iterator();
        while(true){
            Directory directory = directoryIterator.next();
            if(directory==null){
                return "";
            }
            if(directory.getJobID().equals(jobID) && (directory.getDirectoryPath() + "/" + directory.getDirectoryName()).equals(localDirectoryPath)){
                return directory.getDestDriveDirectoryID();
            }
        }
    }

    public long getSpaceTakenByAllRunningJobs(){
        AtomicLong size = new AtomicLong();
        this.jobRepository
            .findAll()
            .forEach(job -> {
                if(job.getStatus().equals(Job.STATES.TRANSFERRING)){
                    size.addAndGet(job.getSize());
                }
            });

        return size.get();
    }
}
