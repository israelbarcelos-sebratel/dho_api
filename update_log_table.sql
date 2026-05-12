ALTER TABLE recruitment_process_log ADD COLUMN opportunity_id INT;
ALTER TABLE recruitment_process_log ADD CONSTRAINT fk_log_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities(id);
