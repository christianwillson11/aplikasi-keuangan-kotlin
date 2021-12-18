package com.example.uaspaba

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tuann.floatingactionbuttonexpandable.FloatingActionButtonExpandable
import cn.pedant.SweetAlert.SweetAlertDialog




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OutcomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OutcomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var sp: SharedPreferences
    var db = Firebase.firestore

    lateinit var autoCompleteTextView: AutoCompleteTextView
    lateinit var outcomeRecyclerView: RecyclerView
    var items = arrayListOf<ItemData>()

    var showingCategoryOf = "Semua"

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var outcomeRecyclerAdapter: RecyclerView.Adapter<ItemRecyclerAdapter.ViewHolder>? = null


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
        return inflater.inflate(R.layout.fragment_outcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCategoryDataFromSp()
        loadCategoryPicker()

        outcomeRecyclerView = view.findViewById(R.id.outcomeRecyclerView)
        layoutManager = LinearLayoutManager(activity)
        outcomeRecyclerView.layoutManager = layoutManager

        outcomeRecyclerAdapter = ItemRecyclerAdapter(items)
        outcomeRecyclerView.adapter = outcomeRecyclerAdapter

        setupFab()

        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, rowId ->
                showingCategoryOf = parent.getItemAtPosition(position) as String
                loadDataFromDb("outcome", showingCategoryOf)
            }



        (outcomeRecyclerAdapter as ItemRecyclerAdapter).setOnItemClickCallback(object: ItemRecyclerAdapter.OnItemClickCallback {
            override fun onItemDelete(data: ItemData) {
                SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Apakah Anda yakin?")
                    .setContentText("Apakah Anda yakin ingin menghapus data?")
                    .setConfirmText("Hapus")
                    .setConfirmClickListener { sDialog ->
                        deleteDataDb(data.id.toString())
                        sDialog.dismissWithAnimation()
                    }
                    .setCancelButton("Cancel") { sDialog ->
                        sDialog.dismissWithAnimation()
                    }
                    .show()
            }

            override fun onItemClicked(data: ItemData) {
                val intent = Intent(activity, DetailItemActivity::class.java)
                intent.putExtra("detailData", data)
                startActivity(intent)
            }
        })

    }

    private fun setupFab() {
        val inputDataBtn = requireView().findViewById<FloatingActionButtonExpandable>(R.id.inputDataBtn)
        inputDataBtn.setOnClickListener {

            val eIntent = Intent(activity, InputDataActivity::class.java)
            startActivityForResult(eIntent, 12)
        }
        outcomeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    inputDataBtn.collapse()
                } else {
                    inputDataBtn.expand()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadCategoryDataFromSp()
        loadCategoryPicker()
        loadDataFromDb("outcome", showingCategoryOf)
    }

    private fun loadCategoryDataFromSp() {
        sp = requireContext().getSharedPreferences("dataSP", Context.MODE_PRIVATE)

        val gson = Gson()
        val isisp = sp.getString("outcomeSpItems", null)

        if (isisp != null) {
            val type = object: TypeToken<ArrayList<String>>() {}.type
            InputDataActivity.outcomeCategory = gson.fromJson(isisp, type)
            InputDataActivity.outcomeCategory.add("Semua")
        }
    }

    private fun loadCategoryPicker() {

        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.category_dropdown_item, InputDataActivity.outcomeCategory)
        autoCompleteTextView = requireView().findViewById(R.id.autoCompleteTextView)
        autoCompleteTextView.setAdapter(arrayAdapter)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadDataFromDb(whereType: String, category: String) {
        items.clear()

        if (category == "Semua") {
            val docRef = db.collection("finance").whereEqualTo("type", whereType)
            docRef.get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            items.add(
                                ItemData(
                                    document.id,
                                    document.get("title").toString(),
                                    document.get("description").toString(),
                                    document.get("category").toString(),
                                    document.get("date").toString(),
                                    document.get("type").toString(),
                                    document.get("value").toString().toInt()
                                )
                            )
                        }
                        items.sortBy { it.title }
                        outcomeRecyclerAdapter?.notifyDataSetChanged()
                    } else {
                        SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Terjadi kesalahan!")
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Error: $exception")
                        .show()
                }
        } else {
            val docRef = db.collection("finance").whereEqualTo("type", whereType).whereEqualTo("category", category)
            docRef.get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        for (document in documents) {
                            items.add(
                                ItemData(
                                    document.id,
                                    document.get("title").toString(),
                                    document.get("description").toString(),
                                    document.get("category").toString(),
                                    document.get("date").toString(),
                                    document.get("type").toString(),
                                    document.get("value").toString().toInt()
                                )
                            )
                        }
                        items.sortBy { it.title }
                        outcomeRecyclerAdapter?.notifyDataSetChanged()
                    } else {
                        SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Terjadi kesalahan!")
                            .show()
                    }
                }
                .addOnFailureListener { exception ->
                    SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Error: $exception")
                        .show()
                }
        }
    }

    private fun deleteDataDb(id: String) {
        db.collection("finance").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireActivity().applicationContext, "Data berhasil dihapus", Toast.LENGTH_LONG).show()
                loadDataFromDb("outcome", showingCategoryOf)
                SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Berhasil")
                    .setContentText("Data berhasil dihapus")
                    .setConfirmText("OK")
                    .show()
            }
            .addOnFailureListener { exception ->
                SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Data gagal dihapus")
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
         * @return A new instance of fragment OutcomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OutcomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}