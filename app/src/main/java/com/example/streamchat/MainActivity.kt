package com.example.streamchat
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope

import com.example.streamchat.R
import com.example.streamchat.ui.theme.StreamChatTheme
import com.example.streamchat.util.LoginEvent
import com.example.streamchat.util.UILoadingState
import com.example.streamchat.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeToEvents()
        setContent {
            StreamChatTheme {
                LoginScreen()
            }
        }
    }

    private fun subscribeToEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginEvent.collect{   event->
                when(event){
                    is LoginEvent.ErrorInputTooShort -> {
                        showToast("Invalid! Enter more than 3 characters")
                    }
                    is LoginEvent.ErrorLogIn -> {
                        showToast("Error: ${event.error}")
                    }
                    is LoginEvent.Success -> {
                        showToast("Login Successful")
                        startActivity(Intent(this@MainActivity, ChannelListActivity::class.java))
                    }
                }
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun LoginScreen(){
        var username by remember { mutableStateOf(TextFieldValue(""))}
        var showProgress by remember { mutableStateOf(false) }
        //val (logo, usernameTextField, btnUserLogin, btnGuestLogin, progressBar) = createRefs()

        viewModel.loadingState.observe(this, Observer { state->
            showProgress = when(state){
                is UILoadingState.Loading ->{
                    true
                }
                is UILoadingState.NotLoading ->{
                    false
                }
            }
        })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center


        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "logo",
                modifier = Modifier
                    .height(50.dp)
                    .width(50.dp)

            )
            Spacer(modifier = Modifier.padding(10.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { newValue -> username = newValue },
                label = {
                    Text(
                        text = "Enter UserName",
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )



            Button(
                onClick = {
                    viewModel.loginUser(username.text, getString( R.string.jwt_token))
                },
                modifier = Modifier.fillMaxWidth()
            ){
                Text(text = "Login as User")
            }



            Button(
                onClick = {
                    viewModel.loginUser(username.text)
                },
                modifier = Modifier.fillMaxWidth()
            ){
                Text(text = "Login as Guest")
            }

            if(showProgress){
                CircularProgressIndicator()
            }
        }
    }




    @Composable
    @Preview(showBackground = true)
    fun PreviewScreen(){
        LoginScreen()
    }
}
