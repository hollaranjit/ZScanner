package com.example.zscanner.login

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.zscanner.CheckNetwork
import com.example.zscanner.MainActivity
import com.example.zscanner.R
import com.example.zscanner.signup.SignUpActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var etLoginEmail      : TextInputEditText
    lateinit var etLoginPassword   : TextInputEditText
    private lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        etLoginEmail    = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)

        mAuth = FirebaseAuth.getInstance()



        if (ContextCompat.checkSelfPermission(
                getApplicationContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED)
        {

        }else {
            ActivityCompat.requestPermissions(
                this,
                Array<String>(1) { android.Manifest.permission.CAMERA },
                0
            );
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener(View.OnClickListener { loginUser() })

        findViewById<Button>(R.id.btnSignUp).setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        })
    }

    private fun loginUser()
    {
        val loginEmail: String = etLoginEmail.text.toString().trim()
        val loginPassword : String = etLoginPassword.text.toString().trim()

        if(loginEmail.equals(""))
        {
            etLoginEmail.setError("Enter Email")
        }
        else if (loginPassword.equals(""))
        {
            etLoginPassword.setError("Enter Password")
        }
        else
        {
            mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener {task->

                if(task.isSuccessful)
                {
                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    Toast.makeText(this, "Error Message" + task.exception!!.message, Toast.LENGTH_SHORT).show()

                }

            }
        }

    }

    override fun onStart() {
        super.onStart()
        if(CheckNetwork.isInternetAvailable(this) == true)
        {

        }
        else
        {
            Toast.makeText(this,"check network connection",Toast.LENGTH_LONG).show()
        }
    }


}