package taiwan.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import taiwan.myapplication.lib.HomeFragmentMetro;
import taiwan.myapplication.lib.ViewItem;

/**
 * Created by TenWang on 2016/4/15.
 */
public class sample extends HomeFragmentMetro {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View metroFragment = super.onCreateView(inflater, container, savedInstanceState);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View _view = layoutInflater.inflate(R.layout.grid_item, null);
        RelativeLayout.LayoutParams layoutParams = getViewItemLayoutParams(0, itemWidth, itemHeight);
        layoutParams.leftMargin = 0 * itemWidth;
        layoutParams.topMargin = 0 * itemHeight;
        _view.setLayoutParams(layoutParams);
        ViewItem viewItem = new ViewItem(new int[]{-1, -1});
        viewItem.view = _view;
        viewItem.view.setTag(viewItem);
        viewItem.size = ViewItem.ItemSize.min;
        viewItem.tag = views.size();
        addViewItem(viewItem, -1);

        View _view2 = layoutInflater.inflate(R.layout.grid_item, null);
        RelativeLayout.LayoutParams layoutParams2 = getViewItemLayoutParams(1, itemWidth, itemHeight);
        layoutParams2.leftMargin = 1 * itemWidth;
        layoutParams2.topMargin = 1 * itemHeight;
        _view2.setLayoutParams(layoutParams2);
        ViewItem viewItem2 = new ViewItem(new int[]{1, 1}, new int[]{1, 2});
        viewItem2.view = _view2;
        viewItem2.view.setTag(viewItem2);
        viewItem2.size = ViewItem.ItemSize.mid_width;
        viewItem2.tag = views.size();
        addViewItem(viewItem2, -1);
        return metroFragment;
    }
    @Override
    protected void onEnterEditMode(View v){
        v.findViewById(R.id.delete).setVisibility(View.VISIBLE);
    }
}
