package com.paul.sunmeter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Home

        // Permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        // Foreground Location Service
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }

        // Get SharedPreferences and store UVI from API
        val sharedPreferences: SharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        storeUVI(sharedPreferences)

        // View Content
        val valCurrentStatus = findViewById<TextView>(R.id.valCurrentStatus)
        val valUVI = findViewById<TextView>(R.id.valUVI)
        val mtrVitaminD = findViewById<ProgressBar>(R.id.mtrVitaminD)

        // Update View Content
        valCurrentStatus.text = if ( sharedPreferences.getBoolean(STORE_USER_OUTSIDE, false) ) "Outside" else "Inside"
        valUVI.text = sharedPreferences.getInt(STORE_UVI, 0).toString()
        mtrVitaminD.progress = sharedPreferences.getFloat(STORE_VITAMIN_D, 0F).toInt()

        // Create ProgressBar Toast
        mtrVitaminD.setOnLongClickListener {
            Toast.makeText(this, "VitaminD (DV): ${mtrVitaminD.progress}%", Toast.LENGTH_SHORT).show()
            true
        }

        // Logic for SharedPreferences Change
//        TODO("there is an issue where this doesn't run when 'W/System: A resource failed to call close.'")
        sharedPreferences.registerOnSharedPreferenceChangeListener { store, key ->
            Log.e("STORE UPDATE", key)
            when (key) {
                STORE_VITAMIN_D -> { mtrVitaminD.progress = store.getFloat(STORE_VITAMIN_D, 0F).toInt() }
                STORE_UVI -> { valUVI.text = store.getInt(STORE_UVI, 0).toString() }
                else -> {}
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuSettings -> { true } // TODO( "route to Settings")
            R.id.menuAbout -> { true } // TODO("route to About")
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun storeUVI(prefs : SharedPreferences) {
//        updateStore(prefs, STORE_UVI, 8)   // The API has a daily limit switch to this for testing instead
        val myHeaders = Headers.Builder()
        myHeaders.add("x-access-token", "openuv-3060ohwrle0athkb-io")
        myHeaders.add("Content-Type", "application/json")

        val requestOptions = Request.Builder()
            // location coordinates of Mumbai, India for the sake of testing (need high UVI for testing)
            .url("https://api.openuv.io/api/v1/uv?lat=19.07&lng=72.87&alt=100&dt=")
            .headers(myHeaders.build())
            .get()
            .build()

        val client = OkHttpClient()
        client.newCall(requestOptions).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val rawData = JSONObject(response.body()!!.string())
                val res = JSONObject(rawData.get("result").toString())

                val uvi = (res.get("uv") as Double).toInt().toString()
                updateStore(prefs, STORE_UVI, uvi)
            }
        })
    }

    private fun updateStore( store: SharedPreferences, store_id: String, value: Any ) {
        val editor = store.edit()
        when (value::class.simpleName) {
            "String" -> { editor.putString( store_id, value as String) }
            "Double" -> { editor.putFloat( store_id, value as Float) }
            "Int" -> { editor.putInt( store_id, value as Int) }
            "Boolean" -> { editor.putBoolean( store_id, value as Boolean) }
            else -> {}
        }
        editor.apply()
    }
}