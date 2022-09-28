package com.example.streamchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.streamchat.util.CreateChannelEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChannelListViewModel @Inject constructor(private val client: ChatClient): ViewModel() {

    private val _createChannelEvent = MutableSharedFlow<CreateChannelEvent>()
    val createChannelEvent = _createChannelEvent.asSharedFlow()



    fun createChannel(channelName: String, channelType: String = "messaging"){
        val trimmedChannelName = channelName.trim()
        val channelId = UUID.randomUUID().toString()

        viewModelScope.launch {
            if(trimmedChannelName.isEmpty()){
                _createChannelEvent.emit(
                    CreateChannelEvent.Error("The channel name can't be empty")
                )
                return@launch
            }

            client.createChannel(
                channelType = channelType,
                channelId = channelId,
                memberIds = emptyList(),
                extraData =  mapOf(
                    "name" to trimmedChannelName
                )
            ).enqueue { result->
                if(result.isSuccess){
                    viewModelScope.launch {
                        _createChannelEvent.emit(CreateChannelEvent.Success)
                    }
                } else{
                    viewModelScope.launch {
                        _createChannelEvent.emit(CreateChannelEvent.Error(result.error().message?: "Unknown Error"))
                    }
                }
            }
        }
    }


}