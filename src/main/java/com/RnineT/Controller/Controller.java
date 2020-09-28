package com.RnineT.Controller;

import com.RnineT.Auth.Token;
import com.RnineT.Status.Database.Directories.Directory;
import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Status.Database.Jobs.Job;
import com.RnineT.Status.Database.Jobs.JobRepository;
import com.RnineT.Status.Status;
import com.RnineT.Status.Statusd;
import com.RnineT.Transfer.Drives.AmazonDrive.api.AmazonDriveAPI;
import com.RnineT.Transfer.Drives.Dropbox.Dropbox;
import com.RnineT.Transfer.Storage.Storage;
import com.RnineT.Transfer.Transfer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@org.springframework.stereotype.Controller
@RestController
public class Controller {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

	@CrossOrigin("https://localhost:3000")
	@GetMapping("/token/get/{drive}/{code}")
	@ResponseBody
	public String getToken(@PathVariable String drive, @PathVariable String code){
		switch (drive){
			case Transfer.BOX:{
				String url = "https://api.box.com/oauth2/token";
				String clientID = "inothb10fvq4yopnj2bzhh9khnawl4f5";
				String clientSecret = System.getenv("R_NINET_BOX_CLIENT_SECRET");

				Token token = new Token(url, clientID, clientSecret, code);

				try {
					return new ObjectMapper().writeValueAsString(token);
				} catch (Exception e){
					return "";
				}
			}
			default:{
				return "";
			}
		}
	}

	@CrossOrigin(value = "https://localhost:3000")
	@PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String transfer(@RequestBody TransferRequest request){
		Statusd statusd = new Statusd(jobRepository, directoryRepository);
		CompletableFuture
			.runAsync(() -> {
				statusd.start();
			});

		Transfer transfer = new Transfer(request, jobRepository, directoryRepository);
		Status status = new Status(jobRepository, directoryRepository);
		Storage storage = Storage.getStorageInstance();

		Job job = new Job();

		job.setSource(((TransferRequest.SourceDrive)request.getSourceDrive()).getName());
		job.setDest(((TransferRequest.DestDrive)request.getDestDrive()).getName());
		job.setId(transfer.getJobID());job.setSize(transfer.getTotalSizeInBytes());
		job.setTotalItemsCount(transfer.getTotalItemsCount());

		Long totalDiskSize = storage.getTotalDiskSpace();
		Long spaceTakenByAllRunningJobsJobs = status.getSpaceTakenByAllRunningJobs();
		Long totalSizeInBytes = transfer.getTotalSizeInBytes();

		if(spaceTakenByAllRunningJobsJobs + totalSizeInBytes > .80 * totalDiskSize){
			storage.addDisk();
		}

		job.setStatus(Job.STATES.TRANSFERRING);
		jobRepository.save(job);

		transfer.startTransfer();

		return "Job initiated. ID = " + transfer.getJobID();
	}

	@GetMapping("/status/{jobID}")
	@ResponseBody
	public String getStatus(@PathVariable String jobID){
		Statusd statusd = new Statusd(jobRepository, directoryRepository);
		Long total = 0L;
		Long uploads = 0L;
		ArrayList<Map<String, String>> errorStateDirectoriesInfo = new ArrayList<Map<String, String>>();
			Iterator<Directory> iterator = directoryRepository
				.findAll()
				.iterator();
			while(iterator.hasNext()){
				Directory directory = iterator.next();
				if(directory.getJobID().equals(jobID)){
					total++;
					if(directory.getState().equals(Directory.STATE_ERROR)) {
						errorStateDirectoriesInfo.add(makeErrorMap(directory));
					} else if(directory.getState().equals(Directory.STATE_UPLOADED)){
						uploads++;
					}
				}
			}

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("jobID", jobID);
			data.put("uploads", uploads);
			data.put("errors", errorStateDirectoriesInfo.size());
			data.put("total", total);
			data.put("errorDetails", errorStateDirectoriesInfo);

			String jsonString = String.format("{'error': 'error getting status for transfer id = %s'}", jobID);
			try {
				jsonString = new ObjectMapper().writeValueAsString(data);
			} catch (Exception e){
				e.printStackTrace();
			}
		return jsonString;
	}

	private Map<String, String> makeErrorMap(Directory directory){
		Map<String, String> data = Collections.emptyMap();
		data.put("jobID", directory.getJobID());
		data.put("directoryID", directory.getLocalDirectoryID());
		data.put("name", directory.getDirectoryName());
		data.put("path", directory.getDirectoryPath());

		return data;
	}
}