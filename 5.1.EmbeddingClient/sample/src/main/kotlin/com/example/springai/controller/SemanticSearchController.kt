package com.example.springai.controller

import com.example.springai.service.SimilarityMetric
import com.example.springai.service.SimpleSemanticSearchService
import org.springframework.web.bind.annotation.*

/** 시맨틱 검색 컨트롤러 */
@RestController
@RequestMapping("/api/semantic-search")
class SemanticSearchController(private val searchService: SimpleSemanticSearchService) {

    /**
     * 문서 추가 POST http://localhost:8080/api/semantic-search/add Body: {"id": "doc1", "text": "Spring
     * AI는 Spring 프레임워크를 위한 AI 통합 라이브러리입니다."}
     */
    @PostMapping("/add")
    fun addDocument(@RequestBody request: AddDocumentRequest): Map<String, String> {
        searchService.addDocument(request.text, request.id)
        return mapOf("status" to "success", "id" to request.id)
    }

    /**
     * 시맨틱 검색 (유사도 메트릭 선택 가능) POST http://localhost:8080/api/semantic-search/search Body: {"query":
     * "프로그래밍", "topK": 5, "metric": "COSINE"}
     */
    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest): Map<String, Any> {
        // 메트릭 파라미터 파싱 (기본값: COSINE)
        val metric =
                try {
                    SimilarityMetric.valueOf(request.metric.uppercase())
                } catch (e: Exception) {
                    SimilarityMetric.COSINE
                }

        val results = searchService.search(request.query, request.topK, metric)

        return mapOf(
                "query" to request.query,
                "topK" to request.topK,
                "metric" to metric.name,
                "metricInfo" to getMetricInfo(metric),
                "results" to
                        results.map { result ->
                            mapOf(
                                    "id" to result.id,
                                    "text" to result.text,
                                    "score" to result.score,
                                    "metric" to result.metric
                            )
                        }
        )
    }

    /**
     * 모든 유사도 메트릭으로 검색 결과 비교 POST http://localhost:8080/api/semantic-search/compare-metrics Body:
     * {"query": "프로그래밍", "topK": 3}
     */
    @PostMapping("/compare-metrics")
    fun compareMetrics(@RequestBody request: CompareMetricsRequest): Map<String, Any> {
        val allResults = searchService.compareMetrics(request.query, request.topK)

        return mapOf(
                "query" to request.query,
                "topK" to request.topK,
                "metrics" to
                        mapOf(
                                "COSINE" to
                                        mapOf(
                                                "info" to getMetricInfo(SimilarityMetric.COSINE),
                                                "results" to allResults["COSINE"]
                                        ),
                                "EUCLIDEAN" to
                                        mapOf(
                                                "info" to getMetricInfo(SimilarityMetric.EUCLIDEAN),
                                                "results" to allResults["EUCLIDEAN"]
                                        ),
                                "DOT_PRODUCT" to
                                        mapOf(
                                                "info" to
                                                        getMetricInfo(SimilarityMetric.DOT_PRODUCT),
                                                "results" to allResults["DOT_PRODUCT"]
                                        ),
                                "MANHATTAN" to
                                        mapOf(
                                                "info" to getMetricInfo(SimilarityMetric.MANHATTAN),
                                                "results" to allResults["MANHATTAN"]
                                        )
                        )
        )
    }

    /** 메트릭 정보 반환 */
    private fun getMetricInfo(metric: SimilarityMetric): Map<String, String> {
        return when (metric) {
            SimilarityMetric.COSINE ->
                    mapOf(
                            "name" to "Cosine Similarity",
                            "description" to "방향성 중심 유사도",
                            "range" to "-1 to 1",
                            "interpretation" to "클수록 유사 (1=동일방향, 0=직교, -1=반대방향)"
                    )
            SimilarityMetric.EUCLIDEAN ->
                    mapOf(
                            "name" to "Euclidean Distance",
                            "description" to "직선 거리",
                            "range" to "0 to ∞",
                            "interpretation" to "작을수록 유사 (0=동일)"
                    )
            SimilarityMetric.DOT_PRODUCT ->
                    mapOf(
                            "name" to "Dot Product",
                            "description" to "방향과 크기를 모두 고려",
                            "range" to "-∞ to ∞",
                            "interpretation" to "클수록 유사"
                    )
            SimilarityMetric.MANHATTAN ->
                    mapOf(
                            "name" to "Manhattan Distance",
                            "description" to "그리드 거리 (L1 norm)",
                            "range" to "0 to ∞",
                            "interpretation" to "작을수록 유사 (0=동일)"
                    )
        }
    }

    /** 모든 문서 조회 GET http://localhost:8080/api/semantic-search/documents */
    @GetMapping("/documents")
    fun getAllDocuments(): Map<String, Any> {
        val documents = searchService.getAllDocuments()

        return mapOf(
                "count" to documents.size,
                "documents" to
                        documents.map { doc ->
                            mapOf(
                                    "id" to doc.id,
                                    "text" to doc.text,
                                    "embeddingDimension" to doc.embedding.size
                            )
                        }
        )
    }

    /** 문서 삭제 DELETE http://localhost:8080/api/semantic-search/remove/{id} */
    @DeleteMapping("/remove/{id}")
    fun removeDocument(@PathVariable id: String): Map<String, Any> {
        val removed = searchService.removeDocument(id)
        return mapOf("success" to removed, "id" to id)
    }

    /** 모든 문서 삭제 DELETE http://localhost:8080/api/semantic-search/clear */
    @DeleteMapping("/clear")
    fun clearDocuments(): Map<String, String> {
        searchService.clearDocuments()
        return mapOf("status" to "cleared")
    }
}

data class AddDocumentRequest(val id: String, val text: String)

data class SearchRequest(
        val query: String,
        val topK: Int = 5,
        val metric: String = "COSINE" // COSINE, EUCLIDEAN, DOT_PRODUCT, MANHATTAN
)

data class CompareMetricsRequest(val query: String, val topK: Int = 3)
