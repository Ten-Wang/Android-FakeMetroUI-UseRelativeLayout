package taiwan.myapplication.lib;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import taiwan.myapplication.R;

/**
 * Created by TenWang on 2016/4/8 refactor HomeScreen.
 */
public class HomeFragmentMetro extends Fragment {
    public View metroFragment;
    private ViewGroup _root;
    private int statusBar = 0;
    private int actionBar = 0;
    private int navigationBar = 0;

    private ItemOperator itemOperator;

    private enum ItemOperator {
        reSize, move
    }

    //It's provide an status for others component to use.
    private MetroActionMode actionMode;

    public enum MetroActionMode {
        switchMode, editMode
    }

    private MetroListener metroActionModeListener;

    public interface MetroListener {
        void metroAction(MetroActionMode mode);
    }

    public HomeFragmentMetro newInstance(MetroListener listener) {
        HomeFragmentMetro fragment = new HomeFragmentMetro();
        this.metroActionModeListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        metroFragment = inflater.inflate(R.layout.fragment_metro, container, false);
        getScreenSizeAndData();


        metroFragment.setOnTouchListener(screenOnTouchListener);
        return metroFragment;
    }

    protected RelativeLayout.LayoutParams getViewItemLayoutParams(int size, int itemWidth, int itemHeight) {
        RelativeLayout.LayoutParams layoutParams;
        switch (size) {
            case 0:
                layoutParams = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
                break;
            case 1:
                layoutParams = new RelativeLayout.LayoutParams(itemWidth * 2, itemHeight);
                break;
            case 2:
                layoutParams = new RelativeLayout.LayoutParams(itemWidth, itemHeight * 2);
                break;
            case 3:
                layoutParams = new RelativeLayout.LayoutParams(itemWidth * 2, itemHeight * 2);
                break;
            default:
                layoutParams = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
                break;
        }
        return layoutParams;
    }

    private int screenWidth;
    private int screenHeight;
    protected int itemWidth;
    protected int itemHeight;

    private void getScreenSizeAndData() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        getActionBarHeight();
        MetroScrollView scrollView = (MetroScrollView) metroFragment.findViewById(R.id.scrollView);
        scrollView.getLayoutParams().height = screenHeight;
        scrollView.getLayoutParams().width = screenWidth;

