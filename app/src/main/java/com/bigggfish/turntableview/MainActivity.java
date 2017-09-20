package com.bigggfish.turntableview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LuckyPlateView mLuckyPlateView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLuckyPlateView = (LuckyPlateView) findViewById(R.id.luck_plate_view);

        mLuckyPlateView.setOnBtnClickListener(new LuckyPlateView.OnBtnClickListener() {
            @Override
            public void onClick() {
                mLuckyPlateView.startRotating(1);
            }
        });

        mLuckyPlateView.setOnRotatingStopListener(new LuckyPlateView.OnRotatingStopListener() {
            @Override
            public void onStop(int stopPosition) {
                Toast.makeText(MainActivity.this, "恭喜您抽中了"+stopPosition+"号奖品", Toast.LENGTH_SHORT).show();
            }
        });

        mLuckyPlateView.setItemTextStrList(getStrList());
        mLuckyPlateView.setItemBitmapList(getBitmapList());

    }

    private List<String> getStrList(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(int i=0; i<8; i++){
            arrayList.add("POSITION" + i);
        }
        return arrayList;
    }

    private List<Bitmap> getBitmapList(){
        ArrayList<Bitmap> arrayList = new ArrayList<>();
        for(int i=0; i<8; i++){
            if(i%2==0)
                arrayList.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
            else
                arrayList.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        }
        return arrayList;
    }
}
