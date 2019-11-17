package com.example.taoti.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class AutoFitTextureView extends TextureView {

    private static final String TAG = "AutoFitTextureView";

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitTextureView(Context context) {
        super(context,null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 是设置比例，而不是具体的值（2,3）和（4,6）结果是一样的
     * @param width
     * @param height
     */
    public void setAspectRatio(int width,int height){
        if(width<0||height<0){
            throw new IllegalArgumentException("长度不能小于零");
        }
        mRatioWidth=width;
        mRatioHeight=height;
        requestLayout();//当view的宽高发生了变化，调用requestLayout对view重新布局，
                        // 会向上递归到顶级View中，后面会执行OnMeasure,OnLayout.注意与invalidate区分
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(0==mRatioHeight||0==mRatioWidth){
            setMeasuredDimension(width,height);
        }else{
            if(width<height*mRatioWidth/mRatioHeight){
                setMeasuredDimension(height*mRatioWidth/mRatioHeight,height);
            }else{
                setMeasuredDimension(width,width*mRatioHeight/mRatioWidth);
            }
        }
    }
}
