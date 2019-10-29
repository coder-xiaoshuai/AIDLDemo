package com.example.aidldemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mBookManger: IBookManager? = null
    private val mConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            mBookManger = null
            Log.d("zs", "unbind service")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBookManger = IBookManager.Stub.asInterface(service)
            mBookManger?.registerListener(mIOnNewBookArrivedListener)
            Toast.makeText(this@MainActivity, "连接成功,可以进行查询或者添加了", Toast.LENGTH_SHORT).show()
        }
    }

    private val mIOnNewBookArrivedListener = object : IOnNewBookArrivedListener.Stub() {
        override fun onNewBookArrived(newBook: Book?) {
            runOnUiThread {
                //刷新数据
                mBookManger?.let {
                    text_show_result.text = it.bookList.toString()
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_connect_service.setOnClickListener {
            val intent = Intent(this@MainActivity, RemoteService::class.java)
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }

        btn_add_book.setOnClickListener {
            val bookId = Random().nextInt(1000)
            val book = Book(bookId, "这是一个书名$bookId")
            mBookManger?.addBook(book)
        }

        btn_query_book.setOnClickListener {
            mBookManger?.let {
                text_show_result.text = it.bookList.toString()
            }
        }

        btn_unregister_listener.setOnClickListener {
            mBookManger?.unregisterListener(mIOnNewBookArrivedListener)
        }
    }


    override fun onDestroy() {
        if (mBookManger != null && mBookManger!!.asBinder().isBinderAlive) {
            Log.d("zs", "unregister listener:$mIOnNewBookArrivedListener")
            try {
                mBookManger!!.unregisterListener(mIOnNewBookArrivedListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        unbindService(mConnection)
        super.onDestroy()
    }
}
