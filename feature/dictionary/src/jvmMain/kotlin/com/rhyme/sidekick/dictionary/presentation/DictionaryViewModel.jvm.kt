package com.rhyme.sidekick.dictionary.presentation

import com.rhyme.sidekick.dictionary.data.source.local.LocalWordDataSource
import com.rhyme.sidekick.dictionary.data.source.remote.RemoteWordDetailDataSource
import com.rhyme.sidekick.dictionary.domain.repository.WordRepository
import com.rhyme.sidekick.dictionary.domain.usecase.GetWordDetailUseCase
import com.rhyme.sidekick.dictionary.domain.usecase.SearchWordsUseCase

/**
 * 词典ViewModel的具体实现（JVM平台）
 */
class DictionaryViewModel : BaseDictionaryViewModel(
    searchWordsUseCase = SearchWordsUseCase(
        WordRepository(
            LocalWordDataSource(),
            RemoteWordDetailDataSource()
        )
    ),
    getWordDetailUseCase = GetWordDetailUseCase(
        WordRepository(
            LocalWordDataSource(),
            RemoteWordDetailDataSource()
        )
    )
)
