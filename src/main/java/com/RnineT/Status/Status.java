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

    /**
     * Fetches the corresponding directory-id for the given path in the dest drive
     * @param jobID
     * @param localDirectoryPath
     * @return
     */
    public String fetchCloudDirectoryID(String jobID, String localDirectoryPath){
        Iterator<Directory> directoryIterator = this.directoryRepository.findAll().iterator();
        while(true){
            Directory directory = directoryIterator.next();
            if(directory==null){
                return "";
            }
            if(directory.getJobID().equals(jobID) && (directory.getDirectoryPath() + "/" + directory.getDirectoryName()).equals(localDirectoryPath)){
                return directory.getCloudDirectoryID();
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

    public boolean isDone(String jobID){
        Job job = this.jobRepository.findById(jobID).get();

        AtomicInteger uploadedDirectories = new AtomicInteger();
        this.directoryRepository.findAll().forEach(directory -> {
            if(directory.getJobID().equals(jobID) && (!directory.getCloudDirectoryID().equals("") || directory.getCloudDirectoryID() != null)) {
                uploadedDirectories.updateAndGet(c -> c+1);
            }
        });

        return job.getTotalItemsCount() == uploadedDirectories.get();
    }
}
