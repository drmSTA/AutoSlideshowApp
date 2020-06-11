package jp.techacademy.drmS.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), View.OnClickListener  {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var timer4slideShow : Timer? = null
    private var handler4imageUpdate = Handler()
    private val INTERVAL_OF_SLIDESHOW : Long = 2000
    private val DELAY_ON_SLIDESHOW : Long = 2000

    private var imageList : ArrayList<Uri> = ArrayList()
    private var imageIndexNow = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        installClickListener4buttons()

        if(haveAuthericationToAccessStorage()){
            initializeMediaList()
        }else{
            Toast.makeText(this, "Gallery画像へのアクセス権限を設定下さい", Toast.LENGTH_LONG).show()
        }
    }

    private fun installClickListener4buttons(){
        button4previous.setOnClickListener(this)
        button4playControl.setOnClickListener(this)
        button4next.setOnClickListener(this)
    }

    private fun haveAuthericationToAccessStorage() : Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                return true
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                return false
            }
        } else {
            // Android 5系以下の場合
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMediaList()
                }
        }
    }

    private fun initializeMediaList(){
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)

                imageList.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id))
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    override fun onClick(view: View) {
        if(imageList.size > 1) {
            when{
                view == button4playControl ->{
                    controlSlideShow()
                }
                view == button4next -> {
                    imageIndexNow = (++imageIndexNow)%imageList.size
                    updateImage()
                }
                view == button4previous -> {
                    // ユーザー入力等アプリ外から画像の指定を受け付ける場合には適切な validation を実行すること
                    imageIndexNow = (--imageIndexNow + imageList.size)%imageList.size
                    updateImage()
                }
            }
        }else{
            Toast.makeText(this, "Gallery画像へのアクセス権限がないか、画像が無い為表示できません", Toast.LENGTH_LONG).show()
        }
    }

    fun updateImage(){
        handler4imageUpdate.post{
            imageView4slideShow.setImageURI(imageList[imageIndexNow])
        }
    }

    fun controlSlideShow(){
        if(timer4slideShow == null){
            // スライドショー開始
            button4previous.isEnabled = false;
            button4next.isEnabled = false;
            button4playControl.text = "停止"

            timer4slideShow = Timer()
            timer4slideShow!!.schedule(object : TimerTask(){
                override fun run() {
                    imageIndexNow = ++imageIndexNow%imageList.size
                    updateImage()
                }
            }, DELAY_ON_SLIDESHOW, INTERVAL_OF_SLIDESHOW)
        }else{
            // スライドショー停止
            button4previous.isEnabled = true;
            button4next.isEnabled = true;
            button4playControl.text = "再生"

            timer4slideShow!!.cancel()
            timer4slideShow = null

        }
    }
}
