package com.example.springai.service

import kotlin.math.abs
import kotlin.math.sqrt
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.stereotype.Service

/** 유사도 측정 방식 열거형 */
enum class SimilarityMetric {
    COSINE, // 코사인 유사도 (방향성 중심, 범위: -1 to 1, 클수록 유사)
    EUCLIDEAN, // 유클리디안 거리 (직선 거리, 범위: 0 to ∞, 작을수록 유사)
    DOT_PRODUCT, // 내적 (방향 + 크기, 범위: -∞ to ∞, 클수록 유사)
    MANHATTAN // 맨해튼 거리 (그리드 거리, 범위: 0 to ∞, 작을수록 유사)
}

/** 다양한 유사도 측정 방식을 지원하는 시맨틱 검색 서비스 */
@Service
class SimpleSemanticSearchService(private val embeddingModel: EmbeddingModel) {
    // 문서 저장 (실제로는 DB에 저장)
    private val documents = mutableListOf<Document>()

    /** 문서 추가 */
    fun addDocument(text: String, id: String) {
        val embedding = embeddingModel.embed(text)
        documents.add(Document(id, text, embedding))
    }

    /** 시맨틱 검색 (기본: 코사인 유사도) */
    fun search(query: String, topK: Int = 5): List<SearchResult> {
        return search(query, topK, SimilarityMetric.COSINE)
    }

    /** 지정된 유사도 메트릭을 사용한 시맨틱 검색 */
    fun search(query: String, topK: Int = 5, metric: SimilarityMetric): List<SearchResult> {
        // 검색어 임베딩 생성
        val queryVector = embeddingModel.embed(query)

        // 모든 문서와의 유사도/거리 계산
        val results =
                documents.map { doc ->
                    val score = calculateSimilarity(queryVector, doc.embedding, metric)
                    SearchResult(doc.id, doc.text, score, metric.name)
                }

        // 메트릭에 따라 정렬 (similarity는 내림차순, distance는 오름차순)
        val sorted =
                when (metric) {
                    SimilarityMetric.COSINE, SimilarityMetric.DOT_PRODUCT ->
                            results.sortedByDescending { it.score }
                    SimilarityMetric.EUCLIDEAN, SimilarityMetric.MANHATTAN ->
                            results.sortedBy { it.score }
                }

        return sorted.take(topK)
    }

    /** 모든 유사도 메트릭으로 검색하여 비교 */
    fun compareMetrics(query: String, topK: Int = 5): Map<String, List<SearchResult>> {
        return mapOf(
                "COSINE" to search(query, topK, SimilarityMetric.COSINE),
                "EUCLIDEAN" to search(query, topK, SimilarityMetric.EUCLIDEAN),
                "DOT_PRODUCT" to search(query, topK, SimilarityMetric.DOT_PRODUCT),
                "MANHATTAN" to search(query, topK, SimilarityMetric.MANHATTAN)
        )
    }

    /** 유사도/거리 계산 (메트릭에 따라) */
    private fun calculateSimilarity(
            a: FloatArray,
            b: FloatArray,
            metric: SimilarityMetric
    ): Double {
        return when (metric) {
            SimilarityMetric.COSINE -> cosineSimilarity(a, b)
            SimilarityMetric.EUCLIDEAN -> euclideanDistance(a, b)
            SimilarityMetric.DOT_PRODUCT -> dotProduct(a, b)
            SimilarityMetric.MANHATTAN -> manhattanDistance(a, b)
        }
    }

    /**
     * 코사인 유사도 계산 공식: cosine(A, B) = (A · B) / (||A|| × ||B||) 범위: -1 to 1 (1 = 동일 방향, 0 = 직교, -1 =
     * 반대 방향)
     */
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size) return 0.0

        val dotProduct = a.zip(b).sumOf { (x, y) -> (x * y).toDouble() }
        val normA = sqrt(a.sumOf { (it * it).toDouble() })
        val normB = sqrt(b.sumOf { (it * it).toDouble() })

        return if (normA > 0.0 && normB > 0.0) {
            dotProduct / (normA * normB)
        } else {
            0.0
        }
    }

    /** 유클리디안 거리 계산 공식: euclidean(A, B) = sqrt(Σ((ai - bi)²)) 범위: 0 to ∞ (0 = 동일, 클수록 다름) */
    private fun euclideanDistance(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size) return Double.MAX_VALUE

        val sumSquaredDiff =
                a.zip(b).sumOf { (x, y) ->
                    val diff = (x - y).toDouble()
                    diff * diff
                }

        return sqrt(sumSquaredDiff)
    }

    /** 내적(Dot Product) 계산 공식: dotProduct(A, B) = Σ(ai × bi) 범위: -∞ to ∞ (클수록 유사) */
    private fun dotProduct(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size) return 0.0

        return a.zip(b).sumOf { (x, y) -> (x * y).toDouble() }
    }

    /** 맨해튼 거리 계산 공식: manhattan(A, B) = Σ(|ai - bi|) 범위: 0 to ∞ (0 = 동일, 클수록 다름) */
    private fun manhattanDistance(a: FloatArray, b: FloatArray): Double {
        if (a.size != b.size) return Double.MAX_VALUE

        return a.zip(b).sumOf { (x, y) -> abs((x - y).toDouble()) }
    }

    /** 모든 문서 조회 */
    fun getAllDocuments(): List<Document> {
        return documents.toList()
    }

    /** 문서 삭제 */
    fun removeDocument(id: String): Boolean {
        return documents.removeIf { it.id == id }
    }

    /** 모든 문서 삭제 */
    fun clearDocuments() {
        documents.clear()
    }
}

data class Document(val id: String, val text: String, val embedding: FloatArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Document) return false

        if (id != other.id) return false
        if (text != other.text) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}

data class SearchResult(
        val id: String,
        val text: String,
        val score: Double,
        val metric: String = "COSINE"
)
