package com.example.streamchat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streamchat.util.Constants
import com.example.streamchat.util.LoginEvent
import com.example.streamchat.util.UILoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: ChatClient
): ViewModel() {

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UILoadingState>()
    val loadingState = _loadingState


    private fun isValidUserName(username: String): Boolean{
        return username.length >= Constants.MIN_USERNAME_LENGTH
    }

    fun loginUser(username: String, token: String? = null){
        val username = username.trim()
        viewModelScope.launch {
            if(isValidUserName(username) && token!=null){
                loginRegisteredUser(username, token)
            } else if(isValidUserName(username) && token==null){
                loginGuestUser(username)
            } else{
                _loginEvent.emit(LoginEvent.ErrorInputTooShort)
            }
        }
    }

    private fun loginGuestUser(username: String) {
        _loadingState.value = UILoadingState.Loading
        client
            .connectGuestUser(userId = username, username = username)
            .enqueue{ result->
                _loadingState.value = UILoadingState.Loading
                if(result.isSuccess){
                viewModelScope.launch {
                    _loginEvent.emit(LoginEvent.Success)
                }
            } else {
                viewModelScope.launch {
                    _loginEvent.emit(LoginEvent.ErrorLogIn(
                        result.error().message ?: "Unknown Error"
                    ))
                }
            }
        }
    }

    private fun loginRegisteredUser(username: String, token: String) {
        val user = User(id = username, name = username)
        _loadingState.value = UILoadingState.Loading
        client
            .connectUser(user=user, token=token)
            .enqueue{ result->
                _loadingState.value = UILoadingState.NotLoading
                if(result.isSuccess){
                    viewModelScope.launch {
                        _loginEvent.emit(LoginEvent.Success)
                    }
                } else {
                    viewModelScope.launch {
                        _loginEvent.emit(LoginEvent.ErrorLogIn(
                            result.error().message ?: "Unknown Error"
                        ))
                    }
                }
            }
    }

}