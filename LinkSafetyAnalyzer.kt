package com.monzer.hares

import android.net.Uri
import java.net.IDN
import java.util.Locale

data class LinkAssessment(
    val normalizedUrl: String,
    val host: String,
    val risk: Risk,
    val reasons: List<String>
) {
    enum class Risk { LOW, REVIEW, HIGH }
}

/**
 * Offline URL triage layer.
 * This uses transparent heuristics, not a trained AI model and not a definitive adult-content detector.
 * A real on-device model and maintained domain reputation list can be plugged in later.
 */
object LinkSafetyAnalyzer {
    private val suspiciousTerms = listOf(
        "porn", "xxx", "sex-video", "adult-video", "hentai", "camgirl", "nude"
    )
    private val redirectParameters = listOf("url", "target", "redirect", "redirect_url", "redir", "dest", "destination", "continue", "out")

    fun analyze(input: String): LinkAssessment {
        val raw = input.trim()
        if (raw.isEmpty()) {
            return LinkAssessment("", "", LinkAssessment.Risk.HIGH, listOf("أدخل رابطًا أولًا"))
        }

        val withScheme = if (raw.contains("://")) raw else "https://$raw"
        val uri = try { Uri.parse(withScheme) } catch (_: Exception) { null }
        val rawHost = uri?.host?.trimEnd('.')?.lowercase(Locale.ROOT).orEmpty()
        if (uri == null || rawHost.isBlank() || rawHost.contains(' ')) {
            return LinkAssessment(withScheme, "", LinkAssessment.Risk.HIGH, listOf("صيغة الرابط غير صالحة أو لا تحتوي على اسم نطاق واضح"))
        }

        val host = try { IDN.toASCII(rawHost).lowercase(Locale.ROOT) } catch (_: Exception) { rawHost }
        val reasons = mutableListOf<String>()
        var score = 0

        if (uri.scheme?.lowercase(Locale.ROOT) != "https") {
            score += 1
            reasons += "الرابط لا يستخدم HTTPS"
        }
        if (raw.substringAfter("://", "").substringBefore('/').contains('@')) {
            score += 3
            reasons += "يحتوي الرابط على بيانات دخول أو علامة @ قد تُستخدم لإخفاء الوجهة"
        }
        if (host.startsWith("xn--") || host.split('.').any { it.startsWith("xn--") }) {
            score += 2
            reasons += "النطاق يحتوي على ترميز دولي قد يُستغل لتشابه الحروف"
        }
        if (host.count { it == '.' } >= 4) {
            score += 1
            reasons += "اسم النطاق طويل أو كثير المستويات"
        }
        if (host.matches(Regex("""\d{1,3}(\.\d{1,3}){3}"""))) {
            score += 2
            reasons += "الرابط يستخدم عنوان IP بدل اسم نطاق مألوف"
        }

        val decodedText = (host + " " + (uri.path ?: "") + " " + (uri.query ?: ""))
            .lowercase(Locale.ROOT)
        val matchedTerms = suspiciousTerms.filter { decodedText.contains(it) }
        if (matchedTerms.isNotEmpty()) {
            score += 4
            reasons += "ظهرت كلمات مرتبطة بمحتوى للبالغين في الرابط"
        }
        val queryNames = uri.queryParameterNames.map { it.lowercase(Locale.ROOT) }
        if (queryNames.any { it in redirectParameters }) {
            score += 1
            reasons += "الرابط قد يمرّر المستخدم عبر إعادة توجيه"
        }
        if (raw.length > 220) {
            score += 1
            reasons += "الرابط طويل بشكل غير معتاد"
        }

        val normalized = Uri.Builder()
            .scheme(uri.scheme?.lowercase(Locale.ROOT) ?: "https")
            .encodedAuthority(uri.encodedAuthority)
            .encodedPath(uri.encodedPath ?: "")
            .encodedQuery(uri.encodedQuery)
            .encodedFragment(null)
            .build().toString()

        val risk = when {
            score >= 4 -> LinkAssessment.Risk.HIGH
            score >= 2 -> LinkAssessment.Risk.REVIEW
            else -> LinkAssessment.Risk.LOW
        }
        if (reasons.isEmpty()) reasons += "لم تظهر مؤشرات واضحة في فحص الرابط الأساسي؛ هذا لا يضمن أن الصفحة آمنة"
        return LinkAssessment(normalized, host, risk, reasons)
    }
}
