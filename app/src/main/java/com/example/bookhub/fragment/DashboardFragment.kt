package com.example.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.DeadObjectException
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.adapter.DashboardRecyclerAdapter
import com.example.bookhub.model.Book
import com.example.bookhub.util.ConnectionMananger
import org.json.JSONException


class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager

    val bookInfoList = arrayListOf<Book>()

    lateinit var recyclerAdapter: DashboardRecyclerAdapter

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)

        layoutManager = LinearLayoutManager(activity)

        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(activity as Context)

        val url = "http://13.235.250.119/v1/book/fetch_books/"

        if (ConnectionMananger().checkConnectivity(activity as Context)){

            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener {
                    // Response can be Handle here
                    try {

                        progressLayout.visibility = View.GONE
                        val success = it.getBoolean("success")

                        if (success){
                            val data = it.getJSONArray("data")
                            for (i in 0 until data.length()){
                                val bookJSONObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJSONObject.getString("book_id"),
                                    bookJSONObject.getString("name"),
                                    bookJSONObject.getString("author"),
                                    bookJSONObject.getString("rating"),
                                    bookJSONObject.getString("price"),
                                    bookJSONObject.getString("image")

                                )
                                bookInfoList.add(bookObject)
                                recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)

                                recyclerDashboard.adapter = recyclerAdapter

                                recyclerDashboard.layoutManager = layoutManager


                            }
                        }else {
                            Toast.makeText(activity as Context, "Some Error Occured", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: JSONException){
                        Toast.makeText(activity as Context, "Some Unexpected Error Occurred !!!", Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener {
                    // Errors can be Handle here
                    Toast.makeText(activity as Context, "Volley error Occurred", Toast.LENGTH_SHORT).show()

                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        } else {

            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found Found")
            dialog.setPositiveButton("Open Settings") {text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()

            }
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)

            }
            dialog.create()
            dialog.show()

        }




        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }


}