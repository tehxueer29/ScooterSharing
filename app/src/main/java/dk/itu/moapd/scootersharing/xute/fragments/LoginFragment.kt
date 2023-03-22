package dk.itu.moapd.scootersharing.xute.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import dk.itu.moapd.scootersharing.xute.R
import dk.itu.moapd.scootersharing.xute.activities.MainActivity

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private val TAG = LoginFragment::class.qualifiedName

    /**
     * This object launches a new activity and receives back some result data.
     */
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createSignInIntent()

    }
    companion object {

    }

    private fun createSignInIntent() {

        // Choose authentication providers.
        val providers = arrayListOf(
//            AuthUI.IdpConfig.EmailBuilder().build(),
//            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .setAlwaysShowSignInMethodScreen(true)
            .setLogo(R.drawable.firebase_icon)
            .setTheme(R.style.Theme_FirebaseAuthentication)
            .build()
        signInLauncher.launch(signInIntent)
    }

    /**
     * When the second activity finishes (i.e., the pre-define login activity), it returns a result
     * to this activity. If the user sign-in the application correctly, we redirect the user to the
     * main activity of this application.
     *
     * @param result A result describing that the caller can launch authentication flow with a
     *      `Intent` and is guaranteed to receive a `FirebaseAuthUIAuthenticationResult` as
     *      result.
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Sign in success, update UI with the signed-in user's information.
            Log.d(TAG,"User logged in the app.")
            startMainActivity()
        } else
        // If sign in fails, display a message to the user.
            Log.d(TAG,"Authentication failed.")
    }

    /**
     * In the case of successfully login, it opens the main activity and starts the Firebase
     * Authentication application.
     */
    private fun startMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}