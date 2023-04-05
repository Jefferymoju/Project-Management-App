package com.snacked.projectapp.activities

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import com.snacked.projectapp.R
import com.snacked.projectapp.adapters.BoardItemsAdapter
import com.snacked.projectapp.databinding.ActivityMainBinding
import com.snacked.projectapp.firebase.FirestoreClass
import com.snacked.projectapp.models.Board
import com.snacked.projectapp.utils.Constants

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences
    private var binding: ActivityMainBinding? = null
    private var rvBoardsList: RecyclerView? = null
    private var tvNoBoardsAvailable: TextView? = null
    private var toolbarMainActivity: Toolbar? = null
    private var drawerLayout : DrawerLayout? = null
    private var navView: NavigationView? = null
    private var navUserImage: ImageView? = null
    private var tvUserName: TextView? = null
    private var fabCreateBoard: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        toolbarMainActivity = findViewById(R.id.toolbar_main_activity)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navUserImage = findViewById(R.id.nav_user_image)
        tvUserName = findViewById(R.id.tv_username)
        fabCreateBoard = findViewById(R.id.fab_create_board)
        rvBoardsList = findViewById(R.id.rv_boards_list)
        tvNoBoardsAvailable = findViewById(R.id.tv_no_boards_available)

        setActionBar()
        navView!!.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.PROJECTAPP_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this, true)
        }else{
           FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
               updateFcmToken(instanceIdResult)
           }
        }

        FirestoreClass().loadUserData(this, true)

        fabCreateBoard!!.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    fun populateBoardsListToUI(boardsList:ArrayList<Board>){
        hideProgressDialog()

        if (boardsList.size > 0){
            rvBoardsList!!.visibility = View.VISIBLE
            tvNoBoardsAvailable!!.visibility = View.GONE

            rvBoardsList!!.layoutManager = LinearLayoutManager(this)
            rvBoardsList!!.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rvBoardsList!!.adapter = adapter

           adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
               override fun onClick(position: Int, model: Board) {
                   val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                   intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                   startActivity(intent)
               }

           })

        }else{
            rvBoardsList!!.visibility = View.GONE
            tvNoBoardsAvailable!!.visibility = View.VISIBLE
        }

    }

    private fun setActionBar(){
        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if(drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
           drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if(drawerLayout?.isDrawerOpen(GravityCompat.START) == true){
            drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
           doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: com.snacked.projectapp.models.User, readBoardsList: Boolean) {
        hideProgressDialog()
        // The instance of the header view of the navigation view.
        val headerView = navView?.getHeaderView(0)

        // The instance of the user image of the navigation view.
        val navUserImage = headerView?.findViewById<ImageView>(R.id.nav_user_image)

        // Load the user image in the ImageView.
        if (navUserImage != null) {

            mUserName = user.name

            Glide
                .with(this@MainActivity)
                .load(user.image) // URL of the image
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_place_holder) // A default place holder
                .into(navUserImage)
        } // the view in which the image will be loaded.

        // The instance of the user name TextView of the navigation view.
        val navUsername = headerView?.findViewById<TextView>(R.id.tv_username)
        // Set the user name
        if (navUsername != null) {
            navUsername.text = user.name
        }

        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
           FirestoreClass().getBoardsList(this)
        }

        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile ->{
               startActivityForResult(Intent(this@MainActivity, MyProfileActivity::class.java),
                   MY_PROFILE_REQUEST_CODE)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun tokenUpdateSuccess(){
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFcmToken(token: String){
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
}





