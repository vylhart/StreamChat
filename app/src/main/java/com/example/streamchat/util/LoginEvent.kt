package com.example.streamchat.util

sealed class LoginEvent{
    object ErrorInputTooShort : LoginEvent()
    data class ErrorLogIn(val error: String): LoginEvent()
    object Success: LoginEvent()
}
