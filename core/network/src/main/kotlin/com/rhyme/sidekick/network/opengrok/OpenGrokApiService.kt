package com.rhyme.sidekick.network.opengrok

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * OpenGrok API接口
 */
interface OpenGrokApi {
    // ==================== 文件相关接口 ====================
    
    /**
     * 获取文件注释
     */
    @GET("annotation")
    fun getFileAnnotation(@Query("path") path: String): Call<List<AnnotationResponse>>

    /**
     * 获取文件内容
     */
    @GET("file/content")
    fun getFileContent(@Query("path") path: String): Call<ResponseBody>

    /**
     * 获取文件类型
     */
    @GET("file/genre")
    fun getFileGenre(@Query("path") path: String): Call<String>

    /**
     * 获取文件定义
     */
    @GET("file/defs")
    fun getFileDefinitions(@Query("path") path: String): Call<List<FileDefsResponse>>

    /**
     * 获取历史记录
     */
    @GET("history")
    fun getFileHistory(
        @Query("path") path: String,
        @Query("withFiles") withFiles: Boolean? = null,
        @Query("start") start: Int? = null,
        @Query("max") max: Int? = null
    ): Call<HistoryResponse>

    /**
     * 获取目录列表
     */
    @GET("list")
    fun getDirectoryList(@Query("path") path: String): Call<List<ListResponse>>

    // ==================== 配置管理接口 ====================
    
    /**
     * 获取配置（XML格式）
     */
    @GET("configuration")
    fun getConfiguration(): Call<ResponseBody>

    /**
     * 设置配置（XML格式，异步）
     */
    @PUT("configuration")
    fun setConfiguration(@Body xmlConfig: RequestBody): Call<Void>

    /**
     * 获取配置字段
     */
    @GET("configuration/{field}")
    fun getConfigurationField(@Path("field") field: String): Call<ResponseBody>

    /**
     * 设置配置字段（异步）
     */
    @PUT("configuration/{field}")
    fun setConfigurationField(
        @Path("field") field: String,
        @Query("reindex") reindex: Boolean? = null,
        @Body value: RequestBody
    ): Call<Void>

    /**
     * 重载授权框架（异步）
     */
    @POST("configuration/authorization/reload")
    fun reloadAuthorization(): Call<Void>

    // ==================== 项目管理接口 ====================
    
    /**
     * 获取所有项目
     */
    @GET("projects")
    fun getProjects(): Call<List<String>>

    /**
     * 添加项目
     */
    @POST("projects")
    fun addProject(@Body projectName: RequestBody): Call<Void>

    /**
     * 删除项目
     */
    @DELETE("projects/{project}")
    fun deleteProject(@Path("project") project: String): Call<Void>

    /**
     * 删除项目索引数据
     */
    @DELETE("projects/{project}/data")
    fun deleteProjectData(@Path("project") project: String): Call<Void>

    /**
     * 删除历史缓存
     */
    @DELETE("projects/{project}/historycache")
    fun deleteHistoryCache(@Path("project") project: String): Call<List<String>>

    /**
     * 删除注释缓存
     */
    @DELETE("projects/{project}/annotationcache")
    fun deleteAnnotationCache(@Path("project") project: String): Call<List<String>>

    /**
     * 标记项目为已索引（异步）
     */
    @PUT("projects/{project}/indexed")
    fun markProjectIndexed(@Path("project") project: String): Call<Void>

    /**
     * 获取项目文件列表
     */
    @GET("projects/{project}/files")
    fun getProjectFiles(@Path("project") project: String): Call<List<String>>

    /**
     * 设置项目属性
     */
    @PUT("projects/{project}/property/{propertyName}")
    fun setProjectProperty(
        @Path("project") project: String,
        @Path("propertyName") propertyName: String,
        @Body value: RequestBody
    ): Call<Void>

    /**
     * 获取项目属性
     */
    @GET("projects/{project}/property/{propertyName}")
    fun getProjectProperty(
        @Path("project") project: String,
        @Path("propertyName") propertyName: String
    ): Call<ResponseBody>

    /**
     * 获取项目仓库列表
     */
    @GET("projects/{project}/repositories")
    fun getProjectRepositories(@Path("project") project: String): Call<List<String>>

    /**
     * 获取项目仓库类型
     */
    @GET("projects/{project}/repositories/type")
    fun getProjectRepositoryTypes(@Path("project") project: String): Call<List<String>>

    /**
     * 获取已索引项目列表
     */
    @GET("projects/indexed")
    fun getIndexedProjects(): Call<List<String>>

    // ==================== 组管理接口 ====================
    
    /**
     * 获取所有组
     */
    @GET("groups")
    fun getGroups(): Call<List<String>>

    /**
     * 获取组的所有项目
     */
    @GET("groups/{group}/allprojects")
    fun getGroupProjects(@Path("group") group: String): Call<List<String>>

    /**
     * 获取组模式
     */
    @GET("groups/{group}/pattern")
    fun getGroupPattern(@Path("group") group: String): Call<String>

    /**
     * 检查组匹配
     */
    @POST("groups/{group}/match")
    fun checkGroupMatch(
        @Path("group") group: String,
        @Body projectName: RequestBody
    ): Call<Void>

    // ==================== 搜索与建议接口 ====================
    
    /**
     * 执行搜索
     */
    @GET("search")
    fun executeSearch(
        @Query("full") full: String? = null,
        @Query("def") def: String? = null,
        @Query("symbol") symbol: String? = null,
        @Query("path") path: String? = null,
        @Query("hist") hist: String? = null,
        @Query("type") type: String? = null,
        @Query("projects") projects: String? = null,
        @Query("maxresults") maxresults: Int? = null,
        @Query("start") start: Int? = null,
        @Query("sort") sort: String? = null
    ): Call<SearchResponse>

    /**
     * 获取建议
     */
    @GET("suggest")
    fun getSuggestions(
        @Query("projects") projects: String,
        @Query("field") field: String,
        @Query("caret") caret: String,
        @Query("full") full: String? = null,
        @Query("defs") defs: String? = null,
        @Query("refs") refs: String? = null,
        @Query("path") path: String? = null,
        @Query("hist") hist: String? = null,
        @Query("type") type: String? = null
    ): Call<SuggestResponse>

    /**
     * 获取建议器配置
     */
    @GET("suggest/config")
    fun getSuggestConfig(): Call<SuggestConfig>

    /**
     * 基于查询初始化流行度数据
     */
    @POST("suggest/init/queries")
    fun initPopularityFromQueries(@Body queries: List<String>): Call<Void>

    /**
     * 基于原始数据初始化流行度
     */
    @POST("suggest/init/raw")
    fun initPopularityFromRaw(@Body data: List<PopularityDataItem>): Call<Void>

    /**
     * 获取流行度数据
     */
    @GET("suggest/popularity/{project}")
    fun getPopularityData(
        @Path("project") project: String,
        @Query("field") field: String? = null,
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("all") all: Boolean? = null
    ): Call<List<Map<String, Int>>>

    /**
     * 重建所有项目的建议器数据（异步）
     */
    @PUT("suggest/rebuild")
    fun rebuildAllSuggestData(): Call<Void>

    /**
     * 重建指定项目的建议器数据（异步）
     */
    @PUT("suggest/rebuild/{project}")
    fun rebuildProjectSuggestData(@Path("project") project: String): Call<Void>

    // ==================== 系统状态与监控接口 ====================
    
    /**
     * Ping检查
     */
    @GET("system/ping")
    fun ping(): Call<Void>

    /**
     * 获取版本信息
     */
    @GET("system/version")
    fun getVersion(): Call<String>

    /**
     * 获取最后索引时间
     */
    @GET("system/indextime")
    fun getIndexTime(): Call<String>

    /**
     * 重载包含文件
     */
    @PUT("system/includes/reload")
    fun reloadIncludes(): Call<Void>

    /**
     * 更新路径描述
     */
    @POST("system/pathdesc")
    fun updatePathDescriptions(@Body descriptions: List<PathDescription>): Call<Void>

    // ==================== 消息管理接口 ====================
    
    /**
     * 添加消息
     */
    @POST("messages")
    fun addMessage(@Body message: MessageRequest): Call<Void>

    /**
     * 删除消息
     */
    @DELETE("messages")
    fun deleteMessages(
        @Query("tag") tag: String,
        @Body messageText: RequestBody
    ): Call<Void>

    /**
     * 获取消息
     */
    @GET("messages")
    fun getMessages(@Query("tag") tag: String): Call<List<MessageResponse>>

    // ==================== 仓库管理接口 ====================
    
    /**
     * 获取仓库字段值
     */
    @GET("repositories/property/{field}")
    fun getRepositoryProperty(
        @Path("field") field: String,
        @Query("repository") repository: String? = null
    ): Call<ResponseBody>

    // ==================== 异步任务状态接口 ====================
    
