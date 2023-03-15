package com.sunayanpradhan.reminders.Activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sunayanpradhan.reminders.Adapters.ReminderAdapter
import com.sunayanpradhan.reminders.Models.ReminderModel
import com.sunayanpradhan.reminders.Models.UserModel
import com.sunayanpradhan.reminders.R
import com.sunayanpradhan.reminders.databinding.ActivityHomeBinding
import com.sunayanpradhan.reminders.databinding.AddReminderDialogBinding
import com.sunayanpradhan.reminders.databinding.CancelDialogBinding
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var reminderAdapter: ReminderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        binding= DataBindingUtil.setContentView(this,R.layout.activity_home)

        firebaseAuth= FirebaseAuth.getInstance()

        firebaseFirestore= FirebaseFirestore.getInstance()


        setUpReminderAdapter()

        val firebaseUser= firebaseAuth.currentUser

        if (firebaseUser!=null && firebaseUser.isEmailVerified){

            firebaseFirestore.collection("Users").document(firebaseAuth.uid.toString()).addSnapshotListener { value, error ->

                val data=value?.toObject(UserModel::class.java)

                Glide.with(this).load(data?.userProfile).into(binding.profilePicture)


            }
        }

        FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myReminders").whereEqualTo("reminderCompleted",false).get().addOnSuccessListener {

                if (it!=null){

                    binding.reminderAllCount.text= it.documents.size.toString()


                }


            }

        FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myReminders").whereEqualTo("reminderCompleted",true).get().addOnSuccessListener {

                if (it!=null){

                    binding.reminderCompletedCount.text= it.documents.size.toString()

                }

            }



//        86,400,000

        FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myReminders").whereLessThan("reminderDate",Date().time+86400000).get().addOnSuccessListener {

                if (it!=null){

                    binding.reminderTodayCount.text= it.documents.size.toString()

                }

            }

        FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myReminders").whereGreaterThan("reminderDate",Date().time).get().addOnSuccessListener {

                if (it!=null){

                    binding.reminderScheduledCount.text= it.documents.size.toString()

                    binding.reminderScheduledCount.viewTreeObserver.isAlive

                }

            }




        binding.addReminderFab.setOnClickListener {

            showAddReminder()

        }

        binding.reminderToday.setOnClickListener {

            val intent= Intent(this,TodayRemindersActivity::class.java)

            startActivity(intent)

        }

        binding.reminderScheduled.setOnClickListener {

            val intent= Intent(this,ScheduledRemindersActivity::class.java)

            startActivity(intent)

        }

        binding.reminderAll.setOnClickListener {

            val intent= Intent(this,AllRemindersActivity::class.java)

            startActivity(intent)

        }

        binding.reminderCompleted.setOnClickListener {

            val intent= Intent(this,CompletedRemindersActivity::class.java)

            startActivity(intent)

        }


    }



    private fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd/MM/yyyy hh:mm aa")
        return df.parse(date).time
    }

    private fun setUpReminderAdapter(){

        val query=FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myReminders").whereEqualTo("reminderCompleted",false).orderBy("reminderDate")


        val options = FirestoreRecyclerOptions.Builder<ReminderModel>().setQuery(query,ReminderModel::class.java).setLifecycleOwner(this).build()


        reminderAdapter= ReminderAdapter(options)

        recyclerViewSetUp()

    }

    private fun recyclerViewSetUp(){

        binding.reminderRecyclerView.setHasFixedSize(true)

        binding.reminderRecyclerView.adapter=reminderAdapter

//        val layoutManager= LinearLayoutManager(this)

        val layoutManager=WrapContentLinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)

        binding.reminderRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        binding.reminderRecyclerView.layoutManager=layoutManager

        binding.reminderRecyclerView.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

    }

    class WrapContentLinearLayoutManager(
        context: Context?,
        orientation: Int,
        reverseLayout: Boolean
    ) : LinearLayoutManager(
        context,
        orientation,
        reverseLayout
    ) {

        override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {

            }
        }
    }

    private fun showAddReminder() {

        val showAddReminderDialog= BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

        val showAddReminderView: View =layoutInflater.inflate(
            R.layout.add_reminder_dialog,
            null
        )

        showAddReminderDialog.setContentView(showAddReminderView)


        val bottomSheetBehavior= BottomSheetBehavior.from(showAddReminderView.parent as View)

        bottomSheetBehavior.state= BottomSheetBehavior.STATE_EXPANDED


        showAddReminderDialog.show()

        showAddReminderDialog.setCancelable(false)

        val showAddReminderBinding= AddReminderDialogBinding.bind(showAddReminderView)

        showAddReminderBinding.parentLayout.minimumHeight= Resources.getSystem().displayMetrics.heightPixels

        showAddReminderBinding.reminderCancel.setOnClickListener {

            if (
                showAddReminderBinding.reminderTitle.text.isNotEmpty() ||
                showAddReminderBinding.reminderDescription.text.isNotEmpty() ||
                showAddReminderBinding.reminderDate.text.isNotEmpty() ||
                showAddReminderBinding.reminderTime.text.isNotEmpty()){

                val cancelDialog=BottomSheetDialog(this)

                val showCancelDialogView: View =layoutInflater.inflate(
                    R.layout.cancel_dialog,
                    null
                )

                cancelDialog.setContentView(showCancelDialogView)

                (showCancelDialogView.parent as View).setBackgroundColor(Color.TRANSPARENT)

                val cancelBottomSheetBehavior= BottomSheetBehavior.from(showCancelDialogView.parent as View)

                cancelBottomSheetBehavior.state= BottomSheetBehavior.STATE_EXPANDED

                cancelDialog.show()

                val showCancelBinding= CancelDialogBinding.bind(showCancelDialogView)

                showCancelBinding.discard.setOnClickListener {

                    cancelDialog.dismiss()

                    showAddReminderDialog.dismiss()

                }

                showCancelBinding.cancel.setOnClickListener {

                    cancelDialog.dismiss()

                }


            }

            else {

                showAddReminderDialog.dismiss()

            }


        }


        var isDetails=false

        showAddReminderBinding.reminderDetails.setOnClickListener {

            when(isDetails){

                true->{

                    isDetails=false

                    showAddReminderBinding.reminderDetailsLayout.visibility= View.GONE

                    showAddReminderBinding.reminderDetailsIcon.setImageResource(R.drawable.round_keyboard_arrow_down_24)


                }
                false->{

                    isDetails=true

                    showAddReminderBinding.reminderDetailsLayout.visibility= View.VISIBLE

                    showAddReminderBinding.reminderDetailsIcon.setImageResource(R.drawable.round_keyboard_arrow_up_24)


                }

            }

        }




        val calendarDate= Calendar.getInstance()
        val datePicker= DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendarDate.set(Calendar.YEAR,year)
            calendarDate.set(Calendar.MONTH,month)
            calendarDate.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateLabel(calendarDate,showAddReminderBinding)
        }
        val calendarTime= Calendar.getInstance()
        val timePicker=TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
            calendarTime.set(Calendar.HOUR_OF_DAY,hourOfDay)
            calendarTime.set(Calendar.MINUTE,minute)
            calendarTime.timeZone= TimeZone.getDefault()
            updateTimeLabel(calendarTime,showAddReminderBinding)
        }
        showAddReminderBinding.reminderDateLayout.setOnClickListener {
            DatePickerDialog(this,datePicker,
                calendarDate.get(Calendar.YEAR),
                calendarDate.get(Calendar.MONTH),
                calendarDate.get(Calendar.DAY_OF_MONTH)).show()
        }
        showAddReminderBinding.reminderTimeLayout.setOnClickListener {
            TimePickerDialog(this,timePicker,
                calendarTime.get(Calendar.HOUR_OF_DAY),
                calendarDate.get(Calendar.MINUTE),
                false).show()
        }

        showAddReminderBinding.reminderDateChecked.setOnCheckedChangeListener { compoundButton, b ->

            if (showAddReminderBinding.reminderDate.text==""){

                showAddReminderBinding.reminderDateChecked.isChecked=false

                Toast.makeText(this, "Select Date", Toast.LENGTH_SHORT).show()

            }


        }


        showAddReminderBinding.reminderTimeChecked.setOnCheckedChangeListener { compoundButton, b ->

            if (showAddReminderBinding.reminderTime.text==""){

                showAddReminderBinding.reminderTimeChecked.isChecked=false

                Toast.makeText(this, "Select Time", Toast.LENGTH_SHORT).show()

            }


        }


        showAddReminderBinding.reminderTitle.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {



            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (p0?.length==0){

                    showAddReminderBinding.reminderAdd.isEnabled=false

                    showAddReminderBinding.reminderAdd.setTextColor(resources.getColor(R.color.light_blue))

                }

                else{

                    showAddReminderBinding.reminderAdd.isEnabled=true

                    showAddReminderBinding.reminderAdd.setTextColor(resources.getColor(R.color.blue))

            }



            }

            override fun afterTextChanged(p0: Editable?) {



            }


        })




        showAddReminderBinding.reminderAdd.setOnClickListener {


            uploadData(showAddReminderBinding,showAddReminderDialog)

        }



    }


    private fun updateDateLabel(
        calendarDate:Calendar,
        showAddReminderBinding: AddReminderDialogBinding)
    {
        showAddReminderBinding.reminderDate.visibility= View.VISIBLE
        val day = SimpleDateFormat("dd").format(calendarDate.time) // always 2 digits
        val month = SimpleDateFormat("MM").format(calendarDate.time) // always 2 digits
        val year = SimpleDateFormat("yyyy").format(calendarDate.time) // 4 digit year
        showAddReminderBinding.reminderDate.text = "${day}/${month}/${year}"
    }

    private fun updateTimeLabel(
        calendarTime: Calendar,
        showAddReminderBinding: AddReminderDialogBinding
    ) {
        val format=SimpleDateFormat("hh:mm aa")
        val time= format.format(calendarTime.time)
        showAddReminderBinding.reminderTime.visibility= View.VISIBLE
        showAddReminderBinding.reminderTime.text= time
    }
    private fun uploadData(showAddReminderBinding: AddReminderDialogBinding,showAddReminderDialog:BottomSheetDialog) {

        val documentReference: DocumentReference =
            FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
                .collection("myReminders").document()

        val data=ReminderModel()

        data.reminderId=documentReference.id

        data.reminderTitle=showAddReminderBinding.reminderTitle.text.toString()

        data.reminderDescription=showAddReminderBinding.reminderDescription.text.toString()


        if (showAddReminderBinding.reminderDateChecked.isChecked){

            data.reminderDate=convertDateToLong(showAddReminderBinding.reminderDate.text.toString()+" "+showAddReminderBinding.reminderTime.text.toString())

        }
        else{

            data.reminderDate= Date().time

        }

        data.reminderCompleted= false

        documentReference.set(data).addOnSuccessListener {

            showAddReminderDialog.dismiss()


        }


    }


}