package com.example.zscanner

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception
import java.sql.Date
import java.text.SimpleDateFormat

private lateinit var codeScanner: CodeScanner
private var data: String? = null
var checkDevice: List<String>? = null
private var currentDateandTime: String? = null
private lateinit var mAuth: FirebaseAuth
private lateinit var refUsers: DatabaseReference
private var firebaseUserID:String = ""
private lateinit var deviceUsers: DatabaseReference
private val PATH:String = "https://qrcodegenerator-7f55c-default-rtdb.firebaseio.com/"


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkDevice = ArrayList()



        val sdf = SimpleDateFormat("dd.MM.yyyy")
        currentDateandTime = sdf.format(java.util.Date())

        Log.i("date", currentDateandTime.toString())

        mAuth = FirebaseAuth.getInstance()

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        codeScanner = CodeScanner(this, scannerView)


        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not


        if (ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED)
        {
            codeScanner.decodeCallback = DecodeCallback {
                runOnUiThread {
                    data = it.text.toString()
                    Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                }
            }
            codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
                runOnUiThread {
                    Toast.makeText(this, "Camera initialization error: ${it.message}",
                            Toast.LENGTH_LONG).show()
                }
            }

            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }

        }else {
            ActivityCompat.requestPermissions(
                    this,
                    Array<String>(1) { android.Manifest.permission.CAMERA },
                    0
            );
        }


        scannerView.setOnClickListener(View.OnClickListener {
            codeScanner.startPreview()
        })


        findViewById<Button>(R.id.btnExit).setOnClickListener(View.OnClickListener {
            finishAffinity()
        })



        findViewById<Button>(R.id.btnUpdate).setOnClickListener(View.OnClickListener {

            if (data == null) {
                Toast.makeText(this, "Please scan the QRcode", Toast.LENGTH_LONG).show()
            } else {
                var boolCheck :Boolean = true
                firebaseUserID = mAuth.currentUser!!.uid
                refUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users").child(firebaseUserID)


                for(a in checkDevice!!) {
                    if( data!!.toLowerCase() == a.toLowerCase())
                    {
                        boolCheck = false
                        break
                    }
                }

                if(boolCheck != true)
                {
                    Toast.makeText(applicationContext,"Device Occupied", Toast.LENGTH_SHORT).show()
                }

                if(boolCheck)
                {
                    refUsers.child("Device ID").child("deviceID").setValue(data).addOnCompleteListener(OnCompleteListener {
                        Toast.makeText(this, "Updated succesfully", Toast.LENGTH_LONG).show()
                    })

                    refUsers.child("dateOfIssue").setValue(currentDateandTime).addOnCompleteListener(OnCompleteListener {
                        Toast.makeText(this, "Updated succesfully", Toast.LENGTH_LONG).show()
                    })
                }


            }


        })


    }


    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()

    }

    override fun onStart() {
        super.onStart()
        deviceCheck()
    }


    private fun deviceCheck()
    {
        deviceUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users")

        try {
            deviceUsers.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    for(snapShot in p0.children)
                    {

                        (checkDevice as ArrayList<String>).add(snapShot.child("Device ID").child("deviceID").toString())


                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }

            })
        }catch (e: Exception){e.printStackTrace()}





    }


}