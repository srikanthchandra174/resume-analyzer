-- Schema for resume analyzer (plain PostgreSQL; no extensions required)
-- Safe to re-run. Hibernate (ddl-auto: update) can also create these tables on its own.

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

-- Element-collection tables for ResumeAnalysis (no primary key: the LLM may return duplicates)
CREATE TABLE IF NOT EXISTS missing_skills (
    analysis_id BIGINT NOT NULL,
    skill VARCHAR(255),
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS suggested_improvements (
    analysis_id BIGINT NOT NULL,
    improvement TEXT,
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS interview_questions (
    analysis_id BIGINT NOT NULL,
    question TEXT,
    FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE
);

-- Indexes for query performance
CREATE INDEX IF NOT EXISTS idx_resume_analysis_created_at ON resume_analysis(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_resume_analysis_match_score ON resume_analysis(match_score);
CREATE INDEX IF NOT EXISTS idx_resume_analysis_file_name ON resume_analysis(file_name);
CREATE INDEX IF NOT EXISTS idx_resume_analysis_embedding_id ON resume_analysis(embedding_id);
CREATE INDEX IF NOT EXISTS idx_missing_skills_analysis_id ON missing_skills(analysis_id);
CREATE INDEX IF NOT EXISTS idx_suggested_improvements_analysis_id ON suggested_improvements(analysis_id);
CREATE INDEX IF NOT EXISTS idx_interview_questions_analysis_id ON interview_questions(analysis_id);

-- Full-text search indexes
CREATE INDEX IF NOT EXISTS idx_resume_text_gin ON resume_analysis USING gin(to_tsvector('english', resume_text));
CREATE INDEX IF NOT EXISTS idx_job_description_gin ON resume_analysis USING gin(to_tsvector('english', job_description));

-- Audit table for logging changes
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

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_resume_analysis_updated_at ON resume_analysis;
CREATE TRIGGER update_resume_analysis_updated_at BEFORE UPDATE ON resume_analysis
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ---------------------------------------------------------------------------
-- OPTIONAL: semantic search with pgvector (not used by the application yet).
-- Requires the pgvector extension to be installed on the PostgreSQL server.
-- Uncomment to enable.
-- ---------------------------------------------------------------------------
-- CREATE EXTENSION IF NOT EXISTS vector;
--
-- CREATE TABLE IF NOT EXISTS resume_embeddings (
--     id BIGSERIAL PRIMARY KEY,
--     analysis_id BIGINT NOT NULL UNIQUE,
--     embedding vector(1536), -- OpenAI embedding dimension
--     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (analysis_id) REFERENCES resume_analysis(id) ON DELETE CASCADE
-- );
--
-- CREATE INDEX IF NOT EXISTS idx_embedding_vector ON resume_embeddings
--     USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
