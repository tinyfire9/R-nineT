package com.RnineT.Status.Db.Jobs;

import com.RnineT.Status.Db.Jobs.Job;
import org.springframework.data.repository.CrudRepository;

public interface JobRepository extends CrudRepository<Job, String> {

}
