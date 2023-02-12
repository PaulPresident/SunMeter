package com.paul.sunmeter

import android.content.SharedPreferences
import android.location.Location
import android.util.Log

class VitaminDProductionCalculator(prefs: SharedPreferences) {
    private var store: SharedPreferences

    init {
        this.store = prefs
    }

    fun getVitaminD(): Double {
        return store.getFloat(STORE_VITAMIN_D, 0F).toDouble()
    }
    fun setVitaminD(value: Float) {
        updateStore(STORE_VITAMIN_D, value)
    }

    fun isUserOutside(location: Location): Boolean {
        val accuracy = location.accuracy
        if ( accuracy > 50 ) {
            if ( store.getBoolean(STORE_USER_OUTSIDE, false) ) {
                addVitaminD( FG_SERVICE_DELAY_MINS )  // user was and is still outside

            } else {
                addVitaminD( FG_SERVICE_DELAY_MINS/2 )  // user went outside
            }
            updateStore( STORE_USER_OUTSIDE, true )
            return true
        }
        if ( store.getBoolean(STORE_USER_OUTSIDE, false) ) {
            addVitaminD( FG_SERVICE_DELAY_MINS/2 )  // user was recently outside
        }
        updateStore( STORE_USER_OUTSIDE, false )
        return false
    }

    private fun addVitaminD( minutes: Double ) {
        val acquiredVitaminD = 100 * minutes / sunExposureReqTime(
            store.getString(STORE_SKIN_TYPE, "3"),
            store.getInt(STORE_UVI, 0)
        )
        val newVitaminD = store.getFloat(STORE_VITAMIN_D, 0F) + acquiredVitaminD
        Log.e("VITAMIND", "new: $acquiredVitaminD    tot: $newVitaminD")
        updateStore( STORE_VITAMIN_D, newVitaminD)
    }

    private fun sunExposureReqTime( skinType: String?, uvi: Int? ): Double {
        return when ( skinType ) {
            "1" -> { type1( uvi!! ) }
            "2" -> { type2( uvi!! ) }
            "3" -> { type3( uvi!! ) }
            "4" -> { type4( uvi!! ) }
            "5" -> { type5( uvi!! ) }
            else -> { type3( uvi!! ) }
        }
    }

    private fun updateStore( store_id: String, value: Any ) {
        val editor = store.edit()
        when (value::class.simpleName) {
            "String" -> { editor.putString( store_id, value as String) }
            "Double" -> { editor.putFloat( store_id, (value as Double).toFloat() ) }
            "Int" -> { editor.putInt( store_id, value as Int) }
            "Boolean" -> { editor.putBoolean( store_id, value as Boolean) }
            else -> {}
        }
        editor.apply()
    }
}
