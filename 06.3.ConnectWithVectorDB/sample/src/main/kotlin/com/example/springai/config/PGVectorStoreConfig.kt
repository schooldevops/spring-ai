package com.example.springai.config

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.pgvector.PgVectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

/**
 * PGVectorStore 설정
 * 
 * PostgreSQL + PGVector를 사용한 벡터 저장소입니다.
 * 프로덕션 환경에 적합하며, 데이터가 영구적으로 보존됩니다.
 */
@Configuration
class PGVectorStoreConfig(
    private val embeddingModel: EmbeddingModel,
    private val dataSource: DataSource
) {
    @Bean
    fun jdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }
    
    @Bean
    fun pgVectorStore(jdbcTemplate: JdbcTemplate): VectorStore {
        val pgVectorStore = PgVectorStore.builder(
            jdbcTemplate,
            embeddingModel
        )
            .dimensions(1536)  // OpenAI text-embedding-ada-002
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .removeExistingVectorStoreTable(false)
            .initializeSchema(true)
            .build()
        
        return pgVectorStore
    }
}

