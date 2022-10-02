package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.project.databinding.ActivityMainBinding

class FragmentActivity: AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding //(추가)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main)//(원본)
        setContentView(binding.root)//(추가)

        binding.viewpager.apply{ //(추가)
            adapter = MyPagerAdapter2(context as FragmentActivity)
            setPageTransformer(ZoomOutPageTransformer())//화면 이벤트:축소
        }

    }
}