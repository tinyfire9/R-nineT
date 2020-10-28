package com.RnineT.Controller;

import com.RnineT.Auth.BoxToken;
import com.RnineT.Auth.DropboxToken;
import com.RnineT.Auth.Token;
import com.RnineT.Status.Database.Directories.Directory;
import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Status.Database.Jobs.Job;
import com.RnineT.Status.Database.Jobs.JobRepository;
import com.RnineT.Status.Status;
import com.RnineT.Status.Statusd;

import com.RnineT.Transfer.Storage.Storage;
import com.RnineT.Transfer.Transfer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;


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
		Token token;
		switch (drive){
			case Transfer.BOX:{
				token = new BoxToken(code);
				break;
			}

			case Transfer.DROPBOX: {
				token = new DropboxToken(code);
				break;
			}
			default:{
				return "";
			}
		}

		try {
			return new ObjectMapper().writeValueAsString(token);
		} catch (Exception e){
			e.printStackTrace();
			return "";
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
		job.setId(transfer.getJobID());
		job.setTimestamp(new Date().getTime());
		job.setTotalSize(transfer.getTotalSizeInBytes());
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

	@CrossOrigin(value = "https://localhost:3000")
	@GetMapping("/status")
	@ResponseBody
	public String getStatus() {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		this.jobRepository
			.findAll()
			.forEach(job -> {
				AtomicReference<Long> uploadedSize = new AtomicReference<>(0L);
				AtomicReference<Long> uploadedCount = new AtomicReference<>(0L);
				AtomicReference<Long> errorCount = new AtomicReference<>(0L);
				this.directoryRepository.findAll().forEach(directory -> {
					if(directory.getJobID().equals(job.getId())){
						if(directory.getState().equals(Directory.STATE_UPLOADED)) {
							uploadedCount.getAndSet(uploadedCount.get() + 1);
							uploadedSize.getAndSet(uploadedSize.get() + directory.getSize());
						} else if(directory.getState().equals(Directory.STATE_ERROR)){
							errorCount.getAndSet(errorCount.get() + 1);
						}
					}
				});

				HashMap<String, Object> status = new HashMap<String, Object>();
				status.put("jobID", job.getId());
				status.put("srcDrive", job.getSource());
				status.put("destDrive", job.getDest());
				status.put("timestamp",job.getTimestamp());
				status.put("totalSize", job.getTotalSize());
				status.put("uploadedSize", uploadedSize.get());
				status.put("totalItemsCount", job.getTotalItemsCount());
				status.put("uploadedItemsCount", uploadedCount.get());
				status.put("errorItemsCount", errorCount.get());

				data.add(status);
			});

		try {
			String jsonData = new ObjectMapper().writeValueAsString(data);
			return jsonData;
		} catch (Exception e){
			e.printStackTrace();
		}

		return "";
	}

	@GetMapping("/status/{jobID}")
	@ResponseBody
	public String getStatusDetail(@PathVariable String jobID){
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