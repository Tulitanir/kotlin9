package com.example.project

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.project.databinding.ActivityMainBinding
import com.example.project.util.PasswordChangeListener
import com.example.project.util.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), PasswordChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val userDataLoaded = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        val drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        DataManager.setHeader(navView)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_edit, R.id.nav_slideshow, R.id.nav_gallery,
                R.id.nav_music, R.id.nav_imagen, R.id.nav_settings, R.id.nav_post_image,
                R.id.nav_all_posts
            ),
            drawerLayout,
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        if (auth.currentUser != null) {
            if (DataManager.getUserData() == null) {
                db.collection("users")
                    .document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener {
                        val userId = it.id
                        val user = it.toObject<User>()
                        if (user != null) {
                            DataManager.setId(userId)
                            DataManager.setUserData(user)
                            userDataLoaded.value = true
                        }
                    }
            } else {
                userDataLoaded.value = true
            }
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_home -> {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    supportActionBar?.setHomeAsUpIndicator(null)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                R.id.nav_registation -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                else -> {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
            if (!appBarConfiguration.topLevelDestinations.contains(destination.id)) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onChangePassword(oldPassword: String, newPassword: String) {
        Log.d("Pass", "New Password: $newPassword Old Password: $oldPassword")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    object DataManager {
        private var user: User? = null
        private var id: String? = null
        private var header: NavigationView? = null

        fun setHeader(header: NavigationView) {
            this.header = header
        }

        fun setUserData(user: User) {
            this.user = user

            val text = header?.findViewById<TextView>(R.id.headertextNameSurname)
            text!!.text = buildString {
                append(user.username)
                append(" ")
                append(user.usersurname)
            }

            val login = header?.findViewById<TextView>(R.id.headertextLogin)
            login!!.text = user.login

            val date = header?.findViewById<TextView>(R.id.headertextDate)
            date!!.text = user.date

            val blb = user.image

            if (blb != null) {
                val arr = blb.toBytes()
                val bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
                val image = header?.findViewById<ImageView>(R.id.headerimageView)
                image!!.setImageBitmap(bitmap)
            }
        }

        fun getUserData(): User? {
            return user
        }

        fun setId(id: String) {
            this.id = id
        }

        fun getId(): String? {
            return id
        }

        fun clearData() {
            user = null
            id = null
        }
    }
}