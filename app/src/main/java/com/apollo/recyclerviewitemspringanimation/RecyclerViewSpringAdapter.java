package com.apollo.recyclerviewitemspringanimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by lei.xiao on 2018/4/28.
 */

@SuppressWarnings("unchecked")
public class RecyclerViewSpringAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_SPRING_HEADER = 100;
    private static final float SCROLL_RATIO = 0.5f;
    private static final int DURATION = 500;

    /**
     * 位置标记
     */
    private float mLastY = 0;

    private float mMaxScaleValue = 1.5f;
    private State mState = State.NORMAL;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mDelegate;

    /**
     * 弹性头部
     */
    private View mVSpringHeader;
    private Rect mSpringHeaderRect;
    private View mCardView;
    private View cardViewTop;
    private boolean mShowSpringHeader;

    /**
     * 数据变化观察者
     */
    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(fromPosition, toPosition);
        }
    };

    public RecyclerViewSpringAdapter(RecyclerView recyclerView, RecyclerView.Adapter adapter, View springView) {
        mSpringHeaderRect = new Rect();
        mRecyclerView = recyclerView;
        mDelegate = adapter;
        mVSpringHeader = springView;
        mCardView=springView.findViewById(R.id.card_view);
        cardViewTop=springView.findViewById(R.id.card_view_top);
        mDelegate.registerAdapterDataObserver(mDataObserver);
        mShowSpringHeader = springView != null;
        initLayoutManager();
        initTouchListener();
    }

    private void initLayoutManager() {
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        //修复表格可能出现的不占满的问题
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager manager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup lookup = manager.getSpanSizeLookup();
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (type == VIEW_TYPE_SPRING_HEADER)
                        return manager.getSpanCount();
                    return lookup.getSpanSize(position);
                }
            });
        }
    }

    private void initTouchListener() {
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        rollBackAnimation();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        return moveAnimation(event);
                }
                return false;
            }
        });
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SPRING_HEADER) {
            return new SpringHeaderHolder(mVSpringHeader);
        }
        return mDelegate.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mShowSpringHeader && position == 0) {
            return;
        }
        mDelegate.onBindViewHolder(holder, position - 1);
    }

    @Override
    public int getItemCount() {
        if (mShowSpringHeader)
            return mDelegate.getItemCount() + 1;
        return mDelegate.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowSpringHeader && position == 0) {
            return VIEW_TYPE_SPRING_HEADER;
        }
        return mDelegate.getItemViewType(position);
    }

    /**
     * 移动动画
     *
     * @param event
     */
    private boolean moveAnimation(MotionEvent event) {
        if (!mShowSpringHeader) {
            return false;
        }

        if (mState != State.DRAGGING) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                mLastY = event.getY();
            } else {
                return false;
            }
        }
        // 滚动距离乘以一个系数
        int distance = (int) ((event.getY() - mLastY) * SCROLL_RATIO);
        if (distance < 0) {
            return false;
        }
        //只要distance大于0，就认为在拖动
        mState = State.DRAGGING;
        if (mSpringHeaderRect.isEmpty()) {
            mSpringHeaderRect.set(mVSpringHeader.getLeft(), mVSpringHeader.getTop(), mVSpringHeader.getRight(), mVSpringHeader.getBottom());
        }
        //计算缩放
        float scaleX = (distance + mSpringHeaderRect.width()) / mSpringHeaderRect.width();
        float scaleY = (distance + cardViewTop.getLayoutParams().height) / cardViewTop.getLayoutParams().height;
        if (scaleY >= mMaxScaleValue) {
            return true;
        }
        // 增加header高度
        ViewGroup.LayoutParams lp = mVSpringHeader.getLayoutParams();
        lp.height = mSpringHeaderRect.height() + distance;
        mVSpringHeader.setLayoutParams(lp);

        FrameLayout.LayoutParams lp1= (FrameLayout.LayoutParams) mCardView.getLayoutParams();
        lp1.topMargin=distance;
        mCardView.setLayoutParams(lp1);

        // 设置header缩放
        mVSpringHeader.setScaleX(scaleX);
        mVSpringHeader.setScaleY(scaleY);
        return true;
    }

    /**
     * 还原动画
     */
    private void rollBackAnimation() {
        if (!mShowSpringHeader) {
            return;
        }

        if (mState != State.DRAGGING) {
            return;
        }

        final ViewGroup.LayoutParams lp = mVSpringHeader.getLayoutParams();
        int height = mVSpringHeader.getLayoutParams().height;// 图片当前高度
        int newHeight = mSpringHeaderRect.height();// 图片原高度

        final FrameLayout.LayoutParams lp1= (FrameLayout.LayoutParams) mCardView.getLayoutParams();
        int topMargin= lp1.topMargin;
        ValueAnimator valueAnimator=ObjectAnimator.ofInt(topMargin,0).setDuration(DURATION);
        valueAnimator.setInterpolator(new SpringInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp1.topMargin= (int) animation.getAnimatedValue();
                mCardView.setLayoutParams(lp1);
            }
        });

        // 设置动画
        ValueAnimator animLayout = ObjectAnimator.ofInt(height, newHeight).setDuration(DURATION);
        animLayout.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp.height = (int) animation.getAnimatedValue();
                mVSpringHeader.setLayoutParams(lp);
            }
        });
        animLayout.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //清空矩阵以及状态
                mSpringHeaderRect.setEmpty();
                mState = State.NORMAL;
                mLastY = 0;
            }
        });

        ValueAnimator animX = ObjectAnimator.ofFloat(mVSpringHeader, View.SCALE_X, mVSpringHeader.getScaleX(), 1.0f).setDuration(DURATION);
        ValueAnimator animY = ObjectAnimator.ofFloat(mVSpringHeader, View.SCALE_Y, mVSpringHeader.getScaleY(), 1.0f).setDuration(DURATION);
        animX.start();
        animY.start();
        animLayout.start();
        valueAnimator.start();
    }

    /**
     * 设置弹性头部 设置为空，就不显示；
     *
     * @param springHeader
     */
    public void setSpringHeader(View springHeader) {
        mVSpringHeader = springHeader;
        if (null == mVSpringHeader) {
            mShowSpringHeader = false;
        }
    }

    public void setMaxScaleValue(float maxScaleValue) {
        mMaxScaleValue = maxScaleValue;
    }

    public RecyclerView.Adapter getDelegate() {
        return mDelegate;
    }

    /**
     * 弹性头部holder
     */
    private static class SpringHeaderHolder extends RecyclerView.ViewHolder {
        SpringHeaderHolder(View itemView) {
            super(itemView);
        }
    }


    private enum State {
        DRAGGING,
        NORMAL
    }
}

