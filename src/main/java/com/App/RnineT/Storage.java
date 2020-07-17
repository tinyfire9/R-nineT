package com.App.RnineT;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.compute.ComputeScopes;
import com.google.auth.Credentials;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.compute.v1.*;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

public class Disk {
    private Wait wait;
    private DiskClient diskClient;
    private InstanceClient instanceClient;
    private final String INSTANCE_NAME = "vm-1";
    private final String INSTANCE_ID = "7925632754121884046";
    private final String PROJECT_NAME = "R-nineT";
    private final String PROJECT_ID = "r-ninet-283420";
    private final String ZONE = "us-central1-a";
    private final String DISK_IMAGE_NAME = "ubuntu-1604-xenial-v20200713a";
    private final Integer POLL_INTERVAL = 500;
    private final String CREDENTIALS_PATH = System.getenv().get("GOOGLE_APPLICATION_CREDENTIALS");

    Disk(){
        try {
            Credentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(CREDENTIALS_PATH))
                    .createScoped(Collections.singleton(ComputeScopes.CLOUD_PLATFORM));
            diskClient = this.createDiskClient(credentials);
            instanceClient = this.createInstanceClient(credentials);
            wait = new Wait(diskClient, PROJECT_ID, ZONE, POLL_INTERVAL);
        } catch (Exception e) {
            System.out.println("Error creating initializing disk and instance client: ");
            System.out.println(e.getMessage());
        }
    }

    public InstanceClient createInstanceClient(Credentials credentials){
        try {
            InstanceSettings instanceSettings = InstanceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return InstanceClient.create(instanceSettings);
        } catch (Exception e){
            System.out.println("Error creating instance client: ");
            System.out.println(e.getMessage());
            return  null;
        }
    }

    public DiskClient createDiskClient(Credentials credentials) {
        try{
            DiskSettings diskSettings = DiskSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return DiskClient.create(diskSettings);

        } catch (Exception e){
            System.out.println("Error creating disk client: ");
            System.out.println(e.getMessage());
            return  null;
        }
    }

    public void createDisk(String diskName, int size){
        com.google.cloud.compute.v1.Disk disk = com.google.cloud.compute.v1.Disk.newBuilder()
                .setSizeGb(String.format("%s", size))
                .setName(diskName)
                .setSourceImageId(DISK_IMAGE_NAME)
                .build();
        diskClient.insertDisk(ProjectZoneName.of(PROJECT_ID, ZONE), disk);
        this.wait.waitUntilDiskIsReady(diskName);
        String source = String.format(
                "/compute/v1/projects/%s/zones/%s/disks/%s",
                PROJECT_ID,
                ZONE,
                diskName
        );

        AttachedDisk attachedDisk =
                AttachedDisk.newBuilder()
                        .setSource(source)
                        .build();

        instanceClient.attachDiskInstance(
                ProjectZoneInstanceName.of(INSTANCE_ID, PROJECT_ID, ZONE),
                false,
                attachedDisk
        );

        Instance vm = instanceClient.getInstance(ProjectZoneInstanceName.of(INSTANCE_NAME, PROJECT_ID, ZONE));
        System.out.format("VM Name: %s\n Disks: %s", vm.getName(), vm.getDisksList().toString());
    }

    private String getDeviceName(String diskName){
        String deviceName = "";
        List<AttachedDisk> attachedDisks = instanceClient.
                getInstance(ProjectZoneInstanceName.of(INSTANCE_NAME, PROJECT_ID, ZONE))
                .getDisksList();

        for(int i = 0; i < attachedDisks.size(); i++){
            if(attachedDisks.get(i).getSource().contains(diskName)){
                deviceName = attachedDisks.get(i).getDeviceName();
                break;
            }
        }

        return deviceName;
    }

    public void deleteDisk(String diskName) {
        String deviceName = getDeviceName(diskName);

        if(!deviceName.equals("")){
            instanceClient.detachDiskInstance(
                    ProjectZoneInstanceName.of(INSTANCE_NAME, PROJECT_ID, ZONE),
                    deviceName
            );

            this.wait.waitUntilDiskIsDetached(diskName);
        }

        diskClient.deleteDisk(ProjectZoneDiskName.of(diskName, PROJECT_ID, ZONE));
    }
}
