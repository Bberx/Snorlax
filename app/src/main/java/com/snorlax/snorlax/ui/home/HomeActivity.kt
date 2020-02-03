/*
 * Copyright 2019 Oliver Rhyme G. Añasco
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.snorlax.snorlax.ui.home


import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.snorlax.snorlax.R
import com.snorlax.snorlax.ui.home.attendance.AttendanceFragment
import com.snorlax.snorlax.utils.exitApp
import com.snorlax.snorlax.utils.startLoginActivity
import com.snorlax.snorlax.viewmodel.HomeActivityViewModel
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.diag_loading.*
import kotlinx.android.synthetic.main.navigation_header.view.*


class HomeActivity : AppCompatActivity() {

    private lateinit var userIcon: CircleImageView
    private lateinit var emailTextView: TextView
    private lateinit var labelEmail: TextView
    private lateinit var labelName: TextView
    private lateinit var labelRole: TextView

    private lateinit var drawerToggle: ActionBarDrawerToggle


    private lateinit var viewModel: HomeActivityViewModel

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeActivityViewModel::class.java]

        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction().replace(
//                R.id.fragment_container,
//                ScanFragment()
//            ).commit()
            supportFragmentManager.commit {
                replace(R.id.fragment_container, ScanFragment())
            }
            nav_view.setCheckedItem(R.id.nav_scan)
        }

        nav_view.setNavigationItemSelectedListener {
            if (nav_view.checkedItem == it) {
                drawer_layout.closeDrawer(GravityCompat.START)
                return@setNavigationItemSelectedListener true
            }
            when (it.itemId) {
                R.id.nav_scan -> {
//                    supportFragmentManager.beginTransaction().replace(
//                        R.id.fragment_container,
//                        ScanFragment()
//                    ).commit()
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, ScanFragment())
                    }
                }

                R.id.nav_attendance -> {
//                    supportFragmentManager.beginTransaction().replace(
//                        R.id.fragment_container,
//                        AttendanceFragment()
//                    ).commit()
                    supportFragmentManager.commit {
                        replace(
                            R.id.fragment_container,
                            AttendanceFragment()
                        )
                    }
                }

                R.id.nav_students -> {
//                    supportFragmentManager.beginTransaction().replace(
//                        R.id.fragment_container,
//                        StudentsFragment()
//                    ).commit()
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, StudentsFragment())
                    }
                }

                R.id.nav_about -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container, AboutFragment())
                    }
                }


                R.id.nav_exit -> exit()
                R.id.nav_sign_out -> logout()

            }
            drawer_layout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true

        }


        val header = nav_view.getHeaderView(0)
        emailTextView = header.label_email
        userIcon = header.ic_user
        labelEmail = header.label_email
        labelName = header.label_name
        labelRole = header.label_role


        initObservables()

    }

    private fun initObservables() {
        disposables.add(
            viewModel.getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    emailTextView.text = it.email
                    Glide.with(this)
                        .load(viewModel.getUserPhoto())
                        .placeholder(R.drawable.img_avatar)
//                    .transition(DrawableTransitionOptions().crossFade())
//                    .apply(RequestOptions().placeholder(R.drawable.default_avatar))
                        .into(userIcon)
//                userIcon.setImageResource(R.mipmap.ic_launcher)
                    labelEmail.text = it.email
                    labelName.text = it.displayName
                    labelRole.text = viewModel.getRole(it)
                })

//        disposables.add(
//            FirebaseAuthSource.getInstance().loggedOut().subscribe {
//                if (it == true) {
//                    startLoginActivity()
//                }
//            }
//        )

//        btn_logout.clicks().subscribe(homeActivityViewModel.logoutObservable)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    override fun onBackPressed() {
//        super.onBackPressed()

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            exit()
        }
    }

    private fun logout() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Logout")
            setMessage("Are you sure you want to logout?")
            setPositiveButton(
                "Yes"
            ) { _, _ ->
                setDialog(true)
                viewModel.logout().subscribe({
                    startLoginActivity()
                }, {
                    setDialog(false)
                    Snackbar.make(
                        home_layout,
                        it.localizedMessage ?: "Logout failed",
                        Snackbar.LENGTH_LONG
                    ).show()
                })
//                setProgressDialog()
//                viewModel.logout()
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe {
//                        startLoginActivity()
//                    }
            }
            setIcon(R.drawable.ic_logout)
            setNegativeButton("No", null)
        }.create().show()
    }

    private fun setDialog(show: Boolean) {
        val diag = object : Dialog(this) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.diag_loading)
                loading_message.text = getString(R.string.label_logging_out)
            }
        }

        if (show) {
            diag.show()
        } else diag.dismiss()
//        val builder = MaterialAlertDialogBuilder(this).setCancelable(false)
//        builder.setView(R.layout.diag_loading)
//        val dialog: Dialog = builder.create()
//
//        val window = dialog.window
//        window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        window?.setGravity(Gravity.CENTER)
//
//        dialog.setCanceledOnTouchOutside(false)
//        if (show) {
//            dialog.show()
//            dialog.loading_message.text = getString(R.string.label_logging_out)
////            dialog.window!!.setLayout(dialog.loading_container.width, ViewGroup.LayoutParams.WRAP_CONTENT)
//        } else dialog.dismiss()
    }


    private fun exit() {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Exit")
            setMessage("Are you sure you want to exit?")
            setPositiveButton(
                "Yes"
            ) { dialog, _ ->

                //                val homeIntent =
//                    Intent(Intent.ACTION_MAIN)
//                homeIntent.addCategory(Intent.CATEGORY_HOME)
//                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                startActivity(homeIntent)
//                exitProcess(0)

//                finish()
//                CameraX.unbindAll()
//                finishAffinity()
//                finishAndRemoveTask()
//                super.onBackPressed()
                dialog.dismiss()
                exitApp()
            }
            setIcon(R.drawable.ic_exit)
            setNegativeButton("No", null)
        }.create().show()
    }
}