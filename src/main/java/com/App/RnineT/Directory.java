package com.App.RnineT;

import java.io.File;

public class Directory {
    private final String R_NINE_T_BASE_PWD;
    private final String JOBS_DIR_PATH;
    private final String JOBS_DIR_NAME = "r-nineT-jobs";

    Directory(){
        this.R_NINE_T_BASE_PWD = System.getenv().get("R_NINET_BASE_PWD");
        this.JOBS_DIR_PATH = R_NINE_T_BASE_PWD + "/" + JOBS_DIR_NAME;
    }

    public boolean makeDir(String jobID) {
        File jobsDir = new File(JOBS_DIR_PATH);

        if(!jobsDir.isDirectory()){
            if(!jobsDir.mkdir()){
                System.out.format("Error: Couldn't create jobs directory '%s'", JOBS_DIR_NAME);
                return false;
            }
        }

        File jobDir = new File(JOBS_DIR_PATH + "/" + jobID);
        if(!jobDir.isDirectory()){
            if(!jobDir.mkdir()) {
                System.out.format("Error: Couldn't create job directory '/%s/%s'", JOBS_DIR_NAME, jobID);

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
