package com.example.project.util

interface PasswordChangeListener {
    fun onChangePassword(oldPassword: String, newPassword: String)
}