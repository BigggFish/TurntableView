package com.bigggfish.turntableview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;
import java.util.Map;


/**
 * 签到领取流量转盘控件
 * Created by bigggfish on 2017/4/5.
 */

public class LuckyPlateView extends FrameLayout {

    public static final int MODE_PLATE_ROTATING = -1;//盘转动
    public static final int MODE_INDICATOR_ROTATING = -2;//中间指针转动

    private int mItemCount = 10;
    private long delayMillis = 5000L;//停止转动时间(非准确)
    private int mTurnsNum = 10;//转动的圈数
    private int stopPosition = 0;//停止指向的盘块下标
    private String mBtnText = "";
    private Button btnSignIn;//签到按钮
    private TurntableView turntableView;//转盘
    private OnBtnClickListener onBtnClickListener;//点击中间签到按键
    private OnRotatingStopListener onRotatingStopListener;//停止转动监听
    private ObjectAnimator animatorDescending;//降速转动
    private int rotatingMode = MODE_PLATE_ROTATING;//转动模式 指针转动or盘转动

    public LuckyPlateView(@NonNull Context context) {
        this(context, null);
    }

    public LuckyPlateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initChildView(context);
    }

    /**
     * 初始化子View
     */
    private void initChildView(Context context) {
        btnSignIn = new Button(context);
        LayoutParams btnLp = new LayoutParams(DisplayUtils.dp2px(context, 124), DisplayUtils.dp2px(context, 124));
        btnLp.gravity = Gravity.CENTER;
        btnSignIn.setLayoutParams(btnLp);
        btnSignIn.setText(mBtnText);
        btnSignIn.setTextColor(0xfffe511c);
        btnSignIn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        btnSignIn.setBackgroundResource(R.drawable.btn_luck_draw);
        btnSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBtnClickListener != null) {
                    onBtnClickListener.onClick();
                }
            }
        });

        turntableView = new TurntableView(context);
       // FrameLayout.LayoutParams turntableViewLp = new LayoutParams(DisplayUtils.dp2px(context, 172), DisplayUtils.dp2px(context, 172));
        LayoutParams turntableViewLp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        turntableViewLp.gravity = Gravity.CENTER;
        turntableView.setLayoutParams(turntableViewLp);
        turntableView.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        turntableView.setOnRotationListener(
                new TurntableView.OnRotationListener() {
                    @Override
                    public void onStop(int currentItem) {
                        if (onRotatingStopListener != null) {
                            onRotatingStopListener.onStop(currentItem);
                        }
                    }
                }
        );
        addView(turntableView);
        addView(btnSignIn);
    }

    private void initRotationAnim() {
        //设置指针转动的动画
        animatorDescending = ObjectAnimator.ofFloat(btnSignIn, "rotation", 0f, 360f * mTurnsNum + (360 / mItemCount) * stopPosition);
        animatorDescending.setInterpolator(new DecelerateInterpolator(1f));
        animatorDescending.setDuration(delayMillis);
        animatorDescending.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onRotatingStopListener != null) {
                    onRotatingStopListener.onStop(stopPosition);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void startRotating(int stopPosition) {
        setStopPosition(stopPosition);
        if (rotatingMode == MODE_PLATE_ROTATING) {//转盘转模式
            turntableView.startRotation();
        } else if (rotatingMode == MODE_INDICATOR_ROTATING) {//指针转模式
            initRotationAnim();
            animatorDescending.start();
        } else {
            throw new IllegalArgumentException("旋转模式必须等于 -1 或 -2");
        }
    }

    /**
     * 设置是否能点击
     * @param clickable
     */
    public void setStartBtnClickable(boolean clickable){
        btnSignIn.setClickable(clickable);
    }

    public interface OnBtnClickListener {
        void onClick();
    }

    public interface OnRotatingStopListener {
        void onStop(int stopPosition);
    }

    public void removeAnimationListener(){
        if(turntableView != null){
            turntableView.removeAnimation();
            turntableView.clearAnimation();
        }
        this.clearAnimation();
    }

///////////////////////////SET_GET///////////////////////////////////

    /**
     * 设置中间按键字符串
     *
     * @param btnText
     */
    public void setBtnText(String btnText) {
        this.mBtnText = btnText;
        btnSignIn.setText(mBtnText);
    }

    public String getBtnText() {
        return this.mBtnText;
    }

    /**
     * 设置停止盘块下标
     */
    public void setStopPosition(int stopPosition) {
        this.stopPosition = stopPosition;
        turntableView.setStopPosition(stopPosition);
    }

    /**
     * 获取停止盘块下标
     */
    public int getStopPosition() {
        return stopPosition;
    }

    /**
     * 设置停止转动时间（非准确）
     */
    public void setStopDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    /**
     * 设置停止转动时间（非准确）
     */
    public long getStopDelayMillis() {
        return delayMillis;
    }

    /**
     * 设置item文字数组
     */
    public void setItemTextStrList(List<String> strList) {
        mItemCount = strList.size();
        turntableView.setTextList(strList);
    }

    /**
     * 设置item bitmap数组
     */
    public void setItemBitmapList(List<Bitmap> bitmapList){
        turntableView.setBitmapList(bitmapList);
    }

    public void setBitmapMap(Map<String, Bitmap> bitmapMap){
        turntableView.setBitmapMap(bitmapMap);
    }


    /**
     * 设置转动停止监听
     *
     * @param onRotatingStopListener
     */
    public void setOnRotatingStopListener(OnRotatingStopListener onRotatingStopListener) {
        this.onRotatingStopListener = onRotatingStopListener;
    }

    /**
     * 设置签到按键监听
     *
     * @param onBtnClickListener
     */
    public void setOnBtnClickListener(OnBtnClickListener onBtnClickListener) {
        this.onBtnClickListener = onBtnClickListener;
    }

    /**
     * 获取转动模式
     *
     * @return -1 = 转盘转动 -2 = 指针转动
     */
    public int getRotatingMode() {
        return rotatingMode;
    }

    /**
     * 设置转动模式 转盘转动 -1 or 指针转动 -2
     *
     * @param rotatingMode
     */
    public void setRotatingMode(int rotatingMode) {
        this.rotatingMode = rotatingMode;
    }

    /**
     * 获取转的圈数
     *
     * @return
     */
    public int getTurnsNum() {
        return mTurnsNum;
    }

    /**
     * 设置转到的圈数
     *
     * @param turnsNum
     */
    public void setTurnsNum(int turnsNum) {
        this.mTurnsNum = turnsNum;
    }

}
