package taiwan.myapplication.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by TenWang on 2015/2/5.
 */
public class MetroScrollView extends ScrollView {

    public MetroScrollView(Context context) {
        super(context);

    }

    public MetroScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MetroScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)   //這個方法如果返回 true 的話 兩個手指移動，启動一個按下的手指的移動不能被傳播出去。
    {
        super.onInterceptTouchEvent(event);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)        //這個方法如果 true 則整個Activity 的 onTouchEvent() 不會被系統回調
    {
        super.onTouchEvent(event);
        return true;
    }
}
