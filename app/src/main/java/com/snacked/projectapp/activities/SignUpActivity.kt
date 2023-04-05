package com.snacked.projectapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.snacked.projectapp.R
import com.snacked.projectapp.firebase.FirestoreClass
import com.snacked.projectapp.models.User

class SignUpActivity : BaseActivity() {

    private var etName : AppCompatEditText? = null
    private var etEmail : AppCompatEditText? = null
    private var etPassword : AppCompatEditText? = null
    private var btnSignUp : Button? = null

    private var toolbarSignUpActivity : Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnSignUp = findViewById(R.id.btn_sign_up)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        toolbarSignUpActivity = findViewById(R.id.toolbar_sign_up_activity)
        setupActionBar()
    }

    fun userRegisteredSuccess(){
        Toast.makeText(
            this,
            " You have successfully registered the email address ",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()

        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarSignUpActivity)

        val actionBar = supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }

        btnSignUp?.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser(){
        val name: String = etName?.text.toString().trim{ it <= ' '}
        val email: String = etEmail?.text.toString().trim{ it <= ' '}
        val password: String = etPassword?.text.toString().trim{ it <= ' '}

        if(validateForm(name,email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, name, registeredEmail)
                    FirestoreClass().registerUser(this, user)

                } else {
                    Toast.makeText(
                        this,
                    "Registration Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean{
        return when{
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please enter a password")
                false
            }else->{
                true
            }

        }
    }
}