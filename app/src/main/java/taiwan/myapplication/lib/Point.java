package taiwan.myapplication.lib;

public class Point {
    public int X;
    public int Y;
    public int belong;
    public Boolean ckeck;

    public Boolean isEqual(Point p) {
        return (this.X == p.X && this.Y == p.Y);
    }
}
