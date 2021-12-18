package com.example.uaspaba

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReportFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var db = Firebase.firestore
    var items = arrayListOf<ItemData>()
    lateinit var datePicker: EditText
    lateinit var incomeReportLinearLayout: LinearLayout
    lateinit var outcomeReportLinearLayout: LinearLayout

    lateinit var tvGrandTotal: TextView

    var grandTotal = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeReportLinearLayout = view.findViewById(R.id.incomeReportLinearLayout)
        outcomeReportLinearLayout = view.findViewById(R.id.outcomeReportLinearLayout)
        tvGrandTotal = view.findViewById(R.id.tvGrandTotal)

        loadMonthlyReportFromDb("income")
        loadMonthlyReportFromDb("outcome")

        datePicker = view.findViewById(R.id.datePicker)
        datePicker.setOnClickListener {
            val myCalendar = Calendar.getInstance()

            val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = "dd-MM-yyyy"
                val sdf = SimpleDateFormat(dateFormat, Locale.UK)
                datePicker.setText(sdf.format(myCalendar.time))

                loadDataFromDb("outcome", datePicker.text.toString())
                loadDataFromDb("income", datePicker.text.toString())

            }

            DatePickerDialog(requireContext(), datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }



    }




    private fun appendTvToLinearLayout(_type: String, tvText: String, fontSize: Int) {
        val tvItems = TextView(requireActivity().applicationContext)
        tvItems.textSize = fontSize.toFloat()
        val itemName = tvText
        tvItems.text = itemName

        if (_type == "income") {
            incomeReportLinearLayout.addView(tvItems)
        } else if (_type == "outcome") {
            outcomeReportLinearLayout.addView(tvItems)
        }

    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun loadMonthlyReportFromDb(whereType: String) {
        items.clear()
        grandTotal = 0
        incomeReportLinearLayout.removeAllViews()
        outcomeReportLinearLayout.removeAllViews()
        val docRef = db.collection("finance").whereEqualTo("type", whereType)
        docRef.get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    val sdf = SimpleDateFormat("dd-MM-yyyy")
                    val currentDate = sdf.format(Date())
                    val tmp = currentDate.split("-").toTypedArray()
                    val dateOfMonth = tmp[0]
                    val month = tmp[1].toInt()
                    val year = tmp[2].toInt()
                    var total = 0
                    for (document in documents) {
                        val dateDb = document.get("date").toString().split("-").toTypedArray()

                        if (dateOfMonth == "30" || dateOfMonth == "31") {
                            if (dateDb[2].toInt() == year && dateDb[1].toInt() == month) {
                                val tvText = "${document.get("date").toString()} - ${document.get("title").toString()} - Rp.${document.get("value").toString()}"
                                total += document.get("value").toString().toInt()
                                appendTvToLinearLayout(document.get("type").toString(), tvText, 16)
                                if (whereType == "income") {
                                    grandTotal += document.get("value").toString().toInt()
                                } else {
                                    grandTotal -= document.get("value").toString().toInt()
                                }
                            }
                        } else {
                            if (month - 1 == 0) {
                                if (dateDb[2].toInt() == year-1 && dateDb[1].toInt() == 12) {
                                    val tvText = "${document.get("date").toString()} - ${document.get("title").toString()} - Rp.${document.get("value").toString()}"
                                    total += document.get("value").toString().toInt()
                                    appendTvToLinearLayout(document.get("type").toString(), tvText, 16)
                                    if (whereType == "income") {
                                        grandTotal += document.get("value").toString().toInt()
                                    } else {
                                        grandTotal -= document.get("value").toString().toInt()
                                    }
                                }
                            } else {
                                if (dateDb[2].toInt() == year && dateDb[1].toInt() == month - 1) {
                                    val tvText = "${document.get("date").toString()} - ${document.get("title").toString()} - Rp.${document.get("value").toString()}"
                                    total += document.get("value").toString().toInt()
                                    appendTvToLinearLayout(document.get("type").toString(), tvText, 16)
                                    if (whereType == "income") {
                                        grandTotal += document.get("value").toString().toInt()
                                    } else {
                                        grandTotal -= document.get("value").toString().toInt()
                                    }
                                }
                            }
                        }
                        tvGrandTotal.text = "Saldo Akhir Rp.$grandTotal"
                    }

                    appendTvToLinearLayout(whereType, "Total = Rp.$total", 20)
                } else {
                    SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Terjadi kesalahan!")
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Error: $exception")
                    .show()
            }
    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun loadDataFromDb(whereType: String, date: String) {
        grandTotal = 0
        items.clear()
        incomeReportLinearLayout.removeAllViews()
        outcomeReportLinearLayout.removeAllViews()
        val docRef = db.collection("finance").whereEqualTo("type", whereType).whereEqualTo("date", date)
        docRef.get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    if (documents.size() == 0) {
                        appendTvToLinearLayout(whereType, "Tidak ada data.", 16)
                    } else {
                        var total = 0
                        for (document in documents) {
                            val tvText = "${document.get("title").toString()} - Rp.${document.get("value").toString()}"
                            total += document.get("value").toString().toInt()
                            appendTvToLinearLayout(document.get("type").toString(), tvText, 16)
                            if (whereType == "income") {
                                grandTotal += document.get("value").toString().toInt()
                            } else {
                                grandTotal -= document.get("value").toString().toInt()
                            }
                        }
                        tvGrandTotal.text = "Saldo Akhir Rp.$grandTotal"
                        appendTvToLinearLayout(whereType,  "Total = Rp.$total", 20)
                    }


                } else {
                    SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Terjadi kesalahan!")
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Error: $exception")
                    .show()
            }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ReportFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}