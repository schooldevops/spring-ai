package com.example.springai.util

import com.fasterxml.jackson.databind.ObjectMapper

class BeanOutputParser<T : Any>(
        private val clazz: Class<T>,
        private val objectmapper: ObjectMapper =
                com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
) {

    val format: String
        get() {
            val schema = generateJsonSchema(clazz)
            return """
            {
              ${schema.lines().joinToString("\n  ") { "  $it" } }
            }
            """.trimIndent()
        }

    fun parse(jsonText: String): T {
        val cleanedJson = JsonCleaner.cleanJsonText(jsonText)
        return try {
            @Suppress("UNCHECKED_CAST") objectmapper.readValue(cleanedJson, clazz) as T
        } catch (e: Exception) {
            throw IllegalArgumentException("JSON 파싱 실패: ${e.message}, ${cleanedJson}", e)
        }
    }

    private fun generateJsonSchema(clazz: Class<T>): String {
        val fields = clazz.declaredFields
        val schemaFields =
                fields.mapNotNull { field ->
                    val fieldName = field.name
                    val fieldType =
                            when {
                                field.type == String::class.java -> "\"$fieldName\": \"string\""
                                field.type == Int::class.java ||
                                        field.type == Integer::class.java ->
                                        "\"$fieldName\": \"integer\""
                                field.type == Long::class.java ||
                                        field.type == java.lang.Long::class.java ->
                                        "\"$fieldName\": \"integer\""
                                field.type == Double::class.java ||
                                        field.type == java.lang.Double::class.java ->
                                        "\"$fieldName\": \"number\""
                                field.type == Boolean::class.java ||
                                        field.type == java.lang.Boolean::class.java ->
                                        "\"$fieldName\": \"boolean\""
                                List::class.java.isAssignableFrom(field.type) ->
                                        "\"$fieldName\": \"array\""
                                else -> null // 복잡한 타입은 생략
                            }
                    fieldType
                }
        return schemaFields.joinToString(",\n")
    }
}
