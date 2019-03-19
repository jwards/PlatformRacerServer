package game;

public class RectangleF {

    private final float left,top,right,bottom;

    public RectangleF(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean contains(float x,float y){
        return x>= left && x<right && y>=top && y< bottom;
    }

    public boolean intersects(RectangleF rectangleF){
        return intersects(rectangleF.left, rectangleF.top, rectangleF.right, rectangleF.bottom);
    }

    public boolean intersects(float left, float top, float right, float bottom) {
        boolean t1 = inRange(this.left, left, right) || inRange(left, this.left, this.right);
        boolean t2 = inRange(this.top, top, bottom) || inRange(top, this.top, this.bottom);
        return t1 && t2;
    }

    private boolean inRange(float val,float min,float max){
        return val >= min && val <= max;
    }



}
