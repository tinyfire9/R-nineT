package com.RnineT.Controller;

import com.RnineT.Status.Db.Jobs.Job;
import com.RnineT.Status.Db.Jobs.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@org.springframework.stereotype.Controller
@RestController
public class Controller {
    @Autowired
    private JobRepository jobRepository;

	@GetMapping("/")
	public String index() {
		return "Hello world";
	}

	@PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String transfer(@RequestBody TransferRequest request){
		RnineT rnineT = new RnineT(request);

		Job job = new Job();
		job.setSource(((TransferRequest.SourceDrive )request.getSourceDrive()).getName());
		job.setDest(((TransferRequest.DestDrive )request.getDestDrive()).getName());
		job.setId(rnineT.getJobID());

		jobRepository.save(job);

		rnineT.startTransfer();

		return "Job initiated. ID = " + rnineT.getJobID();
	}
}