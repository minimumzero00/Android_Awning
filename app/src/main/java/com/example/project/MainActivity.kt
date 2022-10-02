package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //private val mbtnGoBluetooth: Button? = null

//    onCreate() : Activity가 실행되면서 최초 실행되는 메소드
//    onStart() : Activity가 화면으로 보일 때(만들어질 때) 실행되는 메소드
//    onPause() : Activity를 떠나는 경우 실행되는 메소드
//    onResume() : Activity가 시작되면 실행되는 메소드, onStart() 다음으로 실행됨
//    onStop() : Activity가 화면에 보이지 않을 때(ex. 홈키 누른 경우) 실행되는 메소드
//    onDestroy() : Activity가 메모리에서 제거될 때(ex. 종료) 실행되는 메소드

    private lateinit var binding: ActivityMainBinding //(추가)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main)//(원본)
        //setContentView(R.layout.activity_bluetooth)//(수정) 블루투스 가져오기
        setContentView(binding.root)//(추가)

        binding.viewpager.apply{ //(추가)
            adapter = MyPagerAdapter(context as FragmentActivity)
            setPageTransformer(ZoomOutPageTransformer())//화면 이벤트:축소
        }

    }
}