package com.example.streamchat.util

sealed class UILoadingState{
    object Loading: UILoadingState()
    object NotLoading: UILoadingState()
}
