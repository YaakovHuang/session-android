package org.thoughtcrime.securesms.wallet.qrcode

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.zxing.Result
import com.king.zxing.CameraScan
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityQrcodeCaptureBinding
import org.thoughtcrime.securesms.PassphraseRequiredActionBarActivity
import org.thoughtcrime.securesms.util.QRCodeUtils
import org.thoughtcrime.securesms.util.readBytes

class QrCodeActivity : PassphraseRequiredActionBarActivity(), CameraScan.OnScanResultCallback {


    private lateinit var binding: ActivityQrcodeCaptureBinding

    private val selectQrImage = registerForActivityResult(SelectImageContract()) {
        it?.uri?.readBytes(this)?.let { bytes ->
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            onScanResultCallback(QRCodeUtils.parseCodeResult(bitmap))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityQrcodeCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar ?: return
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)

        val fTag = "qrCodeFragment"
        val qrCodeFragment = QrCodeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_content, qrCodeFragment, fTag)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_qr_code_scan, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_choose_from_gallery -> selectQrImage.launch()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onScanResultCallback(result: Result?): Boolean {
        val intent = Intent()
        intent.putExtra("result", result?.text)
        setResult(RESULT_OK, intent)
        finish()
        return true
    }

}