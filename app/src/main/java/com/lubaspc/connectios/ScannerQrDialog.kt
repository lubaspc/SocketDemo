package com.lubaspc.connectios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.lubaspc.connectios.databinding.DialogScanQrBinding

class ScannerQrDialog : DialogFragment() {
    private var onCb: ((String) -> Unit)? = null
    private lateinit var codeScanner: CodeScanner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogScanQrBinding.inflate(inflater, container, false).apply {
        codeScanner = CodeScanner(requireActivity(), csv)
        csv.setOnClickListener {
            codeScanner.startPreview()
        }
        codeScanner.startPreview()
        codeScanner.decodeCallback = DecodeCallback {
            onCb?.invoke(it.text)
            dismiss()
        }
    }.root


    fun setOnText(onCb: (String) -> Unit): ScannerQrDialog {
        this.onCb = onCb
        return this
    }

    fun show(supportFragmentManager: FragmentManager) {
        super.show(supportFragmentManager, this::class.java.simpleName)
    }
}