package com.rhyme.sidekick.andropilot.domain.model

import com.rhyme.sidekick.andropilot.ui.screens.docli.DocBrief
import com.rhyme.sidekick.andropilot.ui.screens.docli.DocDetail
import com.rhyme.sidekick.andropilot.ui.screens.docli.DocMedia

/**
 * Android文档搜索结果项
 */
data class AndroidDocSearchResult(
    val index: Int,                    // 序号
    val title: String,                 // 标题
    val url: String,                   // URL (kb://格式)
    val description: String            // 描述摘要
) {
    /**
     * 转换为DocBrief用于UI展示
     */
    fun toDocBrief(): DocBrief {
        return DocBrief(
            title = title,
            description = description,
            url = url  // 保留URL以便后续获取详情
        )
    }
}

/**
 * Android文档详情
 */
data class AndroidDocDetail(
    val title: String,                 // 标题
    val url: String,                   // URL
    val content: String,               // 原始内容
    val description: String = "",      // 描述摘要（从web-summary提取）
    val images: List<DocImage> = emptyList(),  // 图片列表
    val links: List<DocLink> = emptyList()     // 链接列表
) {
    /**
     * 转换为DocDetail用于UI展示
     */
    fun toDocDetail(): DocDetail {
        // 将图片和链接都转换为DocMedia
        val mediaItems = mutableListOf<DocMedia>()
        mediaItems.addAll(images.map { it.toDocMedia() })
        mediaItems.addAll(links.map { it.toDocMedia() })
        
        return DocDetail(
            title = title,
            description = description,  // 使用已提取的description
            rawContent = content,
            mediaList = mediaItems
        )
    }
}

/**
 * 文档中的图片信息
 */
data class DocImage(
    val url: String,                   // 图片URL
    val altText: String?,              // 替代文本
    val caption: String?               // 说明文字
) {
    fun toDocMedia(): DocMedia.DocMediaImage {
        return DocMedia.DocMediaImage(url = url, altText = altText)
    }
}

/**
 * 文档中的链接信息
 */
data class DocLink(
    val url: String,                   // 链接URL
    val displayText: String            // 显示文本
) {
    fun toDocMedia(): DocMedia.DocMediaLink {
        return DocMedia.DocMediaLink(link = url, displayText = displayText)
    }
}

/**
 * 命令执行结果
 */
sealed class CommandResult {
    data class Success(val output: String) : CommandResult()
    data class Error(val message: String, val exitCode: Int) : CommandResult()
}
