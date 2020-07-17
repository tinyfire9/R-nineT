package com.App.RnineT;

import com.google.cloud.compute.v1.DiskClient;
import com.google.cloud.compute.v1.ProjectZoneDiskName;

public class Wait {
    private DiskClient diskClient;
    private final String PROJECT_ID;
    private final String ZONE;
    private final Integer POLL_INTERVAL;

    Wait(DiskClient diskClient, String projectID, String zone, Integer pollInterval) {
        this.diskClient = diskClient;
        this.PROJECT_ID = projectID;
        this.ZONE = zone;
        this.POLL_INTERVAL = pollInterval;
    }

    public void waitUntilDiskIsDetached(String diskName){
        try {
            while(diskClient.getDisk(ProjectZoneDiskName.of(diskName, PROJECT_ID, ZONE)).getUsersList().size() > 0){
                Thread.sleep(POLL_INTERVAL);
            }
        }catch (Exception e){}
    }

    public void waitUntilDiskIsReady(String diskName) {
        ProjectZoneDiskName projectZoneDiskName = ProjectZoneDiskName.of(diskName, PROJECT_ID, ZONE);
        String status = diskClient.getDisk(projectZoneDiskName).getStatus();
        try {
            while(!status.equals("READY")) {
                System.out.println("Disk creation status: " + status);
                Thread.sleep(POLL_INTERVAL);
                status = diskClient.getDisk(projectZoneDiskName).getStatus();
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
