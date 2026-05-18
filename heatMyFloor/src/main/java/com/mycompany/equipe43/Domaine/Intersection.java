package com.mycompany.equipe43.Domaine;

public class Intersection {
    private double x; // position en pouces
    private double y; // position en pouces

    // voisins (haut, bas, gauche, droite)
    private Intersection top;
    private Intersection bottom;
    private Intersection left;
    private Intersection right;

    public Intersection(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public void setTop(Intersection top) { this.top = top; }
    public void setBottom(Intersection bottom) { this.bottom = bottom; }
    public void setLeft(Intersection left) { this.left = left; }
    public void setRight(Intersection right) { this.right = right; }

    public Intersection getTop() { return top; }
    public Intersection getBottom() { return bottom; }
    public Intersection getLeft() { return left; }
    public Intersection getRight() { return right; }

    // Translation
    public void translate(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }
}
