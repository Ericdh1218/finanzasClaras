package com.example.finanzasclaras;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new tab_personas();
        return new tab_tarjetas();
    }

    @Override
    public int getItemCount() { return 2; }
}