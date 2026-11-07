package com.monzer.hares

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HaresGuardService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkClipboardForLinks()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    private fun checkClipboardForLinks() {
        if (!SecurityManager.isClipboardProtectionEnabled(applicationContext)) return

        val clipData: ClipData? = clipboardManager.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString() ?: return
            val analysisResult = LinkSafetyAnalyzer.analyzeUrl(text)

            if (analysisResult.riskLevel != LinkSafetyAnalyzer.RiskLevel.SAFE) {
                val db = HaresDatabase.getDatabase(applicationContext)
                CoroutineScope(Dispatchers.IO).launch {
                    db.securityLogDao().insertLog(
                        SecurityLogEntity(
                            url = analysisResult.url,
                            riskLevel = analysisResult.riskLevel.name,
                            reason = analysisResult.reasons.firstOrNull() ?: "تهديد أمني"
                        )
                    )
                }

                Toast.makeText(
                    applicationContext,
                    "⚠️ تحذير حارس: رابط مشبوه في الحافظة!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onInterrupt() {
        try {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
