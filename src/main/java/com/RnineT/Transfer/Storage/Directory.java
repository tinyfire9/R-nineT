package com.RnineT.Transfer.Storage;

import java.io.File;

public class Directory {
    private final String R_NINE_T_BASE_PWD;
    private final String JOBS_DIR_PATH;
    private final String JOBS_DIR_NAME = "r-nineT-jobs";
    private static Directory directory;

    Directory(){
        this.R_NINE_T_BASE_PWD = System.getenv().get("R_NINET_BASE_PWD");
        this.JOBS_DIR_PATH = R_NINE_T_BASE_PWD + "/" + JOBS_DIR_NAME;
    }

    public static Directory getDirectoryInstance(){
        if(directory == null){
            directory = new Directory();
        }

        return directory;
    }

    public String getDirectoryPath(String jobID) {
        return String.format("%s/%s", JOBS_DIR_PATH, jobID);
    }

    public String getJobsDirectory() {
        return JOBS_DIR_PATH;
    }

    public boolean makeDirByJobID(String jobID){
        String jobDirectoryPath = JOBS_DIR_PATH + "/" + jobID;

        return this.makeDir(jobDirectoryPath);
    }

    public boolean makeDir(String path) {
        File jobsDir = new File(JOBS_DIR_PATH);

        if(!jobsDir.isDirectory()){
            if(!jobsDir.mkdir()){
                System.out.format("Error: Couldn't create jobs directory '%s'", JOBS_DIR_NAME);
                return false;
            }
        }

        File jobDir = new File(path);
        if(!jobDir.isDirectory()){
            if(!jobDir.mkdir()) {
                System.out.format("Error: Couldn't create '%s'\n", path);

                return false;
            }

            return true;
        }

        return true;
    }

    public boolean removeDir(String jobID) {
        File jobDir = new File(String.format("%s/%s", JOBS_DIR_PATH, jobID));

        if(!jobDir.isDirectory()){
            return true;
        }

        return jobDir.delete();
    }

}
