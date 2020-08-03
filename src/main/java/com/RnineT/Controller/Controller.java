package com.RnineT.Controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@SpringBootApplication
@RestController
public class Controller {
	public static void main(String[] args) {
		SpringApplication.run(Controller.class, args);
	}

	@GetMapping("/")
	public String index() {
		return "Hello world";
	}

	@PostMapping(value = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String transfer(@RequestBody TransferRequest request){
		RnineT rnineT = new RnineT(request);

		rnineT.startTransfer();

		return "Job initiated. ID = " + rnineT.getJobID();
	}
}