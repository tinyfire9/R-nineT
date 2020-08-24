package com.RnineT.Controller;

import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Status.Database.Jobs.Job;
import com.RnineT.Status.Database.Jobs.JobRepository;
import com.RnineT.Status.Status;
import com.RnineT.Status.Statusd;
import com.RnineT.Transfer.Storage.Storage;
import com.RnineT.Transfer.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@org.springframework.stereotype.Controller
@RestController
public class Controller {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private DirectoryRepository directoryRepository;

	@GetMapping("/")
	public String index() {
		return "Hello world";
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
}