package com.App.RnineT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SpringBootApplication
@RestController
public class RnineTApplication {
	public static void main(String[] args) {
		SpringApplication.run(RnineTApplication.class, args);
	}

	@GetMapping("/")
	public String index() {
		return "Hello world";
	}

	@RequestMapping(value = "/download/{token}/{dirID}", method = RequestMethod.GET)
	@ResponseBody
	public String download(@PathVariable String token, @PathVariable String downloadDirID){
		Directory directory = Directory.getDirectoryInstance();
		UUID jobID = UUID.randomUUID();

		GDrive gDrive = new GDrive(token, jobID.toString());
		directory.makeDirByJobID(jobID.toString());
		gDrive.download(downloadDirID, directory.getDirectoryPath(jobID.toString()));

		return String.format("Download job ID: %s", jobID.toString());
	}

	@RequestMapping(value = "/upload/{token}/{jobID}/{dirName}/{uploadDirID}", method = RequestMethod.GET)
	public String upload(@PathVariable String token, @PathVariable String jobID, @PathVariable String dirName, @PathVariable String uploadDirID){
		Directory directory = Directory.getDirectoryInstance();

		GDrive gDrive = new GDrive(token, jobID);
		gDrive.upload(directory.getDirectoryPath(jobID), dirName, uploadDirID);

		return "Uploading " + uploadDirID;
	}

}