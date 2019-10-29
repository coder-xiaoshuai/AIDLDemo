// IOnNewBookArrivedListener.aidl
package com.example.aidldemo;
import com.example.aidldemo.Book;

// Declare any non-default types here with import statements

interface IOnNewBookArrivedListener {
   void onNewBookArrived(in Book newBook);
}
