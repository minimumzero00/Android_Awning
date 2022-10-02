package com.example.project

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyPagerAdapter2(fa: FragmentActivity) : FragmentStateAdapter(fa){
    private val  NUM_PAGFS =3
    //private val  NUM_PAGFS =2

    override fun getItemCount(): Int = NUM_PAGFS

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {Fragment03.newInstance("설명1","이건 뭐야")}
            //Fragment03이라 xml이 0번과 똑같이 나올 가능성 있음
            1 -> {Fragment03.newInstance("설명2","")}
            //Fragment04 에서 return view를 했기 때문에 "설명3"이 안나옴
            else -> {Fragment04.newInstance("설명3인디","")}
        }
    }
}