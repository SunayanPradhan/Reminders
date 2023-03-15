package com.sunayanpradhan.reminders.Activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sunayanpradhan.reminders.Models.UserModel
import com.sunayanpradhan.reminders.R
import com.sunayanpradhan.reminders.databinding.ActivityMainBinding
import com.sunayanpradhan.reminders.databinding.ForgotPasswordDialogBinding
import com.sunayanpradhan.reminders.databinding.SignUpDialogBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding= DataBindingUtil.setContentView(this, R.layout.activity_main)

        firebaseAuth=FirebaseAuth.getInstance()

        firebaseFirestore=FirebaseFirestore.getInstance()

        val firebaseUser=firebaseAuth.currentUser

        if (firebaseUser!=null && firebaseUser.isEmailVerified){

            startActivity(Intent(this, HomeActivity::class.java))

            overridePendingTransition(0,0)

            finish()
        }


        binding.signIn.setOnClickListener {
            val mail: String = binding.signInEmail.text.toString().trim()
            val password: String = binding.signInPassword.text.toString().trim()
            if (mail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All Field Are Required", Toast.LENGTH_SHORT)
                    .show()
            } else {

                binding.progressBar.visibility= View.VISIBLE

                firebaseAuth.signInWithEmailAndPassword(mail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {


                            checkMailVerification()

                        } else {
                            Toast.makeText(this,
                                "Account Doesn't Exist",
                                Toast.LENGTH_SHORT)
                                .show()
                            binding.progressBar.visibility= View.GONE
                        }
                    }
            }

        }

        binding.goToSignUp.setOnClickListener {

            goToSignUp()

        }

        binding.goToForgotPassword.setOnClickListener {

            goToForgotPassword()

        }


    }

    private fun checkMailVerification() {

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!!.isEmailVerified) {
            Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility=View.INVISIBLE

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            finish()




        } else {
            Toast.makeText(this, "Verify Your Mail First", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility=View.GONE
            firebaseAuth.signOut()
        }
    }


    private fun goToSignUp() {

        val signUpDialog= BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

        val signUpView: View =layoutInflater.inflate(
            R.layout.sign_up_dialog,
            null
        )

        signUpDialog.setContentView(signUpView)

        val bottomSheetBehavior= BottomSheetBehavior.from(signUpView.parent as View)

        bottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED

        signUpDialog.show()

        val signUpBinding=SignUpDialogBinding.bind(signUpView)

        signUpBinding.parentLayout.minimumHeight= Resources.getSystem().displayMetrics.heightPixels

        signUpBinding.signUp.setOnClickListener {


            val mail = signUpBinding.signUpEmail.text.toString().trim()
            val password = signUpBinding.signUpPassword.text.toString().trim()
            val rePassword = signUpBinding.signUpRePassword.text.toString().trim()


            if (mail.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "All Fields are Required", Toast.LENGTH_SHORT)
                    .show()
            } else if (password.length < 7) {


                Toast.makeText(this, "Password Should at least 8", Toast.LENGTH_SHORT)
                    .show()

            } else if (password != rePassword) {

                Toast.makeText(this, "Password Does Not Match", Toast.LENGTH_SHORT)
                    .show()

            } else {

                signUpBinding.progressBar.visibility= View.VISIBLE

                firebaseAuth.createUserWithEmailAndPassword(mail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

                            val user = UserModel(firebaseUser?.uid.toString(),
                                signUpBinding.signUpName.text.toString(),
                                signUpBinding.signUpEmail.text.toString(),
                                "https://res.cloudinary.com/sunayan02/image/upload/v1659941770/suer_myphid.jpg",
                            )

                            if (firebaseUser != null) {
                                firebaseFirestore.collection("Users").document(FirebaseAuth.getInstance().uid.toString())
                                    .set(user)
                            }

                            Toast.makeText(this,
                                "Registration Successful",
                                Toast.LENGTH_SHORT).show()

                            sendEmailVerification()

                            signUpBinding.progressBar.visibility= View.GONE

                            signUpDialog.dismiss()


                        } else {
                            Toast.makeText(this,
                                "Registration Failed",
                                Toast.LENGTH_SHORT).show()

                            signUpBinding.progressBar.visibility= View.GONE

                        }

                    }

            }



        }

        signUpBinding.goToSignIn.setOnClickListener {


            signUpDialog.dismiss()


        }


    }

    private fun sendEmailVerification() {

        val firebaseUser: FirebaseUser? =firebaseAuth.currentUser


        if (firebaseUser!=null){


            firebaseUser.sendEmailVerification().addOnSuccessListener {

                Toast.makeText(this, "Verification Email is Send \n Verify and LogIn Again", Toast.LENGTH_SHORT).show()


            }
        }
        else{

            Toast.makeText(this, "Failed To Send Verification", Toast.LENGTH_SHORT).show()

        }
    }



    private fun goToForgotPassword() {

        val forgotPasswordDialog= BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

        val forgotPasswordView: View =layoutInflater.inflate(
            R.layout.forgot_password_dialog,
            null
        )

        forgotPasswordDialog.setContentView(forgotPasswordView)


        val bottomSheetBehavior= BottomSheetBehavior.from(forgotPasswordView.parent as View)

        bottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED


        forgotPasswordDialog.show()

        val forgotPasswordBinding=ForgotPasswordDialogBinding.bind(forgotPasswordView)

        forgotPasswordBinding.parentLayout.minimumHeight= Resources.getSystem().displayMetrics.heightPixels

        forgotPasswordBinding.forgotPassword.setOnClickListener {

            forgotPasswordBinding.progressBar.visibility=View.VISIBLE

            val mail: String = forgotPasswordBinding.forgotPasswordEmail.text.toString().trim()
            if (mail.isEmpty()) {
                Toast.makeText(this, "Enter Your Mail First", Toast.LENGTH_SHORT)
                    .show()

                forgotPasswordBinding.progressBar.visibility=View.GONE

            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(mail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,
                            "Mail Sent , You Can Recover Your Password Using Mail",
                            Toast.LENGTH_SHORT).show()

                        forgotPasswordBinding.progressBar.visibility=View.GONE

                        forgotPasswordDialog.dismiss()

                    }

                    else {
                        Toast.makeText(this,
                            "Email is Wrong or Account Not Exits",
                            Toast.LENGTH_SHORT).show()

                        forgotPasswordBinding.progressBar.visibility=View.GONE
                    }
                }
            }
        }

        forgotPasswordBinding.goToSignUp.setOnClickListener {

            forgotPasswordDialog.dismiss()

            goToSignUp()

        }



    }



}