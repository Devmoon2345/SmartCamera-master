package com.example.SmartCamera.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.example.SmartCamera.BottomTabView;
import com.example.SmartCamera.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BottomTabView bottomTabView;

    ViewPager viewPager;

    FragmentPagerAdapter adapter;

    ArrayList<Fragment> fragments = new ArrayList<>();

    ArrayList<BottomTabView.TabItemView> tabItemViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        bottomTabView = (BottomTabView) findViewById(R.id.bottomTabView);

        fragments = new ArrayList<>();
        fragments.add(new Fragment1());
        fragments.add(new Fragment2());
        fragments.add(new Fragment3());


        adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };

        viewPager.setAdapter(adapter);

        tabItemViews.add(new BottomTabView.TabItemView(this, "首页", R.color.colorPrimary, R.color.colorAccent, R.drawable.home, R.drawable.home));
        tabItemViews.add(new BottomTabView.TabItemView(this, "其他", R.color.colorPrimary, R.color.colorAccent, R.drawable.classify, R.drawable.classify));
        tabItemViews.add(new BottomTabView.TabItemView(this, "我的", R.color.colorPrimary, R.color.colorAccent, R.drawable.my, R.drawable.my));

        bottomTabView.setTabItemViews(tabItemViews);

        bottomTabView.setUpWithViewPager(viewPager);
    }
}
