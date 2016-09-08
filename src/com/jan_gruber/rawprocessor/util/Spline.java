package com.jan_gruber.rawprocessor.util;

/**
 * http://subversion.developpez.com/projets/Millie/trunk/MillieCommons/src/millie/commons/utils/Spline.java
 * 
 * Millie : Multifunctional Library For Image Processing
 * 
 * (c) Copyright 2009 by Humbert Florent
 * 
 *      This program is free software; you can redistribute it and/or modify  
 *      it under the terms of the GNU General Public License as published by  
 *      the Free Software Foundation; only version 2 of the License.          
 *                                                                            
 *      This program is distributed in the hope that it will be useful,       
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of        
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         
 *      GNU General Public License for more details.                          
 *                                                                            
 *      You should have received a copy of the GNU General Public License     
 *      along with this program; if not, write to the Free Software           
 *      Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA             
 *      02111-1307, USA.                                                      
 */
// originally from package millie.commons.utils;

import java.awt.Point;

/**
 * : P(X) =
 * aX^3+bX^2+cX+d
 * 
 * code de Xavier Philippeau :
 * http://www.developpez.net/forums/showthread.php?t=331608&page=4
 * 
 * @author florent
 * 
 */
public class Spline {

    private int minX;
    private int maxX;
    //private int minY;
    //private int maxY;

    private int[] values;
    private int[] xx;
    private int[] yy;

    public int max(int[] t) {
	if (t.length == 0)
	    throw new IllegalArgumentException("size t == 0");
	int max = t[0];
	for (int i = 1; i < t.length; i++)
	    if (t[i] > max)
		max = t[i];
	return max;
    }

    public int min(int[] t) {
	if (t.length == 0)
	    throw new IllegalArgumentException("size t == 0");
	int min = t[0];
	for (int i = 1; i < t.length; i++)
	    if (t[i] < min)
		min = t[i];
	return min;
    }

    /**
    
     * 
     * @param xx
     * @param yy
     */
    public Spline(int[] xx, int[] yy) {
	if (xx.length != yy.length || xx.length < 2)
	    throw new IllegalArgumentException("Size<=2");
	this.xx = xx;
	this.yy = yy;

	minX = xx[0];
	maxX = xx[xx.length - 1];
	values = new int[maxX - minX + 1];

	computeValue();
    }

    private void computeValue() {
	if (xx.length == 2) {
	    for (int i = 0; i < xx.length - 1; i++) {
		for (int x = xx[i]; x < xx[i + 1]; x++) {
		    int value;
		    double factor = ((double) yy[i + 1] - yy[i])
			    / ((double) xx[i + 1] - xx[i]);
		    value = (int) (factor * (double) x + yy[i] - factor
			    * (double) xx[i]);

		    values[x - minX] = value;
		}
	    }
	} else {
	    Point[] points = new Point[xx.length];
	    for (int i = 0; i < points.length; i++) {
		Point p = new Point(xx[i], yy[i]);
		points[i] = p;
	    }
	    double[] sd = secondDerivative(points);
	    for (int i = 0; i < points.length - 1; i++) {
		Point cur = points[i];
		Point next = points[i + 1];

		for (int x = cur.x; x < next.x; x++) {
		    double t = (double) (x - cur.x) / (next.x - cur.x);

		    double a = 1 - t;
		    double b = t;
		    double h = next.x - cur.x;

		    double y = a
			    * cur.y
			    + b
			    * next.y
			    + (h * h / 6)
			    * ((a * a * a - a) * sd[i] + (b * b * b - b)
				    * sd[i + 1]);
		    /*
		    if (y > 255)
			y = 255;
		    if (y < 0)
			y = 0;
		    */
		    values[x - minX] = (int) y;
		}
	    }

	}
    }

    public int getValue(int x) {
	if (x - minX < 0)
	    return yy[0];
	if (x - minX >= values.length - 1)
	    return yy[yy.length - 1];

	return values[x - minX];
    }

    /**
     * @return
     */
    public int[] getValues(int min, int max) {
	int[] out = new int[max - min + 1];
	for (int i = 0; i < out.length; i++)
	    out[i] = getValue(i);
	return out;
    }

    public static double[] secondDerivative(Point... P) {
	int n = P.length;

	// build the tridiagonal system 
	// (assume 0 boundary conditions: y2[0]=y2[-1]=0) 
	double[][] matrix = new double[n][3];
	double[] result = new double[n];
	matrix[0][1] = 1;
	for (int i = 1; i < n - 1; i++) {
	    matrix[i][0] = (double) (P[i].x - P[i - 1].x) / 6;
	    matrix[i][1] = (double) (P[i + 1].x - P[i - 1].x) / 3;
	    matrix[i][2] = (double) (P[i + 1].x - P[i].x) / 6;
	    result[i] = (double) (P[i + 1].y - P[i].y) / (P[i + 1].x - P[i].x)
		    - (double) (P[i].y - P[i - 1].y) / (P[i].x - P[i - 1].x);
	}
	matrix[n - 1][1] = 1;

	// solving pass1 (up->down)
	for (int i = 1; i < n; i++) {
	    double k = matrix[i][0] / matrix[i - 1][1];
	    matrix[i][1] -= k * matrix[i - 1][2];
	    matrix[i][0] = 0;
	    result[i] -= k * result[i - 1];
	}
	// solving pass2 (down->up)
	for (int i = n - 2; i >= 0; i--) {
	    double k = matrix[i][2] / matrix[i + 1][1];
	    matrix[i][1] -= k * matrix[i + 1][0];
	    matrix[i][2] = 0;
	    result[i] -= k * result[i + 1];
	}

	// return second derivative value for each point P
	double[] y2 = new double[n];
	for (int i = 0; i < n; i++)
	    y2[i] = result[i] / matrix[i][1];
	return y2;
    }

}
