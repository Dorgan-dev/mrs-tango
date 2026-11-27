package com.example.mrs_tango.core.base // Sesuaikan package kamu

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mrs_tango.R
import com.example.mrs_tango.auth.AuthActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

abstract class BaseDrawerActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    private var drawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    protected fun initDrawer(drawer: DrawerLayout, navView: NavigationView) {
        this.drawerLayout = drawer
        this.navigationView = navView

        val prefs = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
        val role = prefs.getString("user_role", "customer")?.lowercase() ?: "customer"
        val userName = prefs.getString("user_name", "Pengguna") ?: "Pengguna"

        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvHeaderName)?.text = userName
        headerView.findViewById<TextView>(R.id.tvHeaderRole)?.text = role.uppercase()

        navigationView.menu.clear()
        when (role) {
            "owner" -> navigationView.inflateMenu(R.menu.owner_menu_drawer)
            "customer" -> navigationView.inflateMenu(R.menu.customer_menu_drawer)
            else -> navigationView.inflateMenu(R.menu.customer_menu_drawer)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
                performLogout()
            } else {
                onDrawerItemSelected(menuItem.itemId)
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    /**
     * Dipakai oleh Fragment UTAMA (munculkan tombol burger & buka kunci swipe sidebar)
     */
    fun setupToolbarWithDrawer(
        toolbar: Toolbar,
        title: String,
        iconColor: Int = Color.WHITE
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = title

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        drawerToggle?.let { drawerLayout.removeDrawerListener(it) }

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ).apply {
            drawerArrowDrawable.color = iconColor
        }

        drawerLayout.addDrawerListener(drawerToggle!!)
        drawerToggle!!.syncState()
    }

    fun openDrawer() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    fun unlockDrawer() {
        if (::drawerLayout.isInitialized) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    /**
     * Dipakai oleh Fragment TURUNAN/FORM (munculkan tombol Back & kunci sidebar)
     */
    fun setupToolbarWithBack(
        toolbar: Toolbar,
        title: String,
        iconColor: Int = Color.WHITE
    ) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        toolbar.navigationIcon?.setTint(iconColor)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun performLogout() {
        val sharedPref = this.getSharedPreferences("user_pref", MODE_PRIVATE)

        MaterialAlertDialogBuilder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah anda ingin logout?")
            .setPositiveButton("Ya") { dialog, _ ->
                val editor = sharedPref.edit()
                editor.clear()
                editor.apply()
                dialog.dismiss()

                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                this.finish()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    protected abstract fun onDrawerItemSelected(itemId: Int)
}