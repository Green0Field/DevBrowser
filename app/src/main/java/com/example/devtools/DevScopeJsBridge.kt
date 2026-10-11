package com.example.devtools

import android.webkit.JavascriptInterface
import com.example.model.AuditItem
import com.example.model.BoxModelMetrics
import com.example.model.ConsoleLogLevel
import com.example.model.DomNodeItem
import com.example.model.NetworkRequestEntry
import com.example.model.NetworkResourceType
import com.example.model.PerformanceMetrics
import com.example.model.StorageEntry
import com.example.model.StorageType
import org.json.JSONArray
import org.json.JSONObject

class DevScopeJsBridge(
    private val onConsoleLogReceived: (ConsoleLogLevel, String, String) -> Unit,
    private val onDomTreeReceived: (List<DomNodeItem>) -> Unit,
    private val onNodeSelectedReceived: (DomNodeItem, Boolean) -> Unit,
    private val onNetworkRequestReceived: (NetworkRequestEntry) -> Unit,
    private val onStorageReceived: (List<StorageEntry>) -> Unit,
    private val onPerformanceReceived: (PerformanceMetrics) -> Unit,
    private val onSourcesReceived: (String) -> Unit
) {

    @JavascriptInterface
    fun onConsoleLog(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            val rawLevel = obj.optString("level", "LOG").uppercase()
            val level = when (rawLevel) {
                "INFO" -> ConsoleLogLevel.INFO
                "WARN" -> ConsoleLogLevel.WARN
                "ERROR" -> ConsoleLogLevel.ERROR
                else -> ConsoleLogLevel.LOG
            }
            val message = obj.optString("message", "")
            val source = obj.optString("source", "console")
            onConsoleLogReceived(level, message, source)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onDomTreeUpdated(jsonStr: String) {
        try {
            val arr = JSONArray(jsonStr)
            val list = ArrayList<DomNodeItem>(arr.length())
            for (i in 0 until arr.length()) {
                val itemObj = arr.optJSONObject(i) ?: continue
                list.add(parseDomNode(itemObj))
            }
            onDomTreeReceived(list)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onNodeSelected(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            onNodeSelectedReceived(parseDomNode(obj), false)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onElementPickedFromPage(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            onNodeSelectedReceived(parseDomNode(obj), true)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onNetworkRequest(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            val reqHeaders = mutableMapOf<String, String>()
            obj.optJSONObject("requestHeaders")?.let { hObj ->
                val keys = hObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    reqHeaders[k] = hObj.optString(k, "")
                }
            }
            val respHeaders = mutableMapOf<String, String>()
            obj.optJSONObject("responseHeaders")?.let { hObj ->
                val keys = hObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    respHeaders[k] = hObj.optString(k, "")
                }
            }
            val rawType = obj.optString("resourceType", "FETCH_XHR")
            val resType = try {
                NetworkResourceType.valueOf(rawType)
            } catch (_: Exception) {
                NetworkResourceType.FETCH_XHR
            }
            val entry = NetworkRequestEntry(
                id = obj.optString("id", "req_${System.currentTimeMillis()}"),
                method = obj.optString("method", "GET"),
                url = obj.optString("url", ""),
                status = obj.optInt("status", 200),
                statusText = obj.optString("statusText", "OK"),
                resourceType = resType,
                mimeType = obj.optString("mimeType", "application/json"),
                startTimeOffsetMs = obj.optLong("startTimeOffsetMs", 0L),
                durationMs = obj.optLong("durationMs", 25L),
                sizeBytes = obj.optInt("sizeBytes", 0),
                requestHeaders = reqHeaders,
                responseHeaders = respHeaders,
                requestBody = obj.optString("requestBody", ""),
                responsePreview = obj.optString("responsePreview", ""),
                isError = obj.optBoolean("isError", false)
            )
            onNetworkRequestReceived(entry)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onStorageUpdated(jsonStr: String) {
        try {
            val arr = JSONArray(jsonStr)
            val list = ArrayList<StorageEntry>(arr.length())
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val typeStr = obj.optString("type", "LOCAL_STORAGE")
                val type = try {
                    StorageType.valueOf(typeStr)
                } catch (_: Exception) {
                    StorageType.LOCAL_STORAGE
                }
                list.add(
                    StorageEntry(
                        key = obj.optString("key", ""),
                        value = obj.optString("value", ""),
                        type = type
                    )
                )
            }
            onStorageReceived(list)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onPerformanceUpdated(jsonStr: String) {
        try {
            val obj = JSONObject(jsonStr)
            val auditsArr = obj.optJSONArray("audits") ?: JSONArray()
            val audits = ArrayList<AuditItem>(auditsArr.length())
            for (i in 0 until auditsArr.length()) {
                val a = auditsArr.optJSONObject(i) ?: continue
                audits.add(
                    AuditItem(
                        category = a.optString("category", "Performance"),
                        title = a.optString("title", ""),
                        description = a.optString("description", ""),
                        passed = a.optBoolean("passed", true),
                        metricValue = a.optString("metricValue", "")
                    )
                )
            }
            val metrics = PerformanceMetrics(
                domNodesCount = obj.optInt("domNodesCount", 0),
                maxDomDepth = obj.optInt("maxDomDepth", 0),
                scriptsCount = obj.optInt("scriptsCount", 0),
                stylesheetsCount = obj.optInt("stylesheetsCount", 0),
                imagesCount = obj.optInt("imagesCount", 0),
                missingAltImagesCount = obj.optInt("missingAltImagesCount", 0),
                domContentLoadedMs = obj.optLong("domContentLoadedMs", 0L),
                loadEventMs = obj.optLong("loadEventMs", 0L),
                firstPaintMs = obj.optLong("firstPaintMs", 0L),
                firstContentfulPaintMs = obj.optLong("firstContentfulPaintMs", 0L),
                jsHeapUsedMb = obj.optDouble("jsHeapUsedMb", 8.4).toFloat(),
                jsHeapTotalMb = obj.optDouble("jsHeapTotalMb", 24.0).toFloat(),
                performanceScore = obj.optInt("performanceScore", 95),
                accessibilityScore = obj.optInt("accessibilityScore", 92),
                bestPracticesScore = obj.optInt("bestPracticesScore", 96),
                seoScore = obj.optInt("seoScore", 94),
                audits = audits
            )
            onPerformanceReceived(metrics)
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun onSourcesUpdated(htmlSource: String) {
        onSourcesReceived(htmlSource)
    }

    private fun parseDomNode(obj: JSONObject): DomNodeItem {
        val attrs = mutableMapOf<String, String>()
        obj.optJSONObject("attributes")?.let { attrObj ->
            val keys = attrObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                attrs[k] = attrObj.optString(k, "")
            }
        }
        val computed = mutableMapOf<String, String>()
        obj.optJSONObject("computedStyles")?.let { compObj ->
            val keys = compObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = compObj.optString(k, "")
                if (v.isNotBlank()) computed[k] = v
            }
        }
        val bmObj = obj.optJSONObject("boxModel")
        val boxModel = if (bmObj != null) {
            BoxModelMetrics(
                width = bmObj.optInt("width", 0),
                height = bmObj.optInt("height", 0),
                marginTop = bmObj.optInt("marginTop", 0),
                marginRight = bmObj.optInt("marginRight", 0),
                marginBottom = bmObj.optInt("marginBottom", 0),
                marginLeft = bmObj.optInt("marginLeft", 0),
                borderTop = bmObj.optInt("borderTop", 0),
                borderRight = bmObj.optInt("borderRight", 0),
                borderBottom = bmObj.optInt("borderBottom", 0),
                borderLeft = bmObj.optInt("borderLeft", 0),
                paddingTop = bmObj.optInt("paddingTop", 0),
                paddingRight = bmObj.optInt("paddingRight", 0),
                paddingBottom = bmObj.optInt("paddingBottom", 0),
                paddingLeft = bmObj.optInt("paddingLeft", 0)
            )
        } else {
            BoxModelMetrics()
        }

        return DomNodeItem(
            path = obj.optString("path", ""),
            cssSelector = obj.optString("cssSelector", ""),
            tag = obj.optString("tag", "div"),
            idAttr = obj.optString("idAttr", ""),
            classAttr = obj.optString("classAttr", ""),
            attributes = attrs,
            textPreview = obj.optString("textPreview", ""),
            childCount = obj.optInt("childCount", 0),
            depth = obj.optInt("depth", 0),
            inlineStyle = obj.optString("inlineStyle", ""),
            computedStyles = computed,
            boxModel = boxModel,
            outerHtmlPreview = obj.optString("outerHtmlPreview", "")
        )
    }
}
