package org.thoughtcrime.securesms.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import com.goterl.lazysodium.utils.KeyPair
import network.qki.messenger.R
import network.qki.messenger.databinding.ActivityRegisterBinding
import org.session.libsession.snode.SnodeModule
import org.session.libsession.utilities.TextSecurePreferences
import org.session.libsignal.crypto.ecc.ECKeyPair
import org.session.libsignal.database.LokiAPIDatabaseProtocol
import org.session.libsignal.utilities.Hex
import org.session.libsignal.utilities.KeyHelper
import org.session.libsignal.utilities.hexEncodedPublicKey
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.BaseActionBarActivity
import org.thoughtcrime.securesms.crypto.KeyPairUtilities
import org.thoughtcrime.securesms.home.HomeActivity
import org.thoughtcrime.securesms.util.push
import org.thoughtcrime.securesms.wallet.WalletViewModel

class RegisterActivity : BaseActionBarActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val walletViewModel by viewModels<WalletViewModel>()

    internal val database: LokiAPIDatabaseProtocol
        get() = SnodeModule.shared.storage
    private lateinit var seed: ByteArray
    private var ed25519KeyPair: KeyPair? = null
    private var x25519KeyPair: ECKeyPair? = null
        set(value) { field = value; updatePublicKeyTextView() }

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TextSecurePreferences.apply {
            setHasViewedSeed(this@RegisterActivity, false)
            setConfigurationMessageSynced(this@RegisterActivity, true)
            setRestorationTime(this@RegisterActivity, 0)
            setLastProfileUpdateTime(this@RegisterActivity, System.currentTimeMillis())
        }
        binding.registerButton.setOnClickListener { register() }
        binding.copyButton.setOnClickListener { copyPublicKey() }
        updateKeyPair()
        walletViewModel.initWalletLiveData.observe(this) {
            if (it == true) {
                // This is here to resolve a case where the app restarts before a user completes onboarding
                // which can result in an invalid database state
                database.clearAllLastMessageHashes()
                database.clearReceivedMessageHashValues()
                KeyPairUtilities.store(this, seed!!, ed25519KeyPair!!, x25519KeyPair!!)

                val userHexEncodedPublicKey = x25519KeyPair!!.hexEncodedPublicKey
                val registrationID = KeyHelper.generateRegistrationId(false)
                TextSecurePreferences.setLocalRegistrationId(this, registrationID)
                TextSecurePreferences.setLocalNumber(this, userHexEncodedPublicKey)
                TextSecurePreferences.setRestorationTime(this, 0)
                TextSecurePreferences.setHasViewedSeed(this, false)
                TextSecurePreferences.setProfileName(this, "")

                TextSecurePreferences.setHasSeenWelcomeScreen(this, true)
                TextSecurePreferences.setIsUsingFCM(this, true)
                val application = ApplicationContext.getInstance(this)
                application.startPollingIfNeeded()
                application.registerForFCMIfNeeded(true)
                val intent = Intent(this, HomeActivity::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                push(intent)
                finish()
            }
        }
    }
    // endregion

    // region Updating
    private fun updateKeyPair() {
        val keyPairGenerationResult = KeyPairUtilities.generate()
        seed = keyPairGenerationResult.seed
        ed25519KeyPair = keyPairGenerationResult.ed25519KeyPair
        x25519KeyPair = keyPairGenerationResult.x25519KeyPair
    }

    private fun updatePublicKeyTextView() {
        val hexEncodedPublicKey = x25519KeyPair!!.hexEncodedPublicKey
        val characterCount = hexEncodedPublicKey.count()
        var count = 0
        val limit = 32
        fun animate() {
            val numberOfIndexesToShuffle = 32 - count
            val indexesToShuffle = (0 until characterCount).shuffled().subList(0, numberOfIndexesToShuffle)
            var mangledHexEncodedPublicKey = hexEncodedPublicKey
            for (index in indexesToShuffle) {
                try {
                    mangledHexEncodedPublicKey = mangledHexEncodedPublicKey.substring(0, index) + "0123456789abcdef__".random() + mangledHexEncodedPublicKey.substring(index + 1, mangledHexEncodedPublicKey.count())
                } catch (exception: Exception) {
                    // Do nothing
                }
            }
            count += 1
            if (count < limit) {
                binding.publicKeyTextView.text = mangledHexEncodedPublicKey
                Handler().postDelayed({
                    animate()
                }, 32)
            } else {
                binding.publicKeyTextView.text = hexEncodedPublicKey
            }
        }
        animate()
    }
    // endregion

    // region Interaction
    private fun register() {
        walletViewModel.initWallet(Hex.toStringCondensed(seed))
    }

    private fun copyPublicKey() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Session ID", x25519KeyPair!!.hexEncodedPublicKey)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    private fun openURL(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show()
        }
    }
    // endregion
}