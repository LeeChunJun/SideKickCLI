package com.rhyme.sidekick.andropilot.ui.screens.docli

import com.rhyme.sidekick.andropilot.domain.service.AndroidDocsService
import com.rhyme.sidekick.andropilot.ui.components.Direction
import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope
import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 项目名：SideKickCLI
 * 包名：com.rhyme.sidekick.andropilot.ui.screens.docli
 *
 * @author 李春俊-Rhyme
 * @since 11:53 2026/5/10 星期日
 * @version 1.0
 */
class DocliViewModel : ViewModel() {

    private val docsService = AndroidDocsService()

    // User Intent Channel - 只取最新的一个事件
    private val _userIntent = MutableSharedFlow<DocliUiAction>(replay = 0, extraBufferCapacity = 1)
    val userIntent = _userIntent.asSharedFlow() // 让外部能够观察用户意图（例如用于测试或日志记录）

    // UI State - 界面状态
    private val _uiState = MutableStateFlow(DocliUiState())
    val uiState: StateFlow<DocliUiState> = _uiState.asStateFlow()

    // 搜索框状态
    private val _searchBoxState = MutableStateFlow(DocliUiSearchBoxState())
    val searchBoxState: StateFlow<DocliUiSearchBoxState> = _searchBoxState.asStateFlow()

    // 结果列表状态
    private val _resultListState = MutableStateFlow(DocliUiResultListState())
    val resultListState: StateFlow<DocliUiResultListState> = _resultListState.asStateFlow()

    // 详情页状态
    private val _detailPageState = MutableStateFlow(DocliUiDetailPageState())
    val detailPageState: StateFlow<DocliUiDetailPageState> = _detailPageState.asStateFlow()

    // 保存原始搜索结果，用于过滤
    private var originalSearchResults: List<DocBrief> = emptyList()

    init {
        viewModelScope.launch {
            // 收集用户意图并处理
            _userIntent.collect { action ->
                processAction(action)
            }
        }
    }

    /**
     * 发送用户意图
     */
    fun sendAction(action: DocliUiAction) {
        viewModelScope.launch {
            _userIntent.emit(action)
        }
    }

    /**
     * 处理用户意图
     */
    private fun processAction(action: DocliUiAction) {
        when (action) {
            is DocliUiAction.Initialize -> handleInitialize(action.searchResults)
            is DocliUiAction.ClearPendingActions -> handleClearPendingActions()

            // 查看模式操作
            is DocliUiAction.NavigateUp -> handleNavigateUp()
            is DocliUiAction.NavigateDown -> handleNavigateDown()
            is DocliUiAction.MoveCursor -> handleMoveCursor(action.direction)
            is DocliUiAction.Confirm -> handleConfirm()
            is DocliUiAction.Exit -> handleExit()
            is DocliUiAction.ShowHelp -> handleShowHelp()
            is DocliUiAction.FocusSearchBox -> handleFocusSearchBox()
            is DocliUiAction.SwitchSearchList -> handleSwitchSearchList()

            // 详情页操作
            is DocliUiAction.SwitchDetailPage -> handleSwitchDetailPage()

            // 输入模式操作
            is DocliUiAction.FinishInput -> handleFinishInput()
            is DocliUiAction.ExitInput -> handleExitInput()
            is DocliUiAction.UpdateInput -> handleUpdateInput(action.content)
            is DocliUiAction.Backspace -> handleBackspace()
            is DocliUiAction.Delete -> handleDelete()
            is DocliUiAction.MoveToLineStart -> handleMoveToLineStart()
            is DocliUiAction.MoveToLineEnd -> handleMoveToLineEnd()
            is DocliUiAction.MoveCursorLeft -> handleMoveCursorLeft()
            is DocliUiAction.MoveCursorRight -> handleMoveCursorRight()

            // 加载模式操作
            is DocliUiAction.FinishLoading -> handleFinishLoading()
        }
    }

    private fun handleInitialize(searchResults: List<DocBrief>) {
        val currentState = _uiState.value
        // 只在搜索结果变化时才重置选中索引
        val shouldResetIndex = _resultListState.value.resultList != searchResults

        // 保存原始搜索结果
        originalSearchResults = searchResults

        // 更新结果列表状态
        _resultListState.value = _resultListState.value.copy(
            resultList = searchResults,
            selectedIndex = if (shouldResetIndex) 0 else _resultListState.value.selectedIndex.coerceAtMost(searchResults.size - 1),
            focus = true
        )

        // 重置其他组件的焦点
        _searchBoxState.value = _searchBoxState.value.copy(focus = false)
        _detailPageState.value = _detailPageState.value.copy(focus = false)

        // 更新主UI状态
        _uiState.value = currentState.copy(
            status = DocliUiStatus.View
        )
    }

    private fun handleUpdateInput(content: String) {
        // 在输入模式下更新搜索框内容，在光标位置插入字符
        // 验证输入内容为单个可打印字符
        if (content.length == 1 && content[0] in ' '..'~') {
            val currentState = _searchBoxState.value
            val cursorPos = currentState.cursorPosition
            val currentQuery = currentState.searchQuery

            // 在光标位置插入字符
            val newQuery = currentQuery.substring(0, cursorPos) + content + currentQuery.substring(cursorPos)

            _searchBoxState.value = currentState.copy(
                searchQuery = newQuery,
                cursorPosition = cursorPos + 1 // 光标后移一位
            )
        }
    }

    private fun handleBackspace() {
        // 退格删除：删除光标前的一个字符
        val currentState = _searchBoxState.value
        val cursorPos = currentState.cursorPosition
        val currentQuery = currentState.searchQuery

        if (cursorPos > 0 && currentQuery.isNotEmpty()) {
            // 删除光标前的字符
            val newQuery = currentQuery.substring(0, cursorPos - 1) + currentQuery.substring(cursorPos)
            _searchBoxState.value = currentState.copy(
                searchQuery = newQuery,
                cursorPosition = cursorPos - 1 // 光标前移一位
            )
        }
    }

    private fun handleDelete() {
        // Delete键：删除光标位置的字符（即光标右侧的第一个字符）
        // 示例："abc|def" (|表示光标位置在c和d之间)
        //       cursorPosition = 3
        //       删除后: "abcef" (删除了d)
        //       cursorPosition 保持为 3
        val currentState = _searchBoxState.value
        val cursorPos = currentState.cursorPosition
        val currentQuery = currentState.searchQuery

        if (cursorPos < currentQuery.length) {
            // 保留光标前的部分 + 跳过光标位置的字符 + 保留后面的部分
            val newQuery = currentQuery.substring(0, cursorPos) + currentQuery.substring(cursorPos + 1)
            _searchBoxState.value = currentState.copy(
                searchQuery = newQuery
                // 光标位置不变，因为删除的是光标后的字符
            )
        }
    }

    private fun handleMoveToLineStart() {
        // 移动到行首：光标位置设为 0
        val currentState = _searchBoxState.value
        _searchBoxState.value = currentState.copy(
            cursorPosition = 0
        )
    }

    private fun handleMoveToLineEnd() {
        // 移动到行尾：光标位置设为文本长度
        val currentState = _searchBoxState.value
        _searchBoxState.value = currentState.copy(
            cursorPosition = currentState.searchQuery.length
        )
    }

    private fun handleMoveCursorLeft() {
        // 光标左移一位
        val currentState = _searchBoxState.value
        if (currentState.cursorPosition > 0) {
            _searchBoxState.value = currentState.copy(
                cursorPosition = currentState.cursorPosition - 1
            )
        }
    }

    private fun handleMoveCursorRight() {
        // 光标右移一位
        val currentState = _searchBoxState.value
        if (currentState.cursorPosition < currentState.searchQuery.length) {
            _searchBoxState.value = currentState.copy(
                cursorPosition = currentState.cursorPosition + 1
            )
        }
    }

    private fun handleNavigateUp() {
        // 根据当前焦点决定行为
        if (_detailPageState.value.focus) {
            // 详情页焦点：滚动内容
            handleScrollUp()
        } else if (_resultListState.value.focus) {
            // 列表页焦点：移动选中项
            handleMoveCursor(Direction.UP)
        }
    }

    private fun handleNavigateDown() {
        // 根据当前焦点决定行为
        if (_detailPageState.value.focus) {
            // 详情页焦点：滚动内容
            handleScrollDown()
        } else if (_resultListState.value.focus) {
            // 列表页焦点：移动选中项
            handleMoveCursor(Direction.DOWN)
        }
    }

    private fun handleMoveCursor(direction: Direction) {
        val currentState = _resultListState.value
        val searchResults = currentState.resultList
        if (searchResults.isEmpty()) return

        val newIndex = when (direction) {
            Direction.UP -> (currentState.selectedIndex - 1).coerceAtLeast(0)
            Direction.DOWN -> (currentState.selectedIndex + 1).coerceAtMost(searchResults.size - 1)
            else -> return // LEFT 和 RIGHT 暂不处理，可根据需要扩展
        }

        _resultListState.value = currentState.copy(selectedIndex = newIndex)
    }

    private fun handleConfirm() {
        val listState = _resultListState.value
        if (listState.resultList.isNotEmpty() && listState.selectedIndex < listState.resultList.size) {
            val selectedDoc = listState.resultList[listState.selectedIndex]
            // 先切换到详情页并显示加载状态
            handleSwitchDetailPage()
            // 然后异步加载文档详情
            loadDocumentDetail(selectedDoc)
        }
    }

    private fun loadDocumentDetail(docBrief: DocBrief) {
        viewModelScope.launch {
            // 切换到加载状态
            _uiState.value = _uiState.value.copy(status = DocliUiStatus.Loading)

            // 如果没有URL，使用模拟数据
            if (docBrief.url.isEmpty()) {
                val docDetail = DocDetail(
                    title = docBrief.title,
                    description = docBrief.description,
                    rawContent = "这是 ${docBrief.title} 的详细内容...\n\n${docBrief.description}\n\n更多详细信息待实现。",
                    mediaList = emptyList()
                )
                _detailPageState.value = _detailPageState.value.copy(
                    docDetail = docDetail,
                    focus = true,
                    scrollPercentage = 0f  // 【简化】：重置滚动百分比到顶部
                )
                _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
                return@launch
            }

            // 从android.exe获取文档详情
            docsService.getDocumentDetail(docBrief.url).onSuccess { androidDoc ->
                val docDetail = androidDoc.toDocDetail()
                _detailPageState.value = _detailPageState.value.copy(
                    docDetail = docDetail,
                    focus = true,
                    scrollPercentage = 0f  // 【简化】：重置滚动百分比到顶部
                )
                _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
            }.onFailure { error ->
                println("获取文档详情失败: ${error.message}")
                // 使用模拟数据作为降级方案
                val docDetail = DocDetail(
                    title = docBrief.title,
                    description = docBrief.description,
                    rawContent = "加载失败: ${error.message}\n\n${docBrief.description}",
                    mediaList = emptyList()
                )
                _detailPageState.value = _detailPageState.value.copy(
                    docDetail = docDetail,
                    focus = true,
                    scrollPercentage = 0f  // 【简化】：重置滚动百分比到顶部
                )
                _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
            }
        }
    }

    /**
     * 执行搜索
     */
    fun performSearch(query: String) {
        viewModelScope.launch {
            // 切换到加载状态
            _uiState.value = _uiState.value.copy(status = DocliUiStatus.Loading)

            docsService.searchDocuments(query).onSuccess { results ->
                // 转换为DocBrief并初始化
                val docBriefs: List<DocBrief> = results.map { it.toDocBrief() }
                handleInitialize(docBriefs)
            }.onFailure { error ->
                println("搜索失败: ${error.message}")
                // TODO: 显示错误信息
                _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
            }
        }
    }

    private fun handleExit() {
        _uiState.value = _uiState.value.copy(pendingExit = true)
    }

    private fun handleShowHelp() {
        _uiState.value = _uiState.value.copy(showingHelp = true)
    }

    private fun handleFocusSearchBox() {
        // 聚焦到搜索框，进入输入模式
        _searchBoxState.value = _searchBoxState.value.copy(
            focus = true,
            cursorPosition = _searchBoxState.value.searchQuery.length // 光标移到末尾
        )
        _resultListState.value = _resultListState.value.copy(focus = false)
        _detailPageState.value = _detailPageState.value.copy(focus = false)
        _uiState.value = _uiState.value.copy(status = DocliUiStatus.Input)
    }

    private fun handleSwitchSearchList() {
        // 切换到搜索列表视图
        _resultListState.value = _resultListState.value.copy(focus = true)
        _detailPageState.value = _detailPageState.value.copy(focus = false)
        _searchBoxState.value = _searchBoxState.value.copy(focus = false)
        _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
    }

    private fun handleScrollDown() {
        if (!_detailPageState.value.focus) return
        val currentState = _detailPageState.value

        // 【简化】：直接增加滚动百分比，组件内部会处理边界
        val newPercentage = (currentState.scrollPercentage + 0.05f).coerceAtMost(1.0f)
        _detailPageState.value = currentState.copy(scrollPercentage = newPercentage)
    }

    private fun handleScrollUp() {
        if (!_detailPageState.value.focus) return
        val currentState = _detailPageState.value

        // 【简化】：直接减少滚动百分比，组件内部会处理边界
        val newPercentage = (currentState.scrollPercentage - 0.05f).coerceAtLeast(0f)
        _detailPageState.value = currentState.copy(scrollPercentage = newPercentage)
    }

    private fun handleSwitchDetailPage() {
        // 切换到详情页视图
        _detailPageState.value = _detailPageState.value.copy(
            focus = true,
            scrollPercentage = 0f  // 【简化】：重置滚动百分比到顶部
        )
        _resultListState.value = _resultListState.value.copy(focus = false)
        _searchBoxState.value = _searchBoxState.value.copy(focus = false)
        _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
    }

    private fun handleFinishInput() {
        // 完成输入，执行搜索
        val query = _searchBoxState.value.searchQuery.trim()

        // 退出输入模式
        _searchBoxState.value = _searchBoxState.value.copy(
            focus = false,
            cursorPosition = 0 // 重置光标位置
        )
        _detailPageState.value = _detailPageState.value.copy(focus = false)

        // 调用真实的搜索服务
        if (query.isNotEmpty()) {
            performSearch(query)
        } else {
            // 如果搜索框为空，显示所有原始结果
            _resultListState.value = _resultListState.value.copy(
                resultList = originalSearchResults,
                selectedIndex = 0,
                focus = true
            )
            _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
        }
    }

    private fun handleExitInput() {
        // 退出输入模式，回到查看模式
        _searchBoxState.value = _searchBoxState.value.copy(
            focus = false,
            cursorPosition = 0 // 重置光标位置
        )
        _resultListState.value = _resultListState.value.copy(focus = true)
        _detailPageState.value = _detailPageState.value.copy(focus = false)
        _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
    }

    private fun handleFinishLoading() {
        // 结束加载状态
        _uiState.value = _uiState.value.copy(status = DocliUiStatus.View)
    }

    private fun handleClearPendingActions() {
        _uiState.value = _uiState.value.copy(
            status = DocliUiStatus.View,
            pendingExit = false,
            pendingNavigation = false,
            showingHelp = false
        )
        // 恢复焦点到结果列表
        _resultListState.value = _resultListState.value.copy(focus = true)
        _searchBoxState.value = _searchBoxState.value.copy(focus = false)
    }

    override fun onCleared() {
        // ViewModel 已清理，释放资源
    }
}


