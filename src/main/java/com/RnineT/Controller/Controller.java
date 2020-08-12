package com.RnineT.Controller;

import com.RnineT.Status.Database.Directories.DirectoryRepository;
import com.RnineT.Status.Database.Jobs.Job;
import com.RnineT.Status.Database.Jobs.JobRepository;
import com.RnineT.Transfer.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


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

	@PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String transfer(@RequestBody TransferRequest request){
		Transfer transfer = new Transfer(request, directoryRepository);

		Job job = new Job();
		job.setSource(((TransferRequest.SourceDrive)request.getSourceDrive()).getName());
		job.setDest(((TransferRequest.DestDrive)request.getDestDrive()).getName());
		job.setId(transfer.getJobID());

		jobRepository.save(job);
		transfer.startTransfer();

		return "Job initiated. ID = " + transfer.getJobID();
	}
}