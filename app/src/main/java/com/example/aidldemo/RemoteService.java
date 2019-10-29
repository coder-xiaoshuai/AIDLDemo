package com.example.aidldemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteService extends Service {
    private List<Book> mBookList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListeners = new RemoteCallbackList<>();
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
    private Thread thread;

    @Override
    public void onCreate() {
        super.onCreate();
        //创建一个线程每5秒添加book
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!mIsServiceDestroyed.get()) {
                        try {
                            Thread.sleep(5 * 1000);
                            int bookId = mBookList.size() + 1;
                            Book book = new Book(bookId, "new Book #" + bookId);
                            onNewBookArrived(book);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }

                    }


                }
            });
            thread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsServiceDestroyed.set(true);
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        final int N = mListeners.beginBroadcast();
        for (int i = 0; i < N; i++) {
            IOnNewBookArrivedListener listener = mListeners.getBroadcastItem(i);
            if (listener != null) {
                listener.onNewBookArrived(book);
            }
        }
        mListeners.finishBroadcast();//和beginBroadcast必须配对使用
//        Log.d("zs", "onNewBookArrived, notify listeners:" + mListeners.size());
//        for (IOnNewBookArrivedListener mListener : mListeners) {
//            Log.d("zs", "onNewBookArrived, notify listener:" + mListener);
//            mListener.onNewBookArrived(book);
//        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private final IBookManager.Stub mBinder = new IBookManager.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            if (mBookList.add(book)) {
                Log.d("zs", "增加了一本数 ID:" + book.getBookId() + ",书名:" + book.getBookName());
            }
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            //替换为RemoteCallbackList
            mListeners.register(listener);
            Log.d("zs", "registerListener, size:" + mListeners.beginBroadcast());
            mListeners.finishBroadcast();
//            if (!mListeners.contains(listener)) {
//                mListeners.add(listener);
//            } else {
//                Log.d("zs", "listener already exists!");
//            }
//            Log.d("zs", "registerListener, size:" + mListeners.size());

        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListeners.unregister(listener);
//            if (mListeners.remove(listener)) {
//                Log.d("zs", "unregister listener succeed");
//            } else {
//                Log.d("zs", "not found listener, can not unregister");
//            }
//
            Log.d("zs", "unregisterListener, current size:" + mListeners.beginBroadcast());
            mListeners.finishBroadcast();
        }
    };

}
