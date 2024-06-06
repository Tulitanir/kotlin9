package com.example.project.dialogs

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.project.R
import com.example.project.util.PasswordChangeListener

class PasswordChangeDialogFragment : DialogFragment() {
    private var listener: PasswordChangeListener? = null
    fun setPasswordChangeListener(listener: PasswordChangeListener) {
        Log.d("adf", "New LISTNER")
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_password_change, null)
            val oldPasswordEditText = dialogView.findViewById<EditText>(R.id.oldPasswordEditText)
            val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
            val repeatPasswordEditText =
                dialogView.findViewById<EditText>(R.id.repeatPasswordEditText)

            builder.setView(dialogView)
                .setTitle("Сменить пароль")
                .setPositiveButton("Принять") { dialog, _ ->
                    val oldPassword = oldPasswordEditText.text.toString()
                    val newPassword = newPasswordEditText.text.toString()
                    val repeatPassword = repeatPasswordEditText.text.toString()

                    if (oldPassword.isEmpty() || newPassword.isEmpty() || repeatPassword.isEmpty()) {
                        Toast.makeText(
                            context,
                            "Все поля должны быть заполненеы",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton
                    }

                    if (newPassword != repeatPassword) {
                        Toast.makeText(context, "Пароли должны совпадать", Toast.LENGTH_SHORT)
                            .show()
                        return@setPositiveButton
                    }
                    listener!!.onChangePassword(oldPassword, newPassword)

                    dialog.dismiss()
                }
                .setNegativeButton("Закрыть") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}