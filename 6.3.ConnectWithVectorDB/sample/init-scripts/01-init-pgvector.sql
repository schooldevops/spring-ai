-- PGVector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 확장 확인
SELECT * FROM pg_extension WHERE extname = 'vector';

