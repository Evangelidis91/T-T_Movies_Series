package com.evangelidis.t_tmoviesseries

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.ArrayList

class SeasonsViewPagerAdapter(manager: FragmentManager) :
    FragmentPagerAdapter(manager) {

    override fun getCount(): Int {
        return mFragmentList.size
    }

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()


    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mFragmentTitleList[position]
    }
}