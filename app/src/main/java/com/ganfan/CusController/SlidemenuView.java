package com.ganfan.CusController;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;

import com.ganfan.R;

/**
 * outhor: 淦翻翻
 * time:2022/4/3
 * desc:
 */
public class SlidemenuView extends ViewGroup implements View.OnClickListener {
    // private static final String TAG = "SlidemenuView";
    private int mFunction;
    private View mContentView;
    private View mEditView;
    private OnEditClickListener editClickListener;
    private TextView mReadTv;
    private TextView mDeleteTv;
    private TextView mTopTv;
    //   private int mContentLeft = 0;
    private float mDownX;
    private float mDownY;
    private Scroller mScroller;
    private float mInterceptDownX;
    private float mInterceptDownY;

    public SlidemenuView(Context context) {
        this(context, null);
    }

    public SlidemenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidemenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidemenuView);
        mFunction = a.getInt(R.styleable.SlidemenuView_function, 0x30);

        a.recycle();
        mScroller = new Scroller(context);
        //

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        //加判断，只能有一个子view
        if (childCount > 1) {
            throw new IllegalArgumentException("no more the one child");
        }

        mContentView = getChildAt(0);


        //根据属性添加子view
        mEditView = LayoutInflater.from(getContext()).inflate(R.layout.item_slide_action, this, false);
        initEditView();
        this.addView(mEditView);
        int afterCount = getChildCount();

    }

    private void initEditView() {
        mReadTv = mEditView.findViewById(R.id.read_tv);
        mDeleteTv = mEditView.findViewById(R.id.delet_tv);
        mTopTv = mEditView.findViewById(R.id.top_tv);
        mReadTv.setOnClickListener(this);
        mDeleteTv.setOnClickListener(this);
        mTopTv.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);


        //测量第一个孩子 ，也就是内容部分


        //宽度,中父控件一样宽，高度有三种情况，如果指定大小，那我们获取到它的大小，直接测量。如果是包裹内容，at_most 。如果是match_parent,那就给他大小
        LayoutParams contentLayoutParams = mContentView.getLayoutParams();
        int contentHeight = contentLayoutParams.height;
        int contentHeightMeasureSpace;


        if (contentHeight == LayoutParams.MATCH_PARENT) {
            contentHeightMeasureSpace = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        } else if (contentHeight == LayoutParams.WRAP_CONTENT) {
            contentHeightMeasureSpace = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
        } else {
            // 指定大小
            contentHeightMeasureSpace = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);
        }
        mContentView.measure(widthMeasureSpec, contentHeightMeasureSpace);
        //拿到内容部分测量以后的值
        int contentMeasureHeight = mContentView.getMeasuredHeight();
        //测量编辑部分，  3、4 高度跟内容高度一样
        int editViewSize = widthSize * 3 / 4;
        mEditView.measure(MeasureSpec.makeMeasureSpec(editViewSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(contentMeasureHeight, MeasureSpec.EXACTLY));

        //测量自己
        //宽度就是前面的宽度部和，高度和内容一样高
        setMeasuredDimension(widthSize + editViewSize, contentMeasureHeight);


    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        //摆放内容
        int mContentLeft = 0;
        int contentTop = 0;
        int contentRight = mContentLeft + mContentView.getMeasuredWidth();
        int contentBotton = contentTop + mContentView.getMeasuredHeight();
        mContentView.layout(mContentLeft, contentTop, contentRight, contentBotton);
        //摆放编辑部分
        int editViewLeft = contentRight;
        int editViewTop = contentTop;
        int editViewRight = editViewLeft + mEditView.getMeasuredWidth();
        int editViewBottom = editViewTop + mEditView.getMeasuredHeight();

        mEditView.layout(editViewLeft, editViewTop, editViewRight, editViewBottom);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        editClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (editClickListener == null) {
            return;
        }
        close();
        if (view == mReadTv) {
            editClickListener.onReadClick();
        } else if (view == mDeleteTv) {
            editClickListener.onDeleteClick();
        } else if (view == mTopTv) {
            editClickListener.onTopClick();
        }

        /*
        switch (view.getId()) {
            case R.id.read_tv:
                editClickListener.onReadClick();
                break;

            case R.id.delet_tv:
                editClickListener.onDeleteClick();
                break;

            case R.id.top_tv:
                editClickListener.onTopClick();
                break;
        }

         */

    }

    public interface OnEditClickListener {
        void onReadClick();

        void onTopClick();

        void onDeleteClick();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currX = mScroller.getCurrX();
            //没去到指定位置即可
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        }
    }

    //是否已经打开
    private boolean isOpen = false;
    private Direction mCurrentDirection = Direction.NONE;

    enum Direction {
        LEFT, RIGHT, NONE;
    }

    //mDuration 走完mEditView 4/5宽度需要的时间
    private int mMaxDuration = 800;
    private int mMinDuration = 300;

    public void open() {
        //显示出来
        //  scrollTo(mEditView.getMeasuredWidth(),0);
        int dx = mEditView.getMeasuredWidth() - getScrollX();
        int duration = (int) (dx / (mEditView.getMeasuredWidth() * 4 / 5f) * mMaxDuration);
        int absDuration = Math.abs(duration);
        if (absDuration < mMinDuration) {
            absDuration = mMinDuration;
        }

        mScroller.startScroll(getScrollX(), 0, dx, 0, absDuration);
        invalidate();
        isOpen = true;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        //隐藏
        int dx = -getScrollX();
        int duration = (int) (dx / (mEditView.getMeasuredWidth() * 4 / 5f) * mMaxDuration);

        int absDuration = Math.abs(duration);
        if (absDuration < mMinDuration) {
            absDuration = mMinDuration;
        }
        mScroller.startScroll(getScrollX(), 0, dx, 0, absDuration);
        invalidate();
        isOpen = false;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //选择拦截
        //如果是横向滑动 我就拦截，否则就不拦截
        switch (ev.getAction()) {
            case ACTION_DOWN:
                mInterceptDownX = ev.getX();
                mInterceptDownY = ev.getY();
                break;
            case ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                /*if (Math.abs(x-mInterceptDownX)>Math.abs(y-mInterceptDownY)) {
                    return  true;  //自己消费
                }*/
                if (Math.abs(x - mInterceptDownX) > 0) {
                    return true;
                }
                break;
            case ACTION_UP:
                break;
        }


        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case ACTION_MOVE:
                int scrollX = getScrollX();
                float moveX = event.getX();
                float moveY = event.getY();
                //移动的差值
                int dx = (int) (moveX - mDownX);
                if (dx > 0) {
                    mCurrentDirection = Direction.RIGHT;
                } else {
                    mCurrentDirection = Direction.LEFT;
                }

                //把差值使用
                mDownX = moveX;
                mDownY = moveY;
                //mContentLeft+=dx;
                //判断边界
                int resultScrollx = -dx + scrollX;
                if (resultScrollx <= 0) {
                    scrollTo(0, 0);
                } else if (resultScrollx > mEditView.getMeasuredWidth()) {
                    scrollTo(mEditView.getMeasuredWidth(), 0);
                } else {
                    //把差值使用起来
                    scrollBy(-dx, 0);
                }
                mDownY = moveX;
                mDownY = moveY;
                break;
            case ACTION_UP:
                float upX = event.getX();
                float upY = event.getY();
                //处理翻译以后，是显示还是收缩回去
                int hasBeenScrollx = getScrollX();
                int editViewWidt = mEditView.getMeasuredWidth();
                if (isOpen) {
                    //当前状态打开
                    if (mCurrentDirection == Direction.RIGHT) {
                        //方向向右，如果小于3、4 ，那些关闭
                        //否则打一
                        if (hasBeenScrollx <= editViewWidt * 4 / 4) {
                            close();
                        } else {
                            open();
                        }
                    } else if (mCurrentDirection == Direction.LEFT) {
                        open();
                    }
                } else {
                    //当前状态关闭
                    if (mCurrentDirection == Direction.LEFT) {//向左滑动
                        if (hasBeenScrollx > editViewWidt / 5) {
                            open();
                        } else {
                            close();
                        }
                    } else if (mCurrentDirection == Direction.RIGHT) {
                        //向右滑动
                        close();
                    }
                }

                break;
        }

        return true;
    }
}


