package hxz.www.commonbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import hxz.www.commonbase.R;

/**
 * @date:2019-08-20
 * @author:andy 自定义圆形view
 */
public class HxzImageView extends AppCompatImageView {


    private static final String TAG = "HxzImageView";
    private Paint mPaint;
    private int type;
    private int xRound;
    private int realSize;
    private float leftTopRound, rightTopRound, leftBottomRound, rightBottomRound;
    private static final int TYPE_CIRCLE = 1;
    private static final int TYPE_ROUND = 2;


    public HxzImageView(Context context) {
        this(context, null);
    }

    public HxzImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.hxzImageview);
            xRound = a.getDimensionPixelSize(R.styleable.hxzImageview_XRound, 20);
            leftTopRound = a.getDimensionPixelSize(R.styleable.hxzImageview_leftTopRound, xRound);
            leftBottomRound = a.getDimensionPixelSize(R.styleable.hxzImageview_leftBottomRound, xRound);
            rightTopRound = a.getDimensionPixelSize(R.styleable.hxzImageview_rightTopRound, xRound);
            rightBottomRound = a.getDimensionPixelSize(R.styleable.hxzImageview_rightBottomRound, xRound);

            type = a.getInt(R.styleable.hxzImageview_type, 2);
            if (type == TYPE_CIRCLE) {
                setScaleType(ScaleType.CENTER_CROP);
            }
            a.recycle();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Drawable mDrawable = getDrawable();
        Matrix mDrawMatrix = getImageMatrix();
        if (mDrawable == null) {
            return;
        }

        if (mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) {
            return;
        }

        if (mDrawMatrix == null && getPaddingTop() == 0 && getPaddingLeft() == 0) {
            mDrawable.draw(canvas);
        } else {
            final int saveCount = canvas.getSaveCount();
            canvas.save();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (getCropToPadding()) {
                    final int scrollX = getScrollX();
                    final int scrollY = getScrollY();
                    canvas.clipRect(scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
                            scrollX + getRight() - getLeft() - getPaddingRight(),
                            scrollY + getBottom() - getTop() - getPaddingBottom());
                }
            }
            canvas.translate(getPaddingLeft(), getPaddingTop());
            //当为圆形模式的时候
            if (type == TYPE_CIRCLE) {
                Bitmap bitmap = drawable2Bitmap(mDrawable);
                mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                canvas.drawCircle(realSize / 2, realSize / 2, realSize / 2, mPaint);
            } else if (type == TYPE_ROUND) {
                //当为圆角模式的时候
                Bitmap bitmap = drawable2Bitmap(mDrawable);
                mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
                canvas.drawPath(drawRound(), mPaint);
            } else {
                if (mDrawMatrix != null) {
                    canvas.concat(mDrawMatrix);
                }
                mDrawable.draw(canvas);
            }
            canvas.restoreToCount(saveCount);
        }
    }

    private Path drawRound() {

        Path path = new Path();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int width = getWidth();
        int height = getHeight();
        //画左上角的圆角
        float[] rids = {leftTopRound, leftTopRound, rightTopRound, rightTopRound, rightBottomRound, rightBottomRound, leftBottomRound, leftBottomRound,};

        /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii长度必须为8*/
        path.addRoundRect(new RectF(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom), rids, Path.Direction.CW);
        path.close();
        return path;
    }

    /**
     * drawable转换成bitmap
     */
    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        getRealSize();
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //根据传递的scaletype获取matrix对象，设置给bitmap
        Matrix matrix = getImageMatrix();
        if (matrix != null) {
            canvas.concat(matrix);
        }
        drawable.draw(canvas);
        return bitmap;
    }

    private int getRealSize() {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int width = getWidth();
        int height = getHeight();
        int realWidth = width - paddingLeft - paddingRight;
        int realHeight = height - paddingTop - paddingBottom;
        if (realWidth > realHeight) {
            realSize = realHeight;
        } else {
            realSize = realWidth;
        }
        return realSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 当模式为圆形模式的时候，我们强制让宽高一致
         */
        if (type == TYPE_CIRCLE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int result = Math.min(getMeasuredHeight(), getMeasuredWidth());
            setMeasuredDimension(result, result);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * @param leftTop     左上角圆角度数
     * @param leftBottom  左下角圆角度数
     * @param rightTop    右上角圆角度数
     * @param rightBottom 右下角圆角度数
     */
    public void setRound(float leftTop, float leftBottom, float rightTop, float rightBottom) {
        this.leftTopRound = leftTop;
        this.leftBottomRound = leftBottom;
        this.rightTopRound = rightTop;
        this.rightBottomRound = rightBottom;
        invalidate();
    }


    public void setType(int type) {
        this.type = type;
        invalidate();
    }

}
