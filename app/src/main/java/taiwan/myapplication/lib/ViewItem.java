package taiwan.myapplication.lib;

import android.view.View;

import java.util.ArrayList;

public class ViewItem {

    public int get_id() {
        return _id;
    }

    public ViewItem set_id(int _id) {
        this._id = _id;
        return this;
    }

    private int _id;
    public View view;
    public int tag;
    public ArrayList<Point> positions;

    public void setPositions(int[]... positions) {
        this.positions = new ArrayList<>();
        for (int[] position : positions) {
            Point p = new Point();
            p.X = position[0];
            p.Y = position[1];
            this.positions.add(p);
        }
    }

    public ViewItem(int[]... positions) {
        this.positions = new ArrayList<>();
        for (int[] position : positions) {
            Point p = new Point();
            p.X = position[0];
            p.Y = position[1];
            this.positions.add(p);
        }
    }

    public ItemSize size;

    public enum ItemSize {
        min,
        mid_width,
        mid_height,
        max
    }
}