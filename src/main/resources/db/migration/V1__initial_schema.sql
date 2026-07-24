CREATE TABLE task (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      name VARCHAR(255) NOT NULL,
                      type VARCHAR(32) NOT NULL,
                      status VARCHAR(32) NOT NULL,
                      retry_count INT NOT NULL DEFAULT 0,
                      max_retry_count INT NOT NULL DEFAULT 3,
                      last_error_message VARCHAR(1024) NULL,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL
                          DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,
                      started_at TIMESTAMP NULL DEFAULT NULL,
                      completed_at DATETIME(3) NULL DEFAULT NULL,

                      PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE task_state_audit (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  task_id BIGINT NOT NULL,
                                  from_status VARCHAR(32) NOT NULL,
                                  to_status VARCHAR(32) NOT NULL,
                                  actor VARCHAR(64) NOT NULL,
                                  reason VARCHAR(128) NOT NULL,
                                  retry_count INT NOT NULL,
                                  max_retry_count INT NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;