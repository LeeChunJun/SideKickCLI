package com.rhyme.sidekick.cmdx.viewmodel

import com.rhyme.sidekick.cmdx.viewmodel.core.ViewModel
import com.rhyme.sidekick.cmdx.viewmodel.common.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _state = MutableStateFlow("Loading...")
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)
            _state.value = "Hello from JVM ViewModel!"
        }
    }

    override fun onCleared() {
        println("UserViewModel cleared")
    }
}