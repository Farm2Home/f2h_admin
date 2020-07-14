package com.f2h.f2h_admin.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.cloudinary.android.MediaManager
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.database.F2HDatabase
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.databinding.ActivityUserPagesBinding
import kotlinx.android.synthetic.main.activity_user_pages.view.*
import kotlinx.android.synthetic.main.nav_header.view.*
import kotlinx.coroutines.*


class UserPagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserPagesBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var f2hDatabase: F2HDatabase
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_pages)
        drawerLayout = binding.drawerLayout

        MediaManager.init(this)

        val navController = this.findNavController(R.id.userPagesNavHostFragment)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)

        f2hDatabase = F2HDatabase.getInstance(this)
        updateNavHeader()

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.userPagesNavHostFragment)
        updateNavHeader()
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private fun updateNavHeader(){
        coroutineScope.launch {
            val userSessionData = retrieveSession()
            drawerLayout.navView.getHeaderView(0).navHeaderUserCredentials.text = userSessionData.userName
        }
    }

    private suspend fun retrieveSession() : SessionEntity {
        return withContext(Dispatchers.IO) {
            val sessions = f2hDatabase.sessionDatabaseDao.getAll()
            var session = SessionEntity()
            if (sessions.size==1) {
                session = sessions[0]
                println(session.toString())
            } else {
                f2hDatabase.sessionDatabaseDao.clearSessions()
            }
            return@withContext session
        }
    }

}
