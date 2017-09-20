package com.bigggfish.turntableview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 签到领取流量转盘控件。
 * <p>
 * Created by bigggfish on 2017/4/5.
 */

public class TurntableView extends View {

    //屏幕宽度
    private int mScreenWidth;
    //绘制最里面盘块的画笔
    private Paint mInnerArcPaint;
    //绘制文本的画笔
    private Paint mTextPaint;
    //绘制最外层圆环
    private Paint mOvalPaint;
    //绘制中间盘块的画笔
    private Paint mMidArcPaint;
    //绘制背景画笔
    private Paint mBgPaint;
    //最中心
    private RectF mInnerRange = new RectF();
    //中间
    private RectF mMidRange = new RectF();
    //文字重绘区域
    private Rect mTextRange = new Rect();
    //整个盘块的直径
    private int mRadius;
    //转盘的中心位置
    private int mCenterX;
    private int mCenterY;
    //View padding
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;
    //各个圆环的宽度比例。
    private float mOvalWidthScale = 32f;//最外部圆环
    private float mMidWidthScale = 0.0f;//中间圆环
    private float mInnerWidthScale = 268.0f;//中心圆环
    private float mBaseWidthScale = 300.0f;//总权重
    private float mBtnWidthScale = 115.0f;//按钮权重
    //初始角度
    private float mStartAngle = 0f;
    //停止指针指向的下标
    private int mStopPosition = 0;
    //旋转时间 ms
    private long mDelayMillis = 5000L;
    //额外转动的圈数
    private int mTurnsNum = 8;
    //默认字体大小
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            16, getResources().getDisplayMetrics());
    //item个数
    private int mItemCount = 10;
    //盘块文字描述
    private String[] mTextStrings = new String[]{};
    //盘块图片
    private Bitmap[] mBitmapArrays = null;
    //盘块文字描述
    private List<String> mTextList;
    //中心盘块的颜色
    private int[] mInnerColor = new int[]{0xFFffe464, 0xFFFFFFFF};
    //中间盘块的颜色
    private int[] mMidColor = new int[]{0xFFffe464, 0xFFFFFFFF};
    private int[] mTextColor = new int[]{0xFF846D02, 0xffff1e29};
    //转盘旋转的状态监听器
    private OnRotationListener mListener;
    private ValueAnimator valueAnimator;
    private Bitmap bg;
    private Map<String, Bitmap> mBitmapMap;
    private int defAngle;
    private Bitmap[] mRotateBitmapArray;

    public TurntableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        measureScreenSize();
        initData();
        initPaint();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public TurntableView(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingBottom = getPaddingBottom();
        mPaddingTop = getPaddingTop();

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mScreenWidth, mScreenWidth - mPaddingLeft - mPaddingRight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(heightSpecSize, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, widthSpecSize);
        }

        int width = Math.min(getMeasuredWidth() - mPaddingLeft - mPaddingRight, getMeasuredHeight() - mPaddingTop - mPaddingBottom);
        //计算padding

        //直径
        mRadius = width;
        //Math.min(width - mPaddingLeft - mPaddingRight, width - mPaddingBottom - mPaddingTop);
        //中心点, 以左上角为准。
        mCenterX = getPaddingLeft() + mRadius / 2;
        mCenterY = getPaddingTop() + mRadius / 2;
        initRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景
        drawBg(canvas);
        //绘制盘块
        float tmpAngle = mStartAngle;

        float sweepAngle = mItemCount == 0 ? 360 : 360 / mItemCount;
        //绘制最外层圆环
        canvas.drawCircle(mCenterX, mCenterY, mRadius / 2 - mRadius / 2 * (mOvalWidthScale / mBaseWidthScale) / 2, mOvalPaint);

        for (int i = 0; i < mItemCount; i++) {
            mInnerArcPaint.setColor(mInnerColor[i % 2]);
            mMidArcPaint.setColor(mMidColor[i % 2]);
            mTextPaint.setColor(mTextColor[i % 2]);
            //绘制中间盘块
            canvas.drawArc(mMidRange, tmpAngle, sweepAngle, false, mMidArcPaint);
            //绘制盘块
            canvas.drawArc(mInnerRange, tmpAngle, sweepAngle, true, mInnerArcPaint);
            //绘制文本
            if (mTextList.size() > i && mTextList.get(i) != null)
                drawText(canvas, tmpAngle, sweepAngle, mTextList.get(i));
          /*  if(mBitmapArrays != null && mBitmapArrays.length > i && mBitmapArrays[i] != null)
                drawBitmap(canvas, tmpAngle, sweepAngle, mBitmapArrays[i]);*/

            if (mBitmapArrays != null
                    && mBitmapArrays.length > i
                    && mBitmapArrays[i] != null) {
                drawBitmap2(canvas, tmpAngle, sweepAngle, mBitmapArrays[i], i);
            }
            tmpAngle += sweepAngle;
        }
    }

    //初始化部分数据
    private void initData() {
        mItemCount = mTextStrings.length;
        mTextList = Arrays.asList(mTextStrings);
    }

    //初始化画笔
    private void initPaint() {
        //初始化绘制最中心盘块的画笔
        mInnerArcPaint = new Paint();
        mInnerArcPaint.setAntiAlias(true);
        mInnerArcPaint.setDither(true);

        //初始化文字绘制画笔
        mTextPaint = new Paint();
        //mTextPaint.setColor(0xff411f11);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);

        //初始化挥着外边环画笔
        mOvalPaint = new Paint();
        mOvalPaint.setAntiAlias(true);
        mOvalPaint.setColor(0x00000000);
        mOvalPaint.setStyle(Paint.Style.STROKE);

        //初始化中间盘块画笔
        mMidArcPaint = new Paint();
        mMidArcPaint.setAntiAlias(true);
        mMidArcPaint.setStyle(Paint.Style.STROKE);
        mMidArcPaint.setDither(true);

        bg = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bg_luck_draw_dot);
        mBgPaint = new Paint();

    }

    //计算屏幕尺寸
    private void measureScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
    }

    /**
     * 根据控件大小确定绘制范围
     */
    private void initRect() {
        //设置最外围圆圈宽度
        mOvalPaint.setStrokeWidth(mRadius / 2 * (mOvalWidthScale / mBaseWidthScale));
        //设置中间圆弧宽度
        mMidArcPaint.setStrokeWidth(mRadius / 2 * (mMidWidthScale / mBaseWidthScale));
        //初始化内部盘块绘制的范围
        mInnerRange.set(mPaddingLeft + (1 - mInnerWidthScale / mBaseWidthScale) * mRadius / 2
                , mPaddingTop + (1 - mInnerWidthScale / mBaseWidthScale) * mRadius / 2
                , mPaddingLeft + (1 + mInnerWidthScale / mBaseWidthScale) * mRadius / 2
                , mPaddingTop + (1 + mInnerWidthScale / mBaseWidthScale) * mRadius / 2);
        //初始化中间盘块的范围
        mMidRange.set(mPaddingLeft + mRadius / 2 * (mOvalWidthScale / mBaseWidthScale) + mRadius / 2 * (mMidWidthScale / mBaseWidthScale) / 2
                , mPaddingTop + mRadius / 2 * (mOvalWidthScale / mBaseWidthScale) + mRadius / 2 * (mMidWidthScale / mBaseWidthScale) / 2
                , mPaddingLeft + mRadius - mRadius / 2 * (mOvalWidthScale / mBaseWidthScale) - mRadius / 2 * (mMidWidthScale / mBaseWidthScale) / 2
                , mPaddingTop + mRadius - mRadius / 2 * (mOvalWidthScale / mBaseWidthScale) - mRadius / 2 * (mMidWidthScale / mBaseWidthScale) / 2);
    }

    /*** 绘制背景
     */
    private void drawBg(Canvas canvas) {
        //canvas.drawColor(0x00000000);
        int width = getMeasuredWidth() - mPaddingLeft - mPaddingRight;
        int height = getMeasuredHeight() - mPaddingLeft - mPaddingRight;

        // Matrix类进行图片处理（缩小或者旋转）
        Matrix matrix = new Matrix();

        matrix.postScale((float) width / bg.getWidth(), (float) width / bg.getWidth());
        // 生成新的图片
        bg = Bitmap.createBitmap(bg, 0, 0, bg.getWidth(),
                bg.getHeight(), matrix, true);
        canvas.drawBitmap(bg, mPaddingLeft, mPaddingTop, mBgPaint);
    }

    /**
     * 绘制每个盘块的文本
     */
    private void drawText(Canvas canvas, float tmpAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mInnerRange, tmpAngle, sweepAngle);
        //利用水平偏移量让文字居中
        float textWidth = mTextPaint.measureText(string);
        float hOffset = (float) (mRadius * (mInnerWidthScale / mBaseWidthScale) * Math.PI / mItemCount / 2 - textWidth / 2);
        float vOffset = mRadius / 4 * ((mInnerWidthScale - mBtnWidthScale) / 2) / mBaseWidthScale + DisplayUtils.dp2px(getContext(), 8);//垂直偏移量
        canvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }

    private void drawBitmap(Canvas canvas, float tmpAngle, float sweepAngle, Bitmap bitmap, int position) {
        // 设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 6;

        float angle = (float) ((sweepAngle / 2 + tmpAngle) * (Math.PI / 180));

        int x = (int) (mCenterX + (mRadius / 2 / 2 + 16) * Math.cos(angle));
        int y = (int) (mCenterY + (mRadius / 2 / 2 + 16) * Math.sin(angle));

        // 确定绘制图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth
                / 2, y + imgWidth / 2);
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int canvasWidth = (int) Math.sqrt(bitmapHeight * bitmapHeight + bitmapWidth * bitmapWidth);
        //int canvasWidth = Math.max(bitmap.getWidth(), bitmap.getHeight()) + 16;
        Bitmap itemBitmap;
        itemBitmap = Bitmap.createBitmap(canvasWidth, canvasWidth, Bitmap.Config.ARGB_8888);
        Canvas canvasChild = new Canvas(itemBitmap);
        canvasChild.rotate(tmpAngle - defAngle, canvasWidth/2, canvasWidth/2);//tmpAngle - defAngle
        canvasChild.drawBitmap(bitmap, (canvasWidth-bitmap.getWidth())/2, (canvasWidth-bitmap.getHeight())/2, null);
        canvas.drawBitmap(itemBitmap, null, rect, null);
        if(itemBitmap.isRecycled())
            itemBitmap.recycle();
    }

    private void drawBitmap2(Canvas canvas, float tmpAngle, float sweepAngle, Bitmap bitmap, int position) {
        // 设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 8;

        float angle = (float) ((sweepAngle / 2 + tmpAngle) * (Math.PI / 180));

        //int x = (int) (mCenterX + (mRadius / 2 / 2 + 16) * Math.cos(angle));
        //int y = (int) (mCenterY + (mRadius / 2 / 2 + 16) * Math.sin(angle));

        int x =  (mCenterX);// + (mRadius / 2 / 3)
        int y =  (mCenterY - (mRadius / 2 /2) - DisplayUtils.dp2px(getContext(), 8));
        //Log.e("");

        // 确定绘制图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        canvas.save();

        canvas.rotate(tmpAngle - defAngle, mCenterX, mCenterY);
        canvas.drawBitmap(bitmap, null, rect, null);
        canvas.restore();
    }

    /*private void drawBitmap(Canvas canvas, float tmpAngle, float sweepAngle, Bitmap bitmap){
        // 设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 8;

        float angle = (float) ((sweepAngle + sweepAngle/2) * (Math.PI / 180));

        int x = (int) (mCenterX + (mRadius / 2 / 2) * Math.cos(angle));
        int y = (int) (mCenterY + (mRadius / 2 / 2));


        // 确定绘制图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth
                / 2, y + imgWidth / 2);
      *//*  Matrix matrix = new Matrix();
        matrix.setRotate(tmpAngle - defAngle);
        int a = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Log.e("====>drawBitmap", "a:" + a);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, a, a, matrix, true);
        //canvas.rotate(tmpAngle - defAngle);*//*
        Bitmap itemBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvasChild = new Canvas(itemBitmap);
        canvasChild.rotate(tmpAngle - sweepAngle , mCenterX, mCenterY);//tmpAngle - defAngle
        canvasChild.drawBitmap(bitmap, null, rect, null);
        canvas.drawBitmap(itemBitmap, 0, 0, null);
        itemBitmap.recycle();
    }*/

    /**
     * 绘制每个盘块的图片
     */
   /* private void drawBitmap(Canvas canvas, float tmpAngle, float sweepAngle, Bitmap bitmap){
        // 设置图片的宽度为直径的1/8
        int imgWidth = mRadius / 8;

        float angle = (float) ((sweepAngle/2 + tmpAngle) * (Math.PI / 180));

        int x = (int) (mCenterX + (mRadius / 2 / 2 + 16) * Math.cos(angle));
        int y = (int) (mCenterY + (mRadius / 2 / 2 + 16) * Math.sin(angle));

        // 确定绘制图片的位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth
                / 2, y + imgWidth / 2);
      *//*  Matrix matrix = new Matrix();
        matrix.setRotate(tmpAngle - defAngle);
        int a = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Log.e("====>drawBitmap", "a:" + a);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, a, a, matrix, true);
        //canvas.rotate(tmpAngle - defAngle);*//*
        Bitmap itemBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasChild = new Canvas(itemBitmap);
        canvasChild.rotate(tmpAngle, mCenterX, mCenterY);//tmpAngle - defAngle
        canvasChild.drawBitmap(bitmap, null, rect, null);
        canvas.drawBitmap(itemBitmap, 0, 0, null);
    }*/
///////////////////////////////PUBLIC//////////////////////////////////////////////////////////

    /**
     * 开始旋转
     */
    public void startRotation() {
        initAngle();
        initRotationAnimator();
        valueAnimator.start();
    }

    /**
     * 转动多长时间后停止  毫秒
     *
     * @param delayMillis
     */
    public void startRotatingDelay(long delayMillis) {
        this.mDelayMillis = delayMillis;
        startRotation();
    }

    /**
     * 初始化角度
     */
    private void initAngle() {
        mStartAngle = defAngle;
        invalidate();
    }


    /**
     * 转盘旋转状态监听器
     */
    public interface OnRotationListener {
        void onStop(int currentItem);//动画结束
    }

    private void initRotationAnimator() {
        valueAnimator = ValueAnimator.ofFloat(mStartAngle, 360f * mTurnsNum - (360 / mItemCount) * mStopPosition + mStartAngle);
        //valueAnimator.setInterpolator(new DecelerateInterpolator(1f));
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        //valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(getDelayMillis());
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null) {
                    mListener.onStop(mStopPosition);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //animation.getAnimatedValue()
                mStartAngle = ((float) animation.getAnimatedValue() + 360) % 360;
                invalidate(new Rect((int) mMidRange.left, (int) mMidRange.top, (int) mMidRange.right, (int) mMidRange.bottom));
            }
        });

    }


    public void removeAnimation() {
        if (valueAnimator != null) {
            valueAnimator.removeAllUpdateListeners();
            valueAnimator.removeAllListeners();
        }
    }

///////////////////////////////PUBLIC//////////////////////////////////////////////////////////


///////////////////////////////GET---SET///////////////////////////////////////////////////////

    /**
     * 监听转动结束
     *
     * @param listener
     */
    public void setOnRotationListener(OnRotationListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置字体大小  单位：sp
     */
    public void setTextSize(int textSize) {
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                textSize, getResources().getDisplayMetrics());
    }

    /**
     * 设置初始旋转角度
     */

    public void setStartAngle(int startAngle) {
        mStartAngle = startAngle;
    }

    public List<String> getTextList() {
        return mTextList;
    }

    private void setData(List<String> mTextList, List<Bitmap> mBitmapList) {
        this.mTextList = mTextList;
        mItemCount = mTextList.size();
        mBitmapList.toArray(mBitmapArrays);
        mTextRange.set((int) mInnerRange.left, (int) mInnerRange.top, (int) mInnerRange.right, (int) mInnerRange.bottom);
        invalidate(mTextRange);
    }

    public void setTextList(List<String> mTextList) {
        this.mTextList = mTextList;
        mItemCount = mTextList.size();
        defAngle = 360 - (360 / mItemCount / 2 + 90);
        mStartAngle = defAngle;
        mTextRange.set((int) mInnerRange.left, (int) mInnerRange.top, (int) mInnerRange.right, (int) mInnerRange.bottom);
        invalidate(mTextRange);
    }

    public void setBitmapList(List<Bitmap> mBitmapList) {
        mBitmapArrays = new Bitmap[mBitmapList.size()];
        mBitmapList.toArray(mBitmapArrays);
        mRotateBitmapArray = new Bitmap[mBitmapList.size()];
        mTextRange.set((int) mInnerRange.left, (int) mInnerRange.top, (int) mInnerRange.right, (int) mInnerRange.bottom);
        invalidate(mTextRange);
    }

    public void setBitmapMap(Map<String, Bitmap> mBitmapMap) {
        this.mBitmapMap = mBitmapMap;
        mRotateBitmapArray = new Bitmap[mBitmapMap.size()];
        mTextRange.set((int) mInnerRange.left, (int) mInnerRange.top, (int) mInnerRange.right, (int) mInnerRange.bottom);
        invalidate();
    }

    /**
     * 设置最内部盘块颜色
     *
     * @param colorList
     */
    public void setInnerColor(int[] colorList) {
        this.mInnerColor = colorList;
    }

    /**
     * 设置中间盘块颜色
     *
     * @param colorList
     */
    public void setMidColor(int[] colorList) {
        this.mMidColor = colorList;
    }

    /**
     * 设置停止指针指向转盘的下标
     */
    public void setStopPosition(int stopPosition) {
        if (stopPosition >= mItemCount) {
            throw new IllegalArgumentException("stopPosition不能大于盘块item个数");
        }
        this.mStopPosition = stopPosition;
    }

    /**
     * 获取停止指针指向转盘的下标
     */
    private int getStopPosition() {
        return mStopPosition;
    }

    /**
     * 设置转动时间
     *
     * @param delayMillis
     */
    private void setDelayMillis(long delayMillis) {
        this.mDelayMillis = delayMillis;
    }

    /**
     * 获取转动时间
     */
    private Long getDelayMillis() {
        return this.mDelayMillis;
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

    /**
     * 设置盘块图片
     *
     * @return
     */
    public Bitmap[] getBitmapArrays() {
        return mBitmapArrays;
    }

    public void setBitmapArrays(Bitmap[] bitmapArrays) {
        mBitmapArrays = bitmapArrays;
    }

///////////////////////////////GET---SET///////////////////////////////////////////////////////

}
