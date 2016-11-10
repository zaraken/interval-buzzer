package manolov.em.nik.intervalbuzzer

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false)

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    companion object {
        const val TAG = "MainActivity"
    }

    class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_main)

            traversePreferences(preferenceScreen, { p: Preference -> updateEditTextPreferenceSummary(p) })
            activity.startService(Intent(activity, IntervalBuzzerService::class.java))
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                               key: String) {
            updateEditTextPreferenceSummary(findPreference(key))
        }

        private fun traversePreferences(p: Preference, func: (p: Preference) -> Unit) {
            when (p) {
                is PreferenceGroup -> {
                    for (i in 0 until p.preferenceCount) {
                        traversePreferences(p.getPreference(i), func)
                    }
                }
                else -> {
                    func(p)
                }
            }
        }

        private fun updateEditTextPreferenceSummary(p: Preference) {
            if (p is EditTextPreference) {
                p.setSummary(p.text)
            }
        }
    }
}
