package com.example.uaspaba

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView

class DetailItemActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_item)

        val intentItemData = intent.getParcelableExtra<ItemData>("detailData")

        val titleForName = findViewById<TextView>(R.id.titleForName)
        val titleForDate = findViewById<TextView>(R.id.titleForDate)
        val titleForTotal = findViewById<TextView>(R.id.titleForTotal)
        val titleForDesc = findViewById<TextView>(R.id.titleForDesc)
        val titleForCategory = findViewById<TextView>(R.id.titleForCategory)

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)

        val btnEdit = findViewById<Button>(R.id.btnEdit)

        if (intentItemData != null) {
            if (intentItemData.type == "income") {
                titleForName.text = "Nama Pemasukan"
                titleForDate.text = "Tanggal Pemasukan"
                titleForTotal.text = "Jumlah Pemasukan"
                titleForDesc.text = "Deskripsi / Detail Pemasukan"
                titleForCategory.text = "Kategori Pemasukan"
            }

            tvName.text = intentItemData.title.toString()
            val date = intentItemData.date.toString().split("-").toTypedArray()
            val month = arrayListOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            tvDate.text = "${date[0]} ${month[date[1].toInt() - 1] } ${date[2]}"
            tvTotal.text = "Rp. ${intentItemData.value.toString()}"
            tvDesc.text = intentItemData.description.toString()
            tvCategory.text = intentItemData.category.toString()

        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, InputDataActivity::class.java)
            intent.putExtra("detailData", intentItemData)
            startActivityForResult(intent, 12)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

}