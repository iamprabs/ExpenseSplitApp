package com.example.expensesplit.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensesplit.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName = _fullName.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode = _isLoginMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult: StateFlow<Boolean?> = _loginResult

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onFullNameChange(newName: String) {
        _fullName.value = newName
    }

    fun toggleMode() {
        _isLoginMode.value = !_isLoginMode.value
    }

    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            val success = authRepository.signIn(_email.value, _password.value)
            _loginResult.value = success
            _isLoading.value = false
        }
    }

    fun signUp() {
        viewModelScope.launch {
            _isLoading.value = true
            val success = authRepository.signUp(_email.value, _password.value, _fullName.value)
            _loginResult.value = success
            _isLoading.value = false
        }
    }
}
