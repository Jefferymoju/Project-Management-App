package com.snacked.projectapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.snacked.projectapp.R

class IntroActivity : BaseActivity() {

    private var btnSignUpIntro : Button? = null
    private var btnSignInIntro: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        btnSignUpIntro = findViewById(R.id.btn_sign_up_intro)
        btnSignInIntro = findViewById(R.id.btn_sign_in_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        btnSignUpIntro!!.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnSignInIntro!!.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }


    }
}