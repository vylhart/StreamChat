package com.example.streamchat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.streamchat.ui.theme.StreamChatTheme
import com.example.streamchat.util.CreateChannelEvent
import com.example.streamchat.viewmodel.ChannelListViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class ChannelListActivity : ComponentActivity() {
    private val TAG = "ChannelListActivity"
    val viewModel: ChannelListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {

                var showDialog:Boolean by remember {
                    mutableStateOf(false)
                }

                if(showDialog){
                    CreateChannelDialog(
                        dismiss = { channelName ->
                            viewModel.createChannel(channelName)
                            showDialog = false
                        }
                    )
                }

                ChannelsScreen(
                    filters = Filters.`in`(
                        fieldName = "type",
                        values = listOf("messaging")
                    ),
                    title = "Channel List",
                    isShowingSearch = true,
                    onItemClick = {
                        startActivity(MessageActivity.getIntent(this, channelID = it.cid))
                    },
                    onBackPressed = { finish() },
                    onHeaderActionClick = {
                        Log.i(TAG, "onCreate: clicked")
                        showDialog = true
                    }
                )
            }
        }
    }


    @Composable
    private fun CreateChannelDialog(dismiss: (String)-> Unit){
        Log.i(TAG, "CreateChannelDialog: ")
        var channelName by remember{ mutableStateOf("")}
        AlertDialog(
            onDismissRequest = { dismiss(channelName) },
            title = { Text(text = "Enter channel Name") },
            text = { TextField(value = channelName, onValueChange = { channelName = it }) },
            buttons = {
                Row(modifier = Modifier.padding(all = 8.dp),
                    horizontalArrangement =  Arrangement.Center
                    ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { dismiss(channelName) }
                    ) {
                        Text(text = "Create Channel")
                    }
                }
            }
        )
    }

    private fun subscribeToEvents(){
        lifecycleScope.launchWhenStarted {
            viewModel.createChannelEvent.collect{   event->
                when(event){
                    is CreateChannelEvent.Error->{
                        showToast(event.error)
                    }
                    is CreateChannelEvent.Success ->{
                        showToast("Channel Created")
                    }
                }

            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
