package com.App.RnineT;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
	public String download(@PathVariable String token, @PathVariable String dirID){
		Directory directory = Directory.getDirectoryInstance();
		GDrive gDrive = GDrive.getGDriveInstance();
		UUID jobID = UUID.randomUUID();

		directory.makeDirByJobID(jobID.toString());
		gDrive.download(
				token,
				Collections.singletonList(dirID),
				directory.getDirectoryPath(jobID.toString())
		);

		return String.format("Download job ID: %s", jobID.toString());
	}
}