package com.example.bookhub.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import com.example.bookhub.model.Book
import com.example.bookhub.util.ConnectionMananger
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var bookName: TextView
    lateinit var bookAuthor: TextView
    lateinit var bookPrice: TextView
    lateinit var bookRating: TextView
    lateinit var bookImage: ImageView
    lateinit var bookDesc: TextView
    lateinit var btnAddToFav: Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout

    lateinit var toolbar: androidx.appcompat.widget.Toolbar


    var bookId: String? = "100"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        bookName = findViewById(R.id.txtBookName)
        bookAuthor = findViewById(R.id.txtBookAuthor)
        bookPrice = findViewById(R.id.txtBookPrice)
        bookRating = findViewById(R.id.txtBookRating)
        bookImage = findViewById(R.id.imgBookImage)
        bookDesc = findViewById(R.id.txtBookDesc)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"



        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            Toast.makeText(this@DescriptionActivity,
                "Some unexpected error occurred",
                Toast.LENGTH_SHORT).show()
        }
        if (bookId == "100") {
            finish()
            Toast.makeText(this@DescriptionActivity,
                "Some unexpected error occurred",
                Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id", bookId)

        if (ConnectionMananger().checkConnectivity(this@DescriptionActivity)) {

            val jsonRequest =
                object : JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {
                    try {

                        progressLayout.visibility = View.GONE

                        val success = it.getBoolean("success")
                        if (success) {

                            val bookJsonObject = it.getJSONObject("book_data")

                            val bookImageUrl = bookJsonObject.getString("image")
                            Picasso.get().load(bookJsonObject.getString("image"))
                                .error(R.drawable.default_book_img).into(bookImage)
                            bookName.text = bookJsonObject.getString("name")
                            bookAuthor.text = bookJsonObject.getString("author")
                            bookPrice.text = bookJsonObject.getString("price")
                            bookRating.text = bookJsonObject.getString("rating")
                            bookDesc.text = bookJsonObject.getString("description")

                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                bookName.text.toString(),
                                bookAuthor.text.toString(),
                                bookPrice.text.toString(),
                                bookRating.text.toString(),
                                bookDesc.text.toString(),
                                bookImageUrl
                            )

                            val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                            val isFav = checkFav.get()

                            if (isFav) {
                                btnAddToFav.text = "Remove from favorites"
                                val favColor =
                                    ContextCompat.getColor(applicationContext, R.color.primary)
                                btnAddToFav.setBackgroundColor(favColor)
                            } else {
                                btnAddToFav.text = "Add to favorites"
                                val noFavColor = ContextCompat.getColor(applicationContext,
                                    R.color.colorFavorite)
                                btnAddToFav.setBackgroundColor(noFavColor)
                            }

                            btnAddToFav.setOnClickListener {

                                if (!(DBAsyncTask(applicationContext, bookEntity, 1).execute()
                                          .get())
                                ) {

                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                    val result = async.get()

                                    if (result) {

                                        Toast.makeText(this@DescriptionActivity,
                                            "Book added to favorites",
                                            Toast.LENGTH_SHORT).show()
                                        btnAddToFav.text = "Remove From favorites"
                                        val noFavColor = ContextCompat.getColor(applicationContext,
                                            R.color.teal_200)
                                        btnAddToFav.setBackgroundColor(noFavColor)

                                    } else {

                                        Toast.makeText(this@DescriptionActivity,
                                            "Some Error Occurred",
                                            Toast.LENGTH_SHORT).show()

                                    }


                                } else{

                                    val async = DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                    val result = async.get()

                                    if (result){

                                        Toast.makeText(this@DescriptionActivity, "Book Removed from favorites", Toast.LENGTH_SHORT).show()
                                        btnAddToFav.text = "Add to Favorites"
                                        val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavorite)
                                        btnAddToFav.setBackgroundColor(favColor)

                                    } else{

                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some error Occurred",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }

                                }

                            }


                        } else {
                            Toast.makeText(this@DescriptionActivity,
                                "Some error occurred",
                                Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: JSONException) {
                        Toast.makeText(this@DescriptionActivity,
                            "Some try-catch error occurred",
                            Toast.LENGTH_SHORT).show()

                    }


                }, Response.ErrorListener {

                    Toast.makeText(this@DescriptionActivity, "Volley error $it", Toast.LENGTH_SHORT)
                        .show()

                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "9bf534118365f1"
                        return headers
                    }

                }

            queue.add(jsonRequest)

        } else {

            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")

            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()


        }


    }


    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        /*
            Mode 1 -> Check DB if the book is favorite or not
            Mode 2 -> Save the book into the DB as favorites
            Mode 3 -> Remove the favorite book
        */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {

                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null

                }

                2 -> {

                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true

                }

                3 -> {

                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true

                }

            }


            return false
        }

    }
}