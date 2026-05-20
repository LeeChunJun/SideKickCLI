package com.rhyme.sidekick.quickgrok.presentation

import com.rhyme.sidekick.quickgrok.data.source.local.LocalQuickGrokDataSource
import com.rhyme.sidekick.quickgrok.data.source.remote.RemoteQuickGrokDataSource
import com.rhyme.sidekick.quickgrok.domain.repository.QuickGrokRepository
import com.rhyme.sidekick.quickgrok.domain.usecase.GetProjectsUseCase
import com.rhyme.sidekick.quickgrok.domain.usecase.SearchCodeUseCase

/**
 * QuickGrok ViewModel的具体实现（JVM平台）
 */
class QuickGrokViewModel : BaseQuickGrokViewModel(
    getProjectsUseCase = GetProjectsUseCase(
        QuickGrokRepository(
            LocalQuickGrokDataSource(),
            RemoteQuickGrokDataSource()
        )
    ),
    searchCodeUseCase = SearchCodeUseCase(
        QuickGrokRepository(
            LocalQuickGrokDataSource(),
            RemoteQuickGrokDataSource()
        )
    )
)