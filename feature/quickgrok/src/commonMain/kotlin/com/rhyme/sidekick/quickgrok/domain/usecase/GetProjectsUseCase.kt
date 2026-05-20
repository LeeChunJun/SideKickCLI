package com.rhyme.sidekick.quickgrok.domain.usecase

import com.rhyme.sidekick.quickgrok.domain.model.ProjectInfo
import com.rhyme.sidekick.quickgrok.domain.repository.QuickGrokRepository

/**
 * 获取项目列表用例
 */
class GetProjectsUseCase(
    private val repository: QuickGrokRepository
) {
    suspend operator fun invoke(): Result<List<ProjectInfo>> {
        return repository.getProjects()
    }
}
