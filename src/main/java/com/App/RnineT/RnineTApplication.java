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

	@RequestMapping(value = "/download/{token}/{downloadDirID}", method = RequestMethod.GET)
	@ResponseBody
	public String download(@PathVariable String token, @PathVariable String downloadDirID){
		Directory directory = Directory.getDirectoryInstance();

		GDrive gDrive = new GDrive(token);
		gDrive.download(downloadDirID, directory.getDirectoryPath(gDrive.getJobID()));

		return String.format("Download job ID: %s", gDrive.getJobID());
	}

	@RequestMapping(value = "/upload/{token}/{jobID}/{dirName}/{uploadDirID}", method = RequestMethod.GET)
	public String upload(@PathVariable String token, @PathVariable String jobID, @PathVariable String dirName, @PathVariable String uploadDirID){
		Directory directory = Directory.getDirectoryInstance();

		GDrive gDrive = new GDrive(token, jobID);
		gDrive.upload(directory.getDirectoryPath(jobID), dirName , uploadDirID);

		return "Uploading " + uploadDirID;
	}

}