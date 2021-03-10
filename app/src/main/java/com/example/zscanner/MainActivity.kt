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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat

private lateinit var codeScanner: CodeScanner
private var data: String? = null
/////////////////////////Collections Variables//////////////////////////////
var checkDevice: HashMap<String, Any>? = null
var deviceList: ArrayList<String>? = null
/////////////////////////FireBase Variables////////////////////////////////
private var currentDateandTime: String? = null
private lateinit var mAuth: FirebaseAuth
private lateinit var refUsers: DatabaseReference
private var firebaseUserID:String = ""
private lateinit var deviceUsers: DatabaseReference
private lateinit var adminAccount: DatabaseReference
private val PATH:String = "https://qrcodegenerator-7f55c-default-rtdb.firebaseio.com/"


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        checkDevice = HashMap<String, Any>()


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


        findViewById<Button>(R.id.btnReceive).setOnClickListener(View.OnClickListener{
            removeDevice()
        })



        findViewById<Button>(R.id.btnUpdate).setOnClickListener(View.OnClickListener {

            deviceList = ArrayList(checkDevice!!.keys)

            Log.i("chk",deviceList.toString())
            if (data == null) {
               Toast.makeText(this, "Please scan the QRcode", Toast.LENGTH_LONG).show()
            } else {
                var boolCheck: Boolean = true
                firebaseUserID = mAuth.currentUser!!.uid
                refUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users").child(firebaseUserID).child("deviceID")


                for (a in deviceList!!) {
                    if (data!!.toLowerCase() == a.toLowerCase()) {
                        boolCheck = false
                        break
                    }
                }

                if (boolCheck != true) {
                    Toast.makeText(applicationContext, "Device Occupied", Toast.LENGTH_SHORT).show()
                }

                if (boolCheck) {
                    val userDevice = HashMap<String, Any>()
                    userDevice[data!!] = currentDateandTime.toString()
                    refUsers.updateChildren(userDevice).addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            Toast.makeText(this, "Updated succesfully", Toast.LENGTH_LONG).show()
                        }


                    }

//                    refUsers.child("dateOfIssue").setValue(currentDateandTime).addOnCompleteListener(OnCompleteListener {
//                        Toast.makeText(this, "Updated succesfully", Toast.LENGTH_LONG).show()
//                    })
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
        adminScan()
        deviceCheck()
    }

/////////////////////////////////////////Function To Fetch Data From FireBase//////////////////////////

    private fun adminScan()
    {

        adminAccount = FirebaseDatabase.getInstance(PATH).reference.child("Admin")


        adminAccount.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                for (snapShot in p0.children)
                {
                    //if(mAuth.currentUser!!.uid == snapShot.child("uid").toString())
                    //(adminList as ArrayList<String>).add(snapShot.child("uid").getValue().toString())

                    if(mAuth.currentUser!!.uid == snapShot.child("uid").getValue().toString() )
                    {
                        findViewById<Button>(R.id.btnReceive).visibility = View.VISIBLE
                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun deviceCheck()
    {
        deviceUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users")

        try {
            deviceUsers.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    for (snapShot in p0.children) {

                        //(checkDevice as ArrayList<String>).add(snapShot.child("Device ID").toString())

                        checkDevice = snapShot.child("deviceID").getValue() as HashMap<String, Any>?



                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }

            })
        }catch (e: Exception){e.printStackTrace()}





    }

    private fun removeDevice()
    {


        deviceUsers = FirebaseDatabase.getInstance(PATH).reference.child("Users")

        deviceList = ArrayList(checkDevice!!.keys)

        if (data == null) {
            Toast.makeText(this, "Please scan the QRcode", Toast.LENGTH_LONG).show()
        }else
        {
            for (a in deviceList!!) {
                if (data!!.toLowerCase() == a.toLowerCase()) {
                    Toast.makeText(applicationContext,"Device matched",Toast.LENGTH_LONG).show()
                    deviceUsers.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {


                            for (snapShot in p0.children)
                            {
                              // deviceUsers.child(data!!.toString()).setValue("value ")
                                  if(snapShot.child("deviceID").hasChild(data!!))
                                  {
                                      snapShot.child("deviceID").child(data!!).ref.removeValue()
                                  }
                                Log.i("ss",snapShot.child("deviceID").hasChild(data!!).toString())
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                    break
                }
            }
        }





    }


}