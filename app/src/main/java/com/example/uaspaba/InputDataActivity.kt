package com.example.uaspaba

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class InputDataActivity : AppCompatActivity() {

    companion object {
        var incomeCategory = arrayListOf<String>()
        var outcomeCategory = arrayListOf<String>()
        var typeItems = arrayListOf("pemasukan", "pengeluaran")
    }

    lateinit var sp: SharedPreferences
    val db = Firebase.firestore

    var categoryItems = arrayListOf<String>()

    lateinit var name: String
    var value: Int = 0
    lateinit var description: String
    lateinit var datePicker: EditText
    lateinit var autocompleteTextViewType: AutoCompleteTextView
    lateinit var autoCompleteTextView: AutoCompleteTextView
    lateinit var category: String
    lateinit var type: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_data)

        sp = getSharedPreferences("dataSP", Context.MODE_PRIVATE)

        val gson = Gson()
        var isisp = sp.getString("incomeSpItems", null)

        if (isisp != null) {
            val type = object: TypeToken<ArrayList<String>>() {}.type
            incomeCategory = gson.fromJson(isisp, type)
        } else {
            incomeCategory.add("gaji bulanan")
        }

        isisp = sp.getString("outcomeSpItems", null)
        if (isisp != null) {
            val type = object: TypeToken<ArrayList<String>>() {}.type
            outcomeCategory = gson.fromJson(isisp, type)
        } else {
            outcomeCategory.add("belanja bulanan")
            outcomeCategory.add("biaya lain-lain")
        }
        saveDataToSp()



        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)

        autocompleteTextViewType = findViewById(R.id.autoCompleteTextViewType)
        loadCategoryPicker(typeItems, autocompleteTextViewType)






        autocompleteTextViewType.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, rowId ->
                if (parent.getItemAtPosition(position) as String == "pemasukan") {
                    type = "income"
                    categoryItems = incomeCategory
                } else {
                    type = "outcome"
                    categoryItems = outcomeCategory
                }
                loadCategoryPicker(categoryItems, autoCompleteTextView)
            }


        datePicker = findViewById(R.id.datePicker)
        datePicker.setOnClickListener {
            val myCalendar = Calendar.getInstance()
            
            val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = "dd-MM-yyyy"
                val sdf = SimpleDateFormat(dateFormat, Locale.UK)
                datePicker.setText(sdf.format(myCalendar.time))
            }

            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }


        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, rowId ->
                category = parent.getItemAtPosition(position) as String
            }


        val inputNewCategoryBtn = findViewById<Button>(R.id.inputNewCategoryBtn)
        inputNewCategoryBtn.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.new_category_dialog, null)

            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Kategori Baru")

            val mAlertDialog = mBuilder.show()

            var i = false

            mDialogView.findViewById<RadioGroup>(R.id.radioButtonGroup).setOnCheckedChangeListener { group, checkedId ->
                i = checkedId == R.id.radioButtonIncome
            }

            mDialogView.findViewById<Button>(R.id.newCategorySubmitBtn).setOnClickListener {
                val newCat = mDialogView.findViewById<EditText>(R.id.etNewCategory).text.toString()
                if (i) {
                    if (incomeCategory.contains(newCat.lowercase())) {
                        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning")
                            .setContentText("Kategori $newCat sudah ada dalam list kategori pemasukan Anda.")
                            .show()
                    } else {
                        incomeCategory.add(newCat.lowercase())
                        saveDataToSp()
                        Toast.makeText(this, "Data berhasil ditambahkan ke kategori pemasukan", Toast.LENGTH_LONG).show()
                    }
                } else {
                    if (outcomeCategory.contains(newCat.lowercase())) {
                        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Warning")
                            .setContentText("Kategori $newCat sudah ada dalam list kategori pengeluaran Anda.")
                            .show()
                    } else {
                        outcomeCategory.add(newCat.lowercase())
                        saveDataToSp()
                        Toast.makeText(this, "Data berhasil ditambahkan ke kategori pengeluaran", Toast.LENGTH_LONG).show()
                    }
                }
                mAlertDialog.dismiss()
                loadCategoryPicker(categoryItems, autoCompleteTextView)
            }

            mDialogView.findViewById<Button>(R.id.newCategoryCancelBtn).setOnClickListener {
                mAlertDialog.dismiss()
            }


        }

        val submitBtn = findViewById<Button>(R.id.submitBtn)


        val intentItemData = intent.getParcelableExtra<ItemData>("detailData")
        if (intentItemData != null) {
            submitBtn.text = "Ubah"
            findViewById<EditText>(R.id.etName).setText(intentItemData.title)
            findViewById<EditText>(R.id.etValue).setText(intentItemData.value.toString())
            findViewById<EditText>(R.id.etDescription).setText(intentItemData.description)
            findViewById<EditText>(R.id.datePicker).setText(intentItemData.date)
            category = intentItemData.category.toString()
            type = intentItemData.type.toString()

            autoCompleteTextView.setText(intentItemData.category.toString())
        }


        submitBtn.setOnClickListener {
            name = findViewById<EditText>(R.id.etName).text.toString()
            value = findViewById<EditText>(R.id.etValue).text.toString().toInt()
            description = findViewById<EditText>(R.id.etDescription).text.toString()

            if (submitBtn.text == "Submit") {
                uploadDataToDb()
            } else {
                if (intentItemData != null) {
                    editDataToDb(intentItemData.id.toString())
                } else {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Mohon Coba lagi")
                        .show()
                }
            }

        }


    }

    private fun loadCategoryPicker(items: MutableList<String>, target: AutoCompleteTextView) {
        val arrayAdapter = ArrayAdapter(this, R.layout.category_dropdown_item, items)
        target.setAdapter(arrayAdapter)
    }

    private fun editDataToDb(id: String) {
        db.collection("finance").document(id)
            .update("title", name,
            "description", description,
                "value", value,
                "date", datePicker.text.toString(),
                "category", category,
                "type", type)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil diubah", Toast.LENGTH_LONG).show()
                val eIntent = Intent(this@InputDataActivity, DetailItemActivity::class.java).apply {
                }
                setResult(Activity.RESULT_OK, eIntent)
                finish()
            }
            .addOnFailureListener { exception ->

                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Error: ${exception}")
                    .show()

            }
    }

    private fun uploadDataToDb() {
        val financeData = hashMapOf(
            "title" to name,
            "description" to description,
            "value" to value,
            "date" to datePicker.text.toString(),
            "category" to category,
            "type" to type
        )

        db.collection("finance")
            .add(financeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil diunggah", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Error: ${exception}")
                    .show()
            }
    }

    private fun saveDataToSp() {
        val editor = sp.edit()
        val gson = Gson()
        var json = gson.toJson(incomeCategory)
        editor.putString("incomeSpItems", json)
        json = gson.toJson(outcomeCategory)
        editor.putString("outcomeSpItems", json)
        editor.apply()
    }

}