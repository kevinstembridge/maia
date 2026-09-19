ALTER TABLE jobs.job_execution RENAME COLUMN status TO status;
ALTER TABLE jobs.job_execution ALTER COLUMN status SET NOT NULL;
