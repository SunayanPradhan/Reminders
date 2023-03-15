package com.sunayanpradhan.reminders.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sunayanpradhan.reminders.Adapters.ReminderAdapter
import com.sunayanpradhan.reminders.Models.ReminderModel
import com.sunayanpradhan.reminders.R
import com.sunayanpradhan.reminders.databinding.ActivityCompletedRemindersBinding
import java.util.*

class CompletedRemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletedRemindersBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_reminders)

        binding= DataBindingUtil.setContentView(this, R.layout.activity_completed_reminders)

        firebaseAuth= FirebaseAuth.getInstance()

        firebaseFirestore= FirebaseFirestore.getInstance()

        setUpReminderAdapter()

        binding.optionsBack.setOnClickListener {

            finish()

        }


    }

    private fun setUpReminderAdapter(){

        val query=FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myReminders").whereEqualTo("reminderCompleted",true)


        val options = FirestoreRecyclerOptions.Builder<ReminderModel>().setQuery(query,
            ReminderModel::class.java).setLifecycleOwner(this).build()


        reminderAdapter= ReminderAdapter(options)

        recyclerViewSetUp()

    }



    private fun recyclerViewSetUp(){

        binding.optionsRecyclerView.setHasFixedSize(true)

        binding.optionsRecyclerView.adapter=reminderAdapter

        val layoutManager= LinearLayoutManager(this)

        binding.optionsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        binding.optionsRecyclerView.layoutManager=layoutManager

        binding.optionsRecyclerView.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

    }


}