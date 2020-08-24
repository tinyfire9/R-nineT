package com.RnineT.Status;

import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Status.Database.Jobs.Job;
import com.RnineT.Status.Database.Jobs.JobRepository;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Statusd {
    private JobRepository jobRepository;
    private DirectoryRepository directoryRepository;
    public Statusd(JobRepository jobRepository, DirectoryRepository directoryRepository){
        this.jobRepository = jobRepository;
        this.directoryRepository = directoryRepository;
    }

    private boolean isDone(String jobID){
        Job job = this.jobRepository.findById(jobID).get();

        AtomicInteger uploadedDirectories = new AtomicInteger();
        this.directoryRepository
            .findAll()
            .forEach(directory -> {
                if(directory.getJobID().equals(jobID) && (!directory.getCloudDirectoryID().equals("") || directory.getCloudDirectoryID() != null)) {
                    uploadedDirectories.getAndIncrement();
                    System.out.println("JobID: " + jobID + ", uploadedCount: " + uploadedDirectories.get());
                }
            });

        return job.getTotalItemsCount() == uploadedDirectories.get();
    }

    private void updateJobStatus() {
        this.jobRepository
            .findAll()
            .forEach(job -> {
                System.out.println("Job " + job.getId() + " status: " + job.getStatus());
                if(!job.getStatus().equals(Job.STATES.COMPLETED)){
                    if(isDone(job.getId())){
                        job.setStatus(Job.STATES.COMPLETED);
                        this.jobRepository.save(job);
                        System.out.println("Job " + job.getId() + " completed");
                    }
                }
            });
    }

    private boolean allJobsCompleted(){
        Iterator<Job> jobs = this.jobRepository.findAll().iterator();

        while(jobs.hasNext()){
            if(!jobs.next().getStatus().equals(Job.STATES.COMPLETED)){
                return false;
            }
        }

        return true;
    }

    public void start(){
        try {
            while(true){
                updateJobStatus();
                Thread.sleep(20000);
                if(allJobsCompleted()){
                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
