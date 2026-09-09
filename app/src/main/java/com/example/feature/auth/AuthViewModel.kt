package com.example.feature.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val user: AuthUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isSigningIn = MutableStateFlow(false)
    val isSigningIn: StateFlow<Boolean> = _isSigningIn.asStateFlow()

    init {
        checkInitialAuthState()
        observeAuthState()
    }

    private fun checkInitialAuthState() {
        val user = authRepository.currentUser
        if (user != null) {
            _uiState.value = AuthUiState.Authenticated(user)
        } else {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                if (user != null) {
                    _uiState.value = AuthUiState.Authenticated(user)
                } else if (_uiState.value !is AuthUiState.Loading) {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            }
        }
    }

    fun getGoogleSignInIntent(activity: Activity): Intent {
        return authRepository.getGoogleSignInClient(activity).signInIntent
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _isSigningIn.value = true
            val result = authRepository.signInWithGoogleIntent(data)
            _isSigningIn.value = false
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Authenticated(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(
                    error.localizedMessage ?: "Sign in failed. Please check network and Google account settings."
                )
            }
        }
    }

    fun dismissError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Unauthenticated
        }
    }

    fun signOut(activity: Activity) {
        viewModelScope.launch {
            authRepository.signOut(activity) {
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(AuthRepository(context.applicationContext)) as T
                }
            }
    }
}