    /**
     * 检查任务状态
     */
    @GET("status/{uuid}")
    fun getTaskStatus(@Path("uuid") uuid: String): Call<Void>

    /**
     * 删除任务状态
     */
    @DELETE("status/{uuid}")
    fun deleteTaskStatus(@Path("uuid") uuid: String): Call<Void>
}

/**
 * OpenGrok API服务
 */
class OpenGrokApiService {

    private val api: OpenGrokApi
    
    companion object {
        const val DEFAULT_BASE_URL = "http://localhost:8080/source/api/v1/"
        const val METRICS_BASE_URL = "http://localhost:8080/"
    }

    constructor(baseUrl: String = DEFAULT_BASE_URL) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(OpenGrokApi::class.java)
    }
    
    /**
     * 获取API实例
     */
    fun getApi(): OpenGrokApi = api
    
    // ==================== 文件相关接口 ====================
    
    fun getFileAnnotation(path: String): Call<List<AnnotationResponse>> {
        return api.getFileAnnotation(path)
    }
    
    fun getFileContent(path: String): Call<ResponseBody> {
        return api.getFileContent(path)
    }
    
    fun getFileGenre(path: String): Call<String> {
        return api.getFileGenre(path)
    }
    
    fun getFileDefinitions(path: String): Call<List<FileDefsResponse>> {
        return api.getFileDefinitions(path)
    }
    
    fun getFileHistory(
        path: String,
        withFiles: Boolean? = null,
        start: Int? = null,
        max: Int? = null
    ): Call<HistoryResponse> {
        return api.getFileHistory(path, withFiles, start, max)
    }
    
    fun getDirectoryList(path: String): Call<List<ListResponse>> {
        return api.getDirectoryList(path)
    }
    
    // ==================== 配置管理接口 ====================
    
    fun getConfiguration(): Call<ResponseBody> {
        return api.getConfiguration()
    }
    
    fun setConfiguration(xmlConfig: RequestBody): Call<Void> {
        return api.setConfiguration(xmlConfig)
    }
    
    fun getConfigurationField(field: String): Call<ResponseBody> {
        return api.getConfigurationField(field)
    }
    
    fun setConfigurationField(
        field: String,
        reindex: Boolean? = null,
        value: RequestBody
    ): Call<Void> {
        return api.setConfigurationField(field, reindex, value)
    }
    
    fun reloadAuthorization(): Call<Void> {
        return api.reloadAuthorization()
    }
    
    // ==================== 项目管理接口 ====================
    
    fun getProjects(): Call<List<String>> {
        return api.getProjects()
    }
    
    /**
     * 获取所有项目（同步方法，直接返回结果）
     */
    fun getProjectsSync(): List<String> {
        return try {
            val response = api.getProjects().execute()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                println("Error getting projects: HTTP ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("Error getting projects: ${e.message}")
            emptyList()
        }
    }
    
    fun addProject(projectName: RequestBody): Call<Void> {
        return api.addProject(projectName)
    }
    
    fun deleteProject(project: String): Call<Void> {
        return api.deleteProject(project)
    }
    
    fun deleteProjectData(project: String): Call<Void> {
        return api.deleteProjectData(project)
    }
    
    fun deleteHistoryCache(project: String): Call<List<String>> {
        return api.deleteHistoryCache(project)
    }
    
    fun deleteAnnotationCache(project: String): Call<List<String>> {
        return api.deleteAnnotationCache(project)
    }
    
    fun markProjectIndexed(project: String): Call<Void> {
        return api.markProjectIndexed(project)
    }
    
    fun getProjectFiles(project: String): Call<List<String>> {
        return api.getProjectFiles(project)
    }
    
    fun setProjectProperty(
        project: String,
        propertyName: String,
        value: RequestBody
    ): Call<Void> {
        return api.setProjectProperty(project, propertyName, value)
    }
    
    fun getProjectProperty(project: String, propertyName: String): Call<ResponseBody> {
        return api.getProjectProperty(project, propertyName)
    }
    
    fun getProjectRepositories(project: String): Call<List<String>> {
        return api.getProjectRepositories(project)
    }
    
    fun getProjectRepositoryTypes(project: String): Call<List<String>> {
        return api.getProjectRepositoryTypes(project)
    }
    
    fun getIndexedProjects(): Call<List<String>> {
        return api.getIndexedProjects()
    }
    
    // ==================== 组管理接口 ====================
    
    fun getGroups(): Call<List<String>> {
        return api.getGroups()
    }
    
    fun getGroupProjects(group: String): Call<List<String>> {
        return api.getGroupProjects(group)
    }
    
    fun getGroupPattern(group: String): Call<String> {
        return api.getGroupPattern(group)
    }
    
    fun checkGroupMatch(group: String, projectName: RequestBody): Call<Void> {
        return api.checkGroupMatch(group, projectName)
    }
    
    // ==================== 搜索与建议接口 ====================
    
    fun executeSearch(
        full: String? = null,
        def: String? = null,
        symbol: String? = null,
        path: String? = null,
        hist: String? = null,
        type: String? = null,
        projects: String? = null,
        maxresults: Int? = null,
        start: Int? = null,
        sort: String? = null
    ): Call<SearchResponse> {
        return api.executeSearch(full, def, symbol, path, hist, type, projects, maxresults, start, sort)
    }
    
    /**
     * 执行搜索（同步方法，直接返回结果）
     */
    fun executeSearchSync(
        full: String? = null,
        def: String? = null,
        symbol: String? = null,
        path: String? = null,
        hist: String? = null,
        type: String? = null,
        projects: String? = null,
        maxresults: Int? = null,
        start: Int? = null,
        sort: String? = null
    ): SearchResponse? {
        return try {
            val response = api.executeSearch(full, def, symbol, path, hist, type, projects, maxresults, start, sort).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                println("Error executing search: HTTP ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("Error executing search: ${e.message}")
            null
        }
    }
    
    fun getSuggestions(
        projects: String,
        field: String,
        caret: String,
        full: String? = null,
        defs: String? = null,
        refs: String? = null,
        path: String? = null,
        hist: String? = null,
        type: String? = null
    ): Call<SuggestResponse> {
        return api.getSuggestions(projects, field, caret, full, defs, refs, path, hist, type)
    }
    
    fun getSuggestConfig(): Call<SuggestConfig> {
        return api.getSuggestConfig()
    }
    
    fun initPopularityFromQueries(queries: List<String>): Call<Void> {
        return api.initPopularityFromQueries(queries)
    }
    
    fun initPopularityFromRaw(data: List<PopularityDataItem>): Call<Void> {
        return api.initPopularityFromRaw(data)
    }
    
    fun getPopularityData(
        project: String,
        field: String? = null,
        page: Int? = null,
        pageSize: Int? = null,
        all: Boolean? = null
    ): Call<List<Map<String, Int>>> {
        return api.getPopularityData(project, field, page, pageSize, all)
    }
    
    fun rebuildAllSuggestData(): Call<Void> {
        return api.rebuildAllSuggestData()
    }
    
    fun rebuildProjectSuggestData(project: String): Call<Void> {
        return api.rebuildProjectSuggestData(project)
    }
    
    // ==================== 系统状态与监控接口 ====================
    
    fun ping(): Call<Void> {
        return api.ping()
    }
    
    /**
     * Ping检查（同步方法，直接返回结果）
     */
    fun pingSync(): Boolean {
        return try {
            val response = api.ping().execute()
            response.isSuccessful
        } catch (e: Exception) {
            println("Error pinging: ${e.message}")
            false
        }
    }
    
    fun getVersion(): Call<String> {
        return api.getVersion()
    }
    
    fun getIndexTime(): Call<String> {
        return api.getIndexTime()
    }
    
    fun reloadIncludes(): Call<Void> {
        return api.reloadIncludes()
    }
    
    fun updatePathDescriptions(descriptions: List<PathDescription>): Call<Void> {
        return api.updatePathDescriptions(descriptions)
    }
    
    // ==================== 消息管理接口 ====================
    
    fun addMessage(message: MessageRequest): Call<Void> {
        return api.addMessage(message)
    }
    
    fun deleteMessages(tag: String, messageText: RequestBody): Call<Void> {
        return api.deleteMessages(tag, messageText)
    }
    
    fun getMessages(tag: String): Call<List<MessageResponse>> {
        return api.getMessages(tag)
    }
    
    // ==================== 仓库管理接口 ====================
    
    fun getRepositoryProperty(field: String, repository: String? = null): Call<ResponseBody> {
        return api.getRepositoryProperty(field, repository)
    }
    
    // ==================== 异步任务状态接口 ====================
    
    fun getTaskStatus(uuid: String): Call<Void> {
        return api.getTaskStatus(uuid)
    }
    
    fun deleteTaskStatus(uuid: String): Call<Void> {
        return api.deleteTaskStatus(uuid)
    }
}