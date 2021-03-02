package com.example.zscanner.signup

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.zscanner.R
import com.example.zscanner.login.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    lateinit var etEmail      : TextInputEditText
    lateinit var etUsername   : TextInputEditText
    lateinit var etZucitechID : TextInputEditText
    lateinit var etPassword   : TextInputEditText
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID:String = ""
    private val PATH:String = "https://qrcodegenerator-7f55c-default-rtdb.firebaseio.com/"
    lateinit var dialog: AlertDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        etEmail = findViewById(R.id.etEmail)
        etUsername = findViewById(R.id.etUserName)
        etZucitechID = findViewById(R.id.etZucitechId)
        etPassword = findViewById(R.id.etPassword)

        mAuth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnCreate).setOnClickListener(View.OnClickListener {
            registerUser()
        })

    }
    private fun registerUser()
    {
        val email : String = etEmail.text.toString().trim()
        val userName :String = etUsername.text.toString().trim()
        val zucitechID : String = etZucitechID.text.toString().trim().toUpperCase()
        val password :String = etPassword.text.toString().trim()


        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setView(R.layout.layout_loading_dialog)
        }

        dialog = builder.create()


        if (email.equals(""))
        {
            etEmail.setError("Enter Email")
        }
        else if (userName.equals(""))
        {
            etUsername.setError("Enter UserName")
        }
        else if (zucitechID.equals(""))
        {
            etZucitechID.setError("Enter ZucitechID")
        } else if(password.equals(""))
        {
            etPassword.setError("Enter Password")
        }
        else
        {


            dialog.show()

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task->


                if (task.isSuccessful)
                {
                    firebaseUserID = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users").child(firebaseUserID)

                    val userHashMap = HashMap<String,Any>()
                    userHashMap["uid"] = firebaseUserID
                    userHashMap["userName"] = userName
                    userHashMap["zucitechID"] = zucitechID
                    userHashMap["dateOfIssue"] = ""


                    val previousDevice = HashMap<String,Any>()
                    previousDevice["deviceName"] = ""

                    val currentDevice = HashMap<String,Any>()
                    currentDevice["deviceID"] = ""


                    refUsers.updateChildren(userHashMap).addOnCompleteListener { task ->
                        if(task.isSuccessful)
                        {
                            refUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users").child(firebaseUserID).child("Previous Device")

                            refUsers.updateChildren(previousDevice).addOnCompleteListener { task ->

                            }

                            refUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users").child(firebaseUserID).child("Device ID")

                            refUsers.updateChildren(currentDevice).addOnCompleteListener { task ->

                            }

                            val intent = Intent(this, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }

                    }

                }
                else
                {
                    dialog.dismiss()
                    Toast.makeText(this, "Error Message" + task.exception!!.message, Toast.LENGTH_SHORT).show()
                }



            }
        }
        dialog.dismiss()
    }
}