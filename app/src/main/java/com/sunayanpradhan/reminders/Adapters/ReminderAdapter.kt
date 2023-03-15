package com.sunayanpradhan.reminders.Adapters

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sunayanpradhan.reminders.Models.ReminderModel
import com.sunayanpradhan.reminders.databinding.ReminderLayoutBinding
import java.text.SimpleDateFormat


class ReminderAdapter(options: FirestoreRecyclerOptions<ReminderModel>):FirestoreRecyclerAdapter<ReminderModel,ReminderAdapter.ViewHolder>(options) {


    class ViewHolder(val binding: ReminderLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= ReminderLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ReminderModel) {

        holder.binding.reminderTitle.text=model.reminderTitle

        if (model.reminderDescription==""){

            holder.binding.reminderDescription.visibility= View.GONE

        }
        else {

            holder.binding.reminderDescription.visibility= View.VISIBLE

        }

        holder.binding.reminderDescription.text = model.reminderDescription

        holder.binding.reminderTime.text=SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(model.reminderDate)

        holder.binding.reminderChecked.isChecked=false

        var isChecked=false

        holder.binding.reminderChecked.setOnClickListener {

            if (!isChecked){

                holder.binding.reminderChecked.isChecked=true

                isChecked=true

            }
            else{

                holder.binding.reminderChecked.isChecked=false

                isChecked= false

            }

            val interval:Long = 3000

            val handler = Handler()

            val runnable =
                Runnable {

                    if (holder.binding.reminderChecked.isChecked){

                        FirebaseFirestore.getInstance().collection("reminders").document(FirebaseAuth.getInstance().uid.toString())
                            .collection("myReminders").document(model.reminderId).set(

                                ReminderModel(
                                    model.reminderId,
                                    model.reminderTitle,
                                    model.reminderDescription,
                                    model.reminderDate,
                                    true
                                )

                            )



                    }


                }

            handler.postAtTime(runnable, System.currentTimeMillis()+interval)
            handler.postDelayed(runnable,interval)


        }


    }



}