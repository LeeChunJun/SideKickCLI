package com.rhyme.sidekick.andropilot.data.repository

import com.rhyme.sidekick.andropilot.config.AndroidCliConfig
import com.rhyme.sidekick.andropilot.domain.model.AndroidDocDetail
import com.rhyme.sidekick.andropilot.domain.model.AndroidDocSearchResult
import com.rhyme.sidekick.andropilot.domain.model.CommandResult
import com.rhyme.sidekick.andropilot.domain.model.DocImage
import com.rhyme.sidekick.andropilot.domain.model.DocLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Android文档数据仓库
 * 负责执行android.exe命令并解析输出结果
 */
class AndroidDocsRepository {

    /**
     * android.exe的完整路径
     */
    private val androidExePath = AndroidCliConfig.androidExePath

    /**
     * 搜索Android文档
     * @param query 搜索关键词
     * @return 搜索结果列表
     */
    suspend fun searchDocs(query: String): Result<List<AndroidDocSearchResult>> {
        return withContext(Dispatchers.IO) {
            try {
                // 执行命令: android docs search <query>
                val command = listOf(androidExePath, "docs", "search", query)
                val result = executeCommand(command)

                when (result) {
                    is CommandResult.Success -> {
                        val searchResults = parseSearchResults(result.output)
                        Result.success(searchResults)
                    }
                    is CommandResult.Error -> {
                        Result.failure(Exception("命令执行失败: ${result.message} (退出码: ${result.exitCode})"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取文档详情
     * @param kbUrl 知识库URL (kb://格式)
     * @return 文档详情
     */
    suspend fun fetchDocDetail(kbUrl: String): Result<AndroidDocDetail> {
        return withContext(Dispatchers.IO) {
            try {
                // 执行命令: android docs fetch <kb-url>
                val command = listOf(androidExePath, "docs", "fetch", kbUrl)
                val result = executeCommand(command)

                when (result) {
                    is CommandResult.Success -> {
                        val docDetail = parseDocDetail(result.output)
                        Result.success(docDetail)
                    }
                    is CommandResult.Error -> {
                        Result.failure(Exception("命令执行失败: ${result.message} (退出码: ${result.exitCode})"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 执行外部命令
     */
    private fun executeCommand(command: List<String>): CommandResult {
        val executor = Executors.newSingleThreadExecutor()
        return try {
            val processBuilder = ProcessBuilder(command)
            processBuilder.redirectErrorStream(true) // 合并标准错误和标准输出
            
            val process = processBuilder.start()
            
            // 使用Future来异步读取输出，支持超时控制
            val outputFuture = executor.submit<String> {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.readText()
                }
            }
            
            // 等待进程完成，带超时控制
            val completed = process.waitFor(AndroidCliConfig.COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            
            if (!completed) {
                // 超时，销毁进程
                process.destroyForcibly()
                outputFuture.cancel(true)
                return CommandResult.Error("命令执行超时 (${AndroidCliConfig.COMMAND_TIMEOUT_MS}ms)", -2)
            }
            
            // 获取输出，带超时
            val output = try {
                outputFuture.get(5000, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                ""
            }

            val exitCode = process.exitValue()

            if (exitCode == 0) {
                CommandResult.Success(output)
            } else {
                CommandResult.Error(output, exitCode)
            }
        } catch (e: Exception) {
            CommandResult.Error(e.message ?: "未知错误", -1)
        } finally {
            executor.shutdown()
        }
    }

    /**
     * 解析搜索结果
     * 根据txt文件格式解析:
     * 1. Compose Multiplatform and Jetpack Compose
     *    URL: kb://JetBrains/kotlin-multiplatform-dev-docs/topics/compose/...
     *    This article explains...
     */
    private fun parseSearchResults(output: String): List<AndroidDocSearchResult> {
        val results = mutableListOf<AndroidDocSearchResult>()
        val lines = output.lines()
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            
            // 匹配序号行: "1. Title"
            val matchResult = Regex("""^(\d+)\.\s+(.+)$""").find(line)
            if (matchResult != null) {
                val index = matchResult.groupValues[1].toInt()
                val title = matchResult.groupValues[2].trim()
                
                // 下一行应该是URL
                var url = ""
                var description = ""
                
                if (i + 1 < lines.size) {
                    val urlLine = lines[i + 1].trim()
                    val urlMatch = Regex("""^URL:\s+(.+)$""").find(urlLine)
                    if (urlMatch != null) {
                        url = urlMatch.groupValues[1].trim()
                        
                        // 再下一行是描述
                        if (i + 2 < lines.size) {
                            description = lines[i + 2].trim()
                        }
                    }
                }
                
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    results.add(
                        AndroidDocSearchResult(
                            index = index,
                            title = title,
                            url = url,
                            description = description
                        )
                    )
                }
                
                i += 3 // 跳过这三行
            } else {
                i++
            }
        }
        
        return results
    }

    /**
     * 解析文档详情
     * 根据txt文件格式解析:
     * Title: Shapes in Compose
     * URL: kb://android/develop/ui/compose/graphics/draw/shapes
     * ----------------------------------------
     * [内容开始]
     */
    private fun parseDocDetail(output: String): AndroidDocDetail {
        val lines = output.lines()
        
        var title = ""
        var url = ""
        val contentLines = mutableListOf<String>()
        val images = mutableListOf<DocImage>()
        val links = mutableListOf<DocLink>()
        
        var contentStarted = false
        
        for (line in lines) {
            if (!contentStarted) {
                // 解析标题
                val titleMatch = Regex("""^Title:\s+(.+)$""").find(line.trim())
                if (titleMatch != null) {
                    title = titleMatch.groupValues[1].trim()
                    continue
                }
                
                // 解析URL
                val urlMatch = Regex("""^URL:\s+(.+)$""").find(line.trim())
                if (urlMatch != null) {
                    url = urlMatch.groupValues[1].trim()
                    continue
                }
                
                // 检测分隔线
                if (line.trim().startsWith("---")) {
                    contentStarted = true
                    continue
                }
            } else {
                // 提取图片信息: ![alt text](url)
                val imageMatch = Regex("""!\[(.*?)\]\((.*?)\)""").findAll(line)
                imageMatch.forEach { match ->
                    val altText = match.groupValues[1]
                    val imageUrl = match.groupValues[2]
                    images.add(DocImage(url = imageUrl, altText = altText, caption = null))
                }
                
                // 提取链接信息: [display text](url) - 但不是图片
                // 先移除图片标记，避免重复匹配
                val lineWithoutImages = line.replace(Regex("""!\[(.*?)\]\((.*?)\)"""), "")
                val linkMatch = Regex("""\[(.*?)\]\((.*?)\)""").findAll(lineWithoutImages)
                linkMatch.forEach { match ->
                    val displayText = match.groupValues[1]
                    val linkUrl = match.groupValues[2]
                    links.add(DocLink(url = linkUrl, displayText = displayText))
                }
                
                contentLines.add(line)
            }
        }
        
        val content = contentLines.joinToString("\n").trim()
        // 从content中提取web-summary作为description
        val description = extractWebSummary(content)
        
        return AndroidDocDetail(
            title = title,
            url = url,
            content = content,
            description = description,
            images = images,
            links = links
        )
    }
    
    /**
     * 从content中提取<web-summary></web-summary>标签内的内容
     */
    private fun extractWebSummary(content: String): String {
        val webSummaryRegex = Regex("""<web-summary>(.*?)</web-summary>""", RegexOption.DOT_MATCHES_ALL)
        val matchResult = webSummaryRegex.find(content)
        return matchResult?.groupValues?.get(1)?.trim() ?: ""
    }
}
