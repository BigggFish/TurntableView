package com.bigggfish.turntableview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LuckyPlateView mLuckyPlateView;
    private SeekBar mSeekBar;
    private Switch mSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLuckyPlateView = (LuckyPlateView) findViewById(R.id.luck_plate_view);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSwitch = (Switch) findViewById(R.id.switch_mode);

        mLuckyPlateView.setOnBtnClickListener(new LuckyPlateView.OnBtnClickListener() {
            @Override
            public void onClick() {
                mLuckyPlateView.startRotating(2);
            }
        });

        mLuckyPlateView.setOnRotatingStopListener(new LuckyPlateView.OnRotatingStopListener() {
            @Override
            public void onStop(int stopPosition) {
                Toast.makeText(MainActivity.this, "恭喜您抽中了" + stopPosition + "号奖品", Toast.LENGTH_SHORT).show();
            }
        });

        mLuckyPlateView.setItemTextStrList(getStrList(6));
        mLuckyPlateView.setItemBitmapList(getBitmapList(6));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < 2)
                    progress = 2;
                mLuckyPlateView.setItemTextStrList(getStrList(progress));
                mLuckyPlateView.setItemBitmapList(getBitmapList(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    mLuckyPlateView.setRotatingMode(-2);
                else
                    mLuckyPlateView.setRotatingMode(-1);
            }
        });
    }

    private List<String> getStrList(int count) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            arrayList.add("POSITION" + i);
        }
        return arrayList;
    }

    private List<Bitmap> getBitmapList(int count) {
        ArrayList<Bitmap> arrayList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i % 2 == 0)
                arrayList.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
            else
                arrayList.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        }
        return arrayList;
    }
}