        itemWidth = screenWidth / rowCount;
        itemHeight = (screenHeight - statusBar - actionBar - navigationBar) / columnCount;
        _root = (ViewGroup) metroFragment.findViewById(R.id.fragment_Metro_relativelayout);
        _root.removeAllViews();
        _root.getLayoutParams().width = screenWidth;
        _root.getLayoutParams().height = screenHeight;
        views = new ArrayList<>();
    }

    private void getActionBarHeight() { // 抓取邊框高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBar = getResources().getDimensionPixelSize(resourceId);
        }

        TypedValue tv = new TypedValue();
        if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBar = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        int resId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) {
            navigationBar = getResources().getDimensionPixelSize(resId);
        }
    }

    private int rowCount = 4;
    private int columnCount = 6;
    private ArrayList<Point> screenPointUse;

    private void initPoints() {
        screenPointUse = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                Point p = new Point();
                p.X = j;
                p.Y = i;
                p.ckeck = false;
                screenPointUse.add(p);
            }
        }
    }

    private int tempX;
    private int tempY;

    private void cloneViewItem(ViewItem item) {
        tempX = item.positions.get(0).X;
        tempY = item.positions.get(0).Y;
    }

    protected ArrayList<ViewItem> views;

    public void addViewItem(ViewItem viewItem,int id) {
        if (getActivity() == null) {
            return;
        }
        ((ViewGroup) viewItem.view).setClipChildren(false);
        viewItem.view.setOnLongClickListener(editMode);
        viewItem.view.setOnTouchListener(touchEvent);
        _root.addView(viewItem.view);
        views.add(viewItem);
        if(id != -1) {
            viewItem.set_id(id);
        }else{
            for(int i = 0 ; i <= views.size() ; i++){
                boolean flag = true;
                for(ViewItem vItem: views){
                    if(vItem.get_id() == i){
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    viewItem.set_id(i);
                }
            }
        }
        updateScreenPosition();
    }


    View.OnTouchListener touchEvent = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (actionMode == MetroActionMode.editMode) {
                itemOperator = ItemOperator.move;
                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY() + ((ScrollView) v.getParent().getParent()).getScrollY();
                ;
                ViewItem nowItem = null;
                for (ViewItem item : views) {
                    if (item.view == v) {
                        nowItem = item;
                        break;
                    }
                }
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownItem(nowItem, X, Y);
                        break;
                    case MotionEvent.ACTION_UP:
                        touchUpItem(nowItem, X, Y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (clickItemPointIndex == -1) touchDownItem(nowItem, X, Y);
                        else {
                            if ((int) event.getRawY() > getResources().getDisplayMetrics().heightPixels / 10 * 9) {
                                //幾秒後(delaySec)呼叫runTimerStop這個Runnable，再由這個Runnable去呼叫你想要做的事情
                                ScrollRunnable s = new ScrollRunnable(100);
                                myHandler.postDelayed(s, delayTime * 500);
                            } else if ((int) event.getRawY() < getResources().getDisplayMetrics().heightPixels / 10) {
                                ScrollRunnable s = new ScrollRunnable(-100);
                                myHandler.postDelayed(s, delayTime * 500);
                            }
                            touchMoveItem(nowItem, X, Y);
                        }
                        break;
                }
                _root.invalidate();
            }
            return false;
        }
    };

    private void touchDownItem(ViewItem nowItem, int X, int Y) {
        cloneViewItem(nowItem);
//        nowItem.img_resize.setVisibility(View.VISIBLE);
//        nowItem.view.setAlpha((float)0.5);
        for (int i = 0; i < nowItem.positions.size(); i++) {
            if (nowItem.positions.get(i).X == X / itemWidth && nowItem.positions.get(i).Y == (Y - statusBar - actionBar) / itemHeight) {
                clickItemPointIndex = i;
                break;
            }
        }

        clickItemPointIndex = 0;
        RelativeLayout.LayoutParams lParam = (RelativeLayout.LayoutParams) nowItem.view.getLayoutParams();
        _xDelta = X - lParam.leftMargin;
        _yDelta = Y - lParam.topMargin;

        nowItem.view.bringToFront();
    }

    private void touchUpItem(ViewItem nowItem, int X, int Y) {
        if (X / itemWidth > rowCount - 1 || (Y - statusBar - actionBar) / itemHeight > columnCount - 1)
            return;

        int rootX = nowItem.positions.get(0).X;
        int rootY = nowItem.positions.get(0).Y;
        setItemPosition(nowItem, new int[]{rootX, rootY});
        checkOverlap(nowItem);
        for (ViewItem item : views) {
            if (item != nowItem) {
                for (Point itemP : item.positions) {
                    for (Point p : nowItem.positions) {
                        if (p.isEqual(itemP)) {
                            isNoSpaceToMove = true;
                            break;
                        }
                    }
                }
            }
        }

        if (isNoSpaceToMove) {
            rootX = tempX;
            rootY = tempY;
            isNoSpaceToMove = false;
        }

        boolean isReset = false;
        if (nowItem.size == ViewItem.ItemSize.mid_width) {
            if (rootX < 0) {
                rootX = 0;
                resetItem(nowItem, rootX, rootY);
                isReset = true;
            } else if (rootX + 1 >= rowCount) {
                resetItem(nowItem, rootX, rootY);
                isReset = true;
            }
        } else if (nowItem.size == ViewItem.ItemSize.mid_height) {
            if (rootY < 0) {
                rootY = 0;
                resetItem(nowItem, rootX, rootY);
                isReset = true;
            } else if (rootY + 1 >= columnCount) {
                resetItem(nowItem, rootX, rootY);
                isReset = true;
            }
        } else if (nowItem.size == ViewItem.ItemSize.max) {
            if (rootX < 0 || rootY < 0) {
                rootX = rootX < 0 ? 0 : rootX;
                rootY = rootY < 0 ? 0 : rootY;
                resetItem(nowItem, rootX, rootY);
                isReset = true;
            } else if (rootX + 1 >= rowCount || rootY + 1 >= columnCount) {
                resetItem(nowItem, rootX, rootY);
                isReset = true;
            }
        }

        if (!isReset) {
            RelativeLayout.LayoutParams layoutParamsup = (RelativeLayout.LayoutParams) nowItem.view.getLayoutParams();
            layoutParamsup.leftMargin = rootX * itemWidth;
            layoutParamsup.topMargin = rootY * itemHeight;
            if (nowItem.size == ViewItem.ItemSize.min) {
                layoutParamsup.width = itemWidth;
                layoutParamsup.height = itemHeight;
            }
            nowItem.view.setLayoutParams(layoutParamsup);
        }

        setItemPosition(nowItem, new int[]{rootX, rootY});
        updateScreenPosition();
        checkOverlap(nowItem);

//        nowItem.view.setAlpha((float) 1);
    }

    private void resetItem(ViewItem item, int rootX, int rootY) {
        item.size = ViewItem.ItemSize.min;
        RelativeLayout.LayoutParams layoutParamsup = (RelativeLayout.LayoutParams) item.view.getLayoutParams();
        layoutParamsup.leftMargin = rootX * itemWidth;
        layoutParamsup.topMargin = rootY * itemHeight;
        layoutParamsup.width = itemWidth;
        layoutParamsup.height = itemHeight;
        item.view.setLayoutParams(layoutParamsup);
    }

    private int _xDelta;
    private int _yDelta;
    private static int clickItemPointIndex = 0;

    private int moveX;
    private int moveY;
    private ViewItem moveItem;

    private void touchMoveItem(final ViewItem nowItem, final int X, final int Y) {
        if (X / itemWidth > rowCount - 1 || (Y - statusBar - actionBar) / itemHeight > columnCount - 1) {
            return;
        }
        nowItem.view.setLeft(X - _xDelta);
        nowItem.view.setTop(Y - _yDelta);
        if(nowItem.size == ViewItem.ItemSize.min) {
            nowItem.view.setRight(X - _xDelta + itemWidth);
            nowItem.view.setBottom(Y - _yDelta + itemHeight);
        }else if(nowItem.size == ViewItem.ItemSize.mid_width) {
            nowItem.view.setRight(X - _xDelta + itemWidth * 2);
            nowItem.view.setBottom(Y - _yDelta + itemHeight);
        }else if(nowItem.size == ViewItem.ItemSize.mid_height) {
            nowItem.view.setRight(X - _xDelta + itemWidth);
            nowItem.view.setBottom(Y - _yDelta + itemHeight * 2);
        }else{
            nowItem.view.setRight(X - _xDelta + itemWidth * 2);
            nowItem.view.setBottom(Y - _yDelta + itemHeight * 2);
        }

        int rootX = X / itemWidth;
        int rootY = (Y - statusBar - actionBar) / itemHeight;
        int index = clickItemPointIndex >= nowItem.positions.size() ? 0 : clickItemPointIndex;
        if (nowItem.positions.get(index).X != rootX || nowItem.positions.get(index).Y != rootY) {
            // 設定起始位置
            if (nowItem.size == ViewItem.ItemSize.min) {
                setItemPosition(nowItem, new int[]{rootX, rootY});
            } else if (nowItem.size == ViewItem.ItemSize.mid_width) {
                if (X - _xDelta + ((screenWidth / rowCount - 1) * 2) > screenWidth) {
                    if (clickItemPointIndex == 0) {
                        setItemPosition(nowItem, new int[]{rootX - 1, rootY});
                    } else {
                        setItemPosition(nowItem, new int[]{rootX - 1, rootY - 1});
                    }
                } else {
                    if (clickItemPointIndex == 0) {
                        setItemPosition(nowItem, new int[]{rootX, rootY});
                    } else {
                        setItemPosition(nowItem, new int[]{rootX - 1, rootY});
                    }
                }
            } else if (nowItem.size == ViewItem.ItemSize.mid_height) {
                if (clickItemPointIndex == 0) {
                    setItemPosition(nowItem, new int[]{rootX, rootY});
                } else {
                    setItemPosition(nowItem, new int[]{rootX, rootY - 1});
                }
            } else {
                if (clickItemPointIndex == 0) {
                    setItemPosition(nowItem, new int[]{rootX, rootY});
                } else if (clickItemPointIndex == 1) {
                    setItemPosition(nowItem, new int[]{rootX - 1, rootY});
                } else if (clickItemPointIndex == 2) {
                    setItemPosition(nowItem, new int[]{rootX, rootY - 1});
                } else {
                    setItemPosition(nowItem, new int[]{rootX - 1, rootY - 1});
                }
            }
            moveX = X;
            moveY = Y;

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    if (moveX == X && moveY == Y) {
                        moveItem = nowItem;
                        viewOverlap.sendEmptyMessage(0);
                    }
                }
            }, 350);
        }
    }

    int delayTime = 1;
    //宣告一個Handler
    Handler myHandler = new Handler();

    //主體
    class ScrollRunnable implements Runnable {
        int Y = 100;

        ScrollRunnable(int Y) {
            this.Y = Y;
        }

        @Override
        public void run() {
            final ScrollView scrollView = ((ScrollView) _root.findViewById(R.id.fragment_Metro_relativelayout).getParent());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                ValueAnimator realSmoothScrollAnimation =
                        ValueAnimator.ofInt(scrollView.getScrollY(), scrollView.getScrollY() + Y);
                realSmoothScrollAnimation.setDuration(100);
                realSmoothScrollAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int scrollTo = (Integer) animation.getAnimatedValue();
                        scrollView.scrollTo(0, scrollTo);
                    }
                });

                realSmoothScrollAnimation.start();
            } else {
                scrollView.smoothScrollTo(0, scrollView.getScrollY() + Y);
            }

        }
    }


    private void setItemPosition(ViewItem item, int[] start) {
        switch (item.size) {
            case min:
                item.positions.get(0).X = start[0];
                item.positions.get(0).Y = start[1];
                break;
            case mid_width:
                item.positions.get(0).X = start[0];
                item.positions.get(1).X = start[0] + 1;
                item.positions.get(0).Y = start[1];
                item.positions.get(1).Y = start[1];
                break;
            case mid_height:
                item.positions.get(0).X = start[0];
                item.positions.get(0).Y = start[1];

                item.positions.get(1).X = start[0];
                item.positions.get(1).Y = start[1] + 1;
                break;
            case max:
                item.positions.get(0).X = start[0];
                item.positions.get(1).X = start[0] + 1;
                item.positions.get(0).Y = start[1];
                item.positions.get(1).Y = start[1];

                item.positions.get(2).X = start[0];
                item.positions.get(3).X = start[0] + 1;
                item.positions.get(2).Y = start[1] + 1;
                item.positions.get(3).Y = start[1] + 1;
                break;

            default:
                break;
        }
        updateScreenPosition();
    }

    private Handler viewOverlap = new Handler() {
        public void handleMessage(android.os.Message msg) {
            checkOverlap(moveItem);
        }

        ;
    };

    private static boolean isNoSpaceToMove = false;

    private void checkOverlap(ViewItem item) {
        for (final ViewItem otherItem : views) {
            if (item != otherItem) {
                boolean isOverlap = false;
                for (Point p : item.positions) {
                    for (Point otherP : otherItem.positions) {
                        if (p.isEqual(otherP)) {
                            isOverlap = true;
                            clearPointInScreenFromItem(otherItem);
                            final int[] start = getNewViewPosition(otherItem.size);
                            if (start != null) {
                                isNoSpaceToMove = false;
                                TranslateAnimation animation = new TranslateAnimation(0, (start[0] - otherItem.positions.get(0).X) * itemWidth, 0, (start[1] - otherItem.positions.get(0).Y) * itemHeight);
                                animation.setDuration(500);
                                animation.setAnimationListener(new TranslateAnimation.AnimationListener() {

                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        // TODO Auto-generated method stub
                                        completeAnimation(otherItem.view);
                                        RelativeLayout.LayoutParams layoutParamsup = (RelativeLayout.LayoutParams) otherItem.view.getLayoutParams();
                                        layoutParamsup.leftMargin = start[0] * itemWidth;
                                        layoutParamsup.topMargin = start[1] * itemHeight;
                                    }
                                });
                                setItemPosition(otherItem, new int[]{start[0], start[1]});
                                updateScreenPosition();
                                otherItem.view.startAnimation(animation);
                            } else {
                                if (itemOperator == ItemOperator.reSize) {
                                    itemResize(item);
//                                    showToast("無空間");
                                } else {
                                    setItemPosition(otherItem, new int[]{otherItem.positions.get(0).X, otherItem.positions.get(0).Y});
                                    isNoSpaceToMove = true;
                                }
                            }
                            break;
                        }
                    }

                    if (isOverlap) {
                        break;
                    }
                }
            }
        }
    }

    private void completeAnimation(View view) {
        view.clearAnimation();
        view.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }

    private void clearPointInScreenFromItem(ViewItem viewItem) {
        initPoints();
        for (ViewItem item : views) {
            for (Point itemP : item.positions) {
                if (item != viewItem) {
                    for (Point p : screenPointUse) {
                        if (p.isEqual(itemP)) {
                            p.ckeck = true;

                        }
                    }
                }
            }
        }
    }

    private int[] getNewViewPosition(ViewItem.ItemSize size) {
        switch (size) {
            case min:
                for (Point p : screenPointUse) {
                    if (!p.ckeck)
                        return new int[]{p.X, p.Y};
                }
                break;
            case mid_width:
                for (int i = 0; i < screenPointUse.size(); i++) {
                    try {
                        if (!screenPointUse.get(i).ckeck && !screenPointUse.get(i + 1).ckeck) {
                            if (screenPointUse.get(i).X == rowCount - 1)
                                continue;
                            return new int[]{screenPointUse.get(i).X, screenPointUse.get(i).Y};
                        }
                    } catch (Exception e) {
                    }
                }
                break;
            case mid_height:
                for (int i = 0; i < screenPointUse.size(); i++) {
                    try {
                        if (!screenPointUse.get(i).ckeck && !screenPointUse.get(i + rowCount).ckeck) {
                            if (screenPointUse.get(i).Y == columnCount - 1)
                                continue;
                            return new int[]{screenPointUse.get(i).X, screenPointUse.get(i).Y};
                        }
                    } catch (Exception e) {
                    }
                }
                break;
            case max:
                for (int i = 0; i < screenPointUse.size(); i++) {
                    try {
                        if (!screenPointUse.get(i).ckeck && !screenPointUse.get(i + 1).ckeck && !screenPointUse.get(i + rowCount).ckeck && !screenPointUse.get(i + rowCount + 1).ckeck) {
                            if (screenPointUse.get(i).X == rowCount - 1 || screenPointUse.get(i).Y == columnCount - 1)
                                continue;
                            return new int[]{screenPointUse.get(i).X, screenPointUse.get(i).Y};
                        }
                    } catch (Exception e) {
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }
    View.OnTouchListener screenOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (actionMode == MetroActionMode.editMode) {
                if(metroActionModeListener != null) {
                    metroActionModeListener.metroAction(MetroActionMode.switchMode);
                }
            }
            return false;
        }
    };

    View.OnLongClickListener editMode = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (actionMode != MetroActionMode.editMode) {
                clickItemPointIndex = -1;
                actionMode = MetroActionMode.editMode;
            }
            onEnterEditMode(v);
            return true;
        }
    };

    protected void onEnterEditMode(View v) {

    }

    private void updateScreenPosition() {
        initPoints();
        for (ViewItem item : views) {
            for (Point itemP : item.positions) {
                for (Point p : screenPointUse) {
                    if (p.isEqual(itemP)) {
                        p.ckeck = true;
                        break;
                    }
                }
            }
        }
    }

    private void itemResize(final ViewItem item) {
        int startX = item.positions.get(0).X;
        int startY = item.positions.get(0).Y;
        RelativeLayout.LayoutParams layoutParamsup = null;

        switch (item.size) {
            case min:
                if (startX + 1 > rowCount - 1) {
                    try {
                        startX = getNewViewPosition(ViewItem.ItemSize.mid_width)[0];
                        startY = getNewViewPosition(ViewItem.ItemSize.mid_width)[1];
                    } catch (Exception e) {
                    }
                }
                layoutParamsup = new RelativeLayout.LayoutParams(itemWidth * 2, itemHeight);
                item.setPositions(new int[]{startX, startY},
                        new int[]{startX + 1, startY});
                item.size = ViewItem.ItemSize.mid_width;
                break;

            case mid_width:
                if (startY + 1 > columnCount - 1) {
                    try {
                        startX = getNewViewPosition(ViewItem.ItemSize.mid_height)[0];
                        startY = getNewViewPosition(ViewItem.ItemSize.mid_height)[1];
                    } catch (Exception e) {
                    }
                }
                layoutParamsup = new RelativeLayout.LayoutParams(itemWidth, itemHeight * 2);
                item.setPositions(new int[]{startX, startY},
                        new int[]{startX, startY + 1});
                item.size = ViewItem.ItemSize.mid_height;
                break;

            case mid_height:
                if (startX + 1 > rowCount - 1 || startY + 1 > columnCount - 1) {
                    try {
                        startX = getNewViewPosition(ViewItem.ItemSize.max)[0];
                        startY = getNewViewPosition(ViewItem.ItemSize.max)[1];
                    } catch (Exception e) {
                    }
                }
                layoutParamsup = new RelativeLayout.LayoutParams(itemWidth * 2, itemHeight * 2);
                item.setPositions(new int[]{startX, startY},
                        new int[]{startX + 1, startY},
                        new int[]{startX, startY + 1},
                        new int[]{startX + 1, startY + 1});
                item.size = ViewItem.ItemSize.max;
                break;

            case max:
                layoutParamsup = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
                item.setPositions(new int[]{startX, startY});
                item.size = ViewItem.ItemSize.min;
                break;

            default:
                break;
        }

        updateScreenPosition();
        layoutParamsup.leftMargin = startX * itemWidth;
        layoutParamsup.topMargin = startY * itemHeight;
        item.view.setLayoutParams(layoutParamsup);
        checkOverlap(item);
    }
}
