package com.example.streamchat.util

sealed class CreateChannelEvent{
    object Success: CreateChannelEvent()
    data class Error(val error:String): CreateChannelEvent()
}
