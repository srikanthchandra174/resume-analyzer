-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create schema for resume analyzer
CREATE SCHEMA IF NOT EXISTS resume_analyzer_schema;

-- Create tables with proper constraints and indexes

-- Resume Analysis Table
CREATE TABLE IF NOT EXISTS resume_analysis (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(500) NOT NULL,
    resume_text TEXT NOT NULL,
    job_description TEXT NOT NULL,
    match_score DOUBLE PRECISION NOT NULL,
    summary TEXT,
    embedding_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_match_score CHECK (match_score >= 0 AND match_score <= 100)
);

-- Missing Skills Table
CREATE TABLE IF NOT EXISTS missing_skills (
    analysis_id BIGINT NOT NULL,
    skill VARCHAR(255) NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE,
    PRIMARY KEY (analysis_id, skill)
);

-- Suggested Improvements Table
CREATE TABLE IF NOT EXISTS suggested_improvements (
    analysis_id BIGINT NOT NULL,
    improvement TEXT NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE,
    PRIMARY KEY (analysis_id, improvement(255))
);

-- Interview Questions Table
CREATE TABLE IF NOT EXISTS interview_questions (
    analysis_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE,
    PRIMARY KEY (analysis_id, question(255))
);

-- Create indexes for better query performance
CREATE INDEX idx_resume_analysis_created_at ON resume_analysis(created_at DESC);
CREATE INDEX idx_resume_analysis_match_score ON resume_analysis(match_score);
CREATE INDEX idx_resume_analysis_file_name ON resume_analysis(file_name);
CREATE INDEX idx_resume_analysis_embedding_id ON resume_analysis(embedding_id);

-- Create index for full-text search capability
CREATE INDEX idx_resume_text_gin ON resume_analysis USING gin(to_tsvector('english', resume_text));
CREATE INDEX idx_job_description_gin ON resume_analysis USING gin(to_tsvector('english', job_description));

-- Create table for embeddings (for semantic search with pgvector)
CREATE TABLE IF NOT EXISTS resume_embeddings (
    id BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT NOT NULL UNIQUE,
    embedding vector(1536), -- OpenAI embedding dimension
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE
);

-- Create index for vector similarity search
CREATE INDEX idx_embedding_vector ON resume_embeddings USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

-- Create audit table for logging changes
CREATE TABLE IF NOT EXISTS analysis_audit_log (
    id BIGSERIAL PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_match_score DOUBLE PRECISION,
    new_match_score DOUBLE PRECISION,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(255),
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE
);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_resume_analysis_updated_at BEFORE UPDATE ON resume_analysis
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions to the application user
GRANT ALL PRIVILEGES ON SCHEMA resume_analyzer_schema TO resumeuser;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO resumeuser;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO resumeuser;

-- Create sample data (optional)
-- INSERT INTO resume_analysis (file_name, resume_text, job_description, match_score, summary)
-- VALUES ('sample_resume.pdf', 'Sample resume content...', 'Sample job description...', 85.0, 'Good match');

