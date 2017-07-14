
public class GridCoordinate {

    public GridCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GridCoordinate)) {
            return false;
        }
        if (obj == this)
            return true;
        GridCoordinate gridCoordinate = (GridCoordinate) obj;
        return (gridCoordinate.x == this.x && gridCoordinate.y == this.y);
    }

    private int x;
    private int y;
}
