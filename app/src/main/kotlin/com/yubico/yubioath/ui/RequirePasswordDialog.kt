/*
 * Copyright (c) 2013, Yubico AB.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

package com.yubico.yubioath.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import com.yubico.yubioath.R
import com.yubico.yubioath.client.KeyManager
import kotlinx.android.synthetic.main.dialog_require_password.view.*

class RequirePasswordDialog : DialogFragment() {
    companion object {
        private const val DEVICE_ID = "deviceId"
        private const val DEVICE_SALT = "deviceSalt"
        private const val MISSING = "missing"

        internal fun newInstance(keyManager: KeyManager, deviceId: String, salt: ByteArray, missing: Boolean): RequirePasswordDialog {
            return RequirePasswordDialog().apply {
                arguments = Bundle().apply {
                    putString(DEVICE_ID, deviceId)
                    putByteArray(DEVICE_SALT, salt)
                    putBoolean(MISSING, missing)
                }
                setKeyManager(keyManager)
            }
        }
    }

    private lateinit var keyManager: KeyManager

    private fun setKeyManager(manager: KeyManager) {
        keyManager = manager
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return arguments!!.let {
            val deviceId = it.getString(DEVICE_ID)!!
            val salt = it.getByteArray(DEVICE_SALT)!!
            val missing = it.getBoolean(MISSING)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_require_password, null)
            AlertDialog.Builder(activity).apply {
                setView(view)
                setTitle(if (missing) R.string.password_required else R.string.password_wrong)
                setPositiveButton(R.string.ok, null) // To be able to cancel dismissing the dialog we use the onClick listener set in the onShowListener...
                setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            }.create().apply {
                setOnShowListener { getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val password = view.password.text.toString()
                    val remember = view.rememberPassword.isChecked
                    if (password.isNotEmpty()) {
                        keyManager.clearKeys(deviceId)
                        keyManager.addKey(deviceId, KeyManager.calculateSecret(password, salt, false), remember)
                        keyManager.addKey(deviceId, KeyManager.calculateSecret(password, salt, true), remember)
                        dismiss()
                    } else {
                        view.password_wrapper.error = getString(R.string.password_required)
                    }
                } }
            }
        }
    }
}
