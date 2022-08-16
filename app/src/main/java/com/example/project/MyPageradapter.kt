package com.example.project

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa){
    //private val  NUM_PAGFS =3
    private val  NUM_PAGFS =2

    override fun getItemCount(): Int = NUM_PAGFS

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> {Fragment01.newInstance("","")}
            else -> {Fragment02.newInstance("","")}
            //else -> {Fragment01.newInstance("Page3","")}
        }
    }
}