package org.sportiduino.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.sportiduino.app.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabsAdapter tabsAdapter = new TabsAdapter(getChildFragmentManager());
        tabsAdapter.addFragment(new FragmentReadCard(), getString(R.string.fragment_read_card_label));
        tabsAdapter.addFragment(new FragmentWriteCard(), getString(R.string.fragment_write_card_label));
        tabsAdapter.addFragment(new FragmentStationSettings(), getString(R.string.fragment_station_settings_label));
        binding.viewPager.setAdapter(tabsAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

class TabsAdapter extends FragmentStatePagerAdapter {
    private final List<Fragment> fragmentList;
    private final List<String> fragmentTitles;

    public TabsAdapter(FragmentManager fm){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentList = new ArrayList<>();
        fragmentTitles = new ArrayList<>();
    }

    public void addFragment(Fragment f, String title) {
        fragmentList.add(f);
        fragmentTitles.add(title);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position){
        return fragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }
}

