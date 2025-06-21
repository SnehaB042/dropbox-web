CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    display_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) UNIQUE NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_extension VARCHAR(10) NOT NULL,
    upload_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64),
    duplicate_sequence INTEGER NOT NULL DEFAULT 0,
    
    CONSTRAINT unique_original_sequence UNIQUE (original_filename, duplicate_sequence)
);

CREATE INDEX IF NOT EXISTS idx_files_upload_timestamp ON files(upload_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_files_original_filename ON files(original_filename);
CREATE INDEX IF NOT EXISTS idx_files_checksum ON files(checksum);
CREATE INDEX IF NOT EXISTS idx_files_file_extension ON files(file_extension);
