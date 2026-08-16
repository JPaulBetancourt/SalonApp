package com.example.appagendamientocitas.domain.usecase

import com.example.appagendamientocitas.data.local.entity.User
import com.example.appagendamientocitas.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        authRepository.login(email, password)
}

class RegisterClientUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<User> =
        authRepository.registerClient(name, email, password)
}