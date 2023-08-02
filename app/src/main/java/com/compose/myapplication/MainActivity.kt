package com.compose.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.compose.myapplication.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {

    companion object{
        const val WRITE_EXTERNAL_STORAGE_IMAGE = 1
        const val WRITE_EXTERNAL_STORAGE_MUSIC = 2
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updatePuzzleStats()
        }
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(SaveMusicService.MUSIC_SAVED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun updatePuzzleStats(){
        val fragment: SecondFragment? =
            supportFragmentManager.findFragmentByTag(SecondFragment.TAG) as SecondFragment?
        fragment?.updatePuzzleStats()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_IMAGE -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val puzzleFragment: FirstFragment? =
                        supportFragmentManager.findFragmentByTag(FirstFragment.TAG) as FirstFragment?
                    puzzleFragment?.saveImage()
                } else {
                    Snackbar.make(
                        binding.mainCoordinator,
                        R.string.permission_not_granted,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                return
            }

            WRITE_EXTERNAL_STORAGE_MUSIC -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    val puzzleFragment: FirstFragment? =
                        supportFragmentManager.findFragmentByTag(FirstFragment.TAG) as FirstFragment?
                    puzzleFragment?.saveMusic()
                } else {
                    Snackbar.make(
                        binding.mainCoordinator,
                        R.string.permission_not_granted,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}