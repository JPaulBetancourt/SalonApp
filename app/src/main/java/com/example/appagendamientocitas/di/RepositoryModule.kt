package com.example.appagendamientocitas.di

import com.example.appagendamientocitas.data.repository.FirebaseAuthRepository
import com.example.appagendamientocitas.data.repository.FirestoreAppointmentRepository
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
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(impl: FirestoreAppointmentRepository): AppointmentRepository
}