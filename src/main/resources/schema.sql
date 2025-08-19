CREATE TABLE IF NOT EXISTS models (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) UNIQUE NOT NULL,
  description TEXT,
  owner VARCHAR(100),
  tags JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS model_versions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_id BIGINT NOT NULL,
  version VARCHAR(50) NOT NULL,
  framework VARCHAR(50),
  metrics JSON,
  schema_def JSON,
  artifact_uri VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(model_id, version),
  FOREIGN KEY (model_id) REFERENCES models(id)
);

CREATE TABLE IF NOT EXISTS model_stages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  model_version_id BIGINT NOT NULL,
  stage ENUM('DEV','STAGING','PRODUCTION','ARCHIVED'),
  is_active BOOLEAN DEFAULT TRUE,
  changed_by VARCHAR(100),
  comment TEXT,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (model_version_id) REFERENCES model_versions(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(50),
  entity_id BIGINT,
  action VARCHAR(50),
  old_state JSON,
  new_state JSON,
  performed_by VARCHAR(100),
  performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS access_policies (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role VARCHAR(50),
  action VARCHAR(50),
  resource VARCHAR(50)
);