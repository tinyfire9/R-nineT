package com.App.RnineT;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.deprecated.*;

import java.io.File;
import java.io.FileInputStream;

public class Disk {
    private Compute compute;
    private final String instance = "vm-1";
    private final String instanceID = "7925632754121884046";
    private final String projectName = "R-nineT";
    private final String projectID = "r-ninet-283420";
    private final String zone = "us-central1-a";
    private String rninetBasePWD = System.getenv().get("R_NINET_BASE_PWD");
    private String jobsDirName = "r-nineT-jobs";
    private String jobsDirPath = this.rninetBasePWD + "/" + this.jobsDirName;

    Disk(){
        this.init();
    }

    public boolean init() {
        String credentialsPath = System.getenv().get("GOOGLE_APPLICATION_CREDENTIALS");

        try {
            this.compute = ComputeOptions.newBuilder()
                    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsPath)))
                    .build()
                    .getService();
        } catch (Exception e){
            System.out.println("Error authenticating to compute engine: ");
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean makeDir(String jobID) {
        File jobsDir = new File(jobsDirPath);

        if(!jobsDir.isDirectory()){
            if(!jobsDir.mkdir()){
                System.out.format("Error: Couldn't create jobs directory '%s'", jobsDirName);
                return false;
            }
        }

        File jobDir = new File(jobsDirPath + "/" + jobID);
        if(!jobDir.isDirectory()){
            if(!jobDir.mkdir()) {
                System.out.format("Error: Couldn't create job directory '/%s/%s'", jobsDirName, jobID);

                return false;
            }

            return true;
        }

        return true;
    }

    public boolean removeDir(String jobID) {
        File jobDir = new File(String.format("%s/%s", jobsDirPath, jobID));

        if(!jobDir.isDirectory()){
            return true;
        }

        return jobDir.delete();
    }

    private void sleep(Operation operation){
        while(!operation.isDone()){
            try {
                Thread.sleep(1000);

            } catch (Exception e){
                System.out.println(e);
            }
        }
    }

    public void createDisk(String diskName, int size){
        DiskId diskId = DiskId.of(projectID, zone, diskName);
        DiskConfiguration diskConfig = StandardDiskConfiguration.of(size);
        DiskInfo diskInfo = DiskInfo.newBuilder(
                diskId,
                diskConfig
        ).build();

        Operation operation = this.compute.create(diskInfo);

        this.sleep(operation);

        this.compute.attachDisk(
                InstanceId.of(projectID, zone, instance),
                AttachedDisk.PersistentDiskConfiguration.of(diskId)
        );
    }

    public void deleteDisk(String diskName) {
        Operation operation = this.compute.detachDisk(
                InstanceId.of(projectID, zone, instanceID),
                diskName
        );

        this.sleep(operation);

        this.compute.deleteDisk(DiskId.of(projectName, zone, diskName));
    }
}
