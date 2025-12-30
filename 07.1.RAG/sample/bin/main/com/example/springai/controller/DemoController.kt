package com.example.springai.controller

import com.example.springai.service.KnowledgeBaseService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 데모 데이터 초기화 컨트롤러
 * 
 * RAG 패턴을 테스트하기 위한 샘플 데이터를 추가합니다.
 */
@RestController
@RequestMapping("/api/demo")
class DemoController(
    private val knowledgeBaseService: KnowledgeBaseService
) {
    /**
     * 샘플 데이터 초기화
     * POST http://localhost:8080/api/demo/init
     */
    @PostMapping("/init")
    fun initSampleData(): Map<String, Any> {
        // 샘플 문서 추가
        val documents = listOf(
            "환불 정책" to """
                환불 정책
                
                구매 후 7일 이내, 미사용 제품에 한해 환불이 가능합니다.
                환불 신청은 고객센터로 연락하시거나 온라인으로 신청하실 수 있습니다.
                환불 처리 기간은 신청 후 3-5일 소요됩니다.
            """.trimIndent(),
            
            "배송 정책" to """
                배송 정책
                
                주문 후 1-2일 내 배송이 시작됩니다.
                배송비는 3만원 이상 구매 시 무료입니다.
                배송 추적은 주문 번호로 확인하실 수 있습니다.
            """.trimIndent(),
            
            "교환 정책" to """
                교환 정책
                
                제품에 하자가 있는 경우 30일 이내 교환이 가능합니다.
                교환 신청은 고객센터로 연락하시면 됩니다.
                교환 배송비는 회사에서 부담합니다.
            """.trimIndent(),
            
            "회원 혜택" to """
                회원 혜택
                
                일반 회원: 구매 금액의 1% 적립
                VIP 회원: 구매 금액의 3% 적립, 무료 배송
                회원 등급은 연간 구매 금액에 따라 결정됩니다.
            """.trimIndent(),
            
            "A/S 정책" to """
                A/S 정책
                
                제품 구매 후 1년 이내 무상 A/S가 가능합니다.
                A/S 신청은 고객센터나 온라인으로 신청하실 수 있습니다.
                방문 A/S는 수도권 지역만 가능합니다.
            """.trimIndent()
        )
        
        knowledgeBaseService.addDocuments(documents, source = "회사 정책 문서")
        
        return mapOf(
            "status" to "success",
            "message" to "샘플 데이터가 초기화되었습니다.",
            "documentCount" to documents.size
        )
    }
}

