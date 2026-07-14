package com.xiao.wordshow.util

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader
import java.util.zip.ZipFile

object WordParser {

    /**
     * 从 docx 文件提取纯文本，按句号/问号/感叹号/换行拆分为句子列表
     */
    fun parseDocx(context: Context, uri: Uri): List<String> {
        val text = StringBuilder()
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                // 写入临时文件以便ZipFile读取
                val tmpFile = java.io.File(context.cacheDir, "temp.docx")
                tmpFile.outputStream().use { input.copyTo(it) }
                ZipFile(tmpFile).use { zip ->
                    zip.getInputStream(zip.getEntry("word/document.xml"))?.use { xmlStream ->
                        val parser = XmlPullParserFactory.newInstance().newPullParser()
                        parser.setInput(InputStreamReader(xmlStream))
                        var event = parser.eventType
                        while (event != XmlPullParser.END_DOCUMENT) {
                            if (event == XmlPullParser.START_TAG) {
                                when (parser.name) {
                                    "w:t" -> text.append(parser.nextText())
                                    "w:br", "w:cr" -> text.append("\n")
                                }
                            }
                            event = parser.next()
                        }
                    }
                    tmpFile.delete()
                }
            }
        } catch (e: Exception) {
            return emptyList()
        }

        // 拆分为句子
        return text.toString()
            .replace(Regex("\\s+"), " ")
            .split(Regex("(?<=[。！？!?\\n])"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}
