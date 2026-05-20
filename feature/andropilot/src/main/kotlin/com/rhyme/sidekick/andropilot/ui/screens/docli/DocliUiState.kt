package com.rhyme.sidekick.andropilot.ui.screens.docli


/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.ui.screens.docli
 *
 * @author 李春俊-Rhyme
 * @since 08:34 2026/5/12 星期二
 * @version 1.0
 */

/**
 * 文档搜索界面 - 搜索框
 */
data class DocliUiSearchBoxState(
    val identity: DocliUiIdentity = DocliUiIdentity.SearchBox,
    val searchQuery: String = "",
    val cursorPosition: Int = 0, // 光标位置（0 到 searchQuery.length）
    val focus: Boolean = false
)

/**
 * 文档搜索界面 - 结果列表
 */
data class DocliUiResultListState(
    val identity: DocliUiIdentity = DocliUiIdentity.ResultList,
    val selectedIndex: Int = 0,
    val resultList: List<DocBrief> = emptyList(),
    val focus: Boolean = false
)

//////////////////// 文档搜索界面 ////////////////////////////

/**
 * 文档详情界面 - 详情页
 */
data class DocliUiDetailPageState(
    val identity: DocliUiIdentity = DocliUiIdentity.DetailPage,
    val docDetail: DocDetail = DocDetail(),
    val focus: Boolean = false,
    val scrollPercentage: Float = 0f // 滚动百分比（0.0-1.0），表示当前视口在全文中的位置
)

//////////////////// 文档详情界面 ////////////////////////////

/**
 * 文档界面 - UI组件标识
 */
enum class DocliUiIdentity {
    SearchBox, // 搜索框
    ResultList, // 结果列表
    DetailPage, // 详情页
}

//////////////////// 文档展示界面 ////////////////////////////

/**
 * 文档简易信息数据模型
 */
data class DocBrief(
    val title: String = "",
    val description: String = "",
    val url: String = ""  // kb://格式的URL
)

/**
 * 文档详细信息数据模型
 */
data class DocDetail(
    val title: String = "",
    val description: String = "",
    val rawContent: String = "",
    val mediaList: List<DocMedia> = listOf()
)

/**
 * 文档媒体信息数据模型
 */
sealed class DocMedia(mediaType: MediaType) {
    data class DocMediaLink(val link: String, val displayText: String) : DocMedia(MediaType.Link)
    data class DocMediaImage(val url: String, val altText: String? = null) : DocMedia(MediaType.Image)
}

/**
 * 文档媒体信息枚举
 */
enum class MediaType {
    Link, // 链接
    Image, // 图片
}

//////////////////// 文档数据模型 ////////////////////////////

/**
 * UI Status - 文档界面模式状态
 */
enum class DocliUiStatus {
    View, // 查看文档状态
    Input, // 输入文本状态
    Loading, // 加载中状态
}

/**
 * UI State - 文档界面内容状态
 */
data class DocliUiState(
    val status: DocliUiStatus = DocliUiStatus.View, // 当前UI所处模式状态
    val pendingExit: Boolean = false, // 退出
    val pendingNavigation: Boolean = false, // 导航
    val showingHelp: Boolean = false, // 帮助
)

//////////////////// 文档界面状态 ////////////////////////////
