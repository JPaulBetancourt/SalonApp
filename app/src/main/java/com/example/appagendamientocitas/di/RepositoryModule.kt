package com.example.appagendamientocitas.di

import com.example.appagendamientocitas.data.repository.AppointmentRepositoryImpl
import com.example.appagendamientocitas.data.repository.AuthRepositoryImpl
import com.example.appagendamientocitas.domain.repository.AppointmentRepository
import com.example.appagendamientocitas.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: AppointmentRepositoryImpl): AppointmentRepository
}