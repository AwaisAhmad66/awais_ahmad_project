package com.example.awais.chatapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
//accessing the fragments of the chats and Contacts
public class tabsAccessAdopter extends FragmentPagerAdapter {
    public tabsAccessAdopter(FragmentManager fm) {
        super( fm );
    }
//getting fragment items
    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                ChatsFragment chatfragment = new ChatsFragment();
                return chatfragment;
            case 1:
                ContactsFragment contactfragment = new ContactsFragment();
                return contactfragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

                default:
                 return null;
        }
    }
//the number of the fragments
    @Override
    public int getCount() {
        return 3;
    }

    //getting the fragments title

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Contacts";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
