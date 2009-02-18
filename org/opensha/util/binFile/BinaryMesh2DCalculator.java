package org.opensha.util.binFile;

public class BinaryMesh2DCalculator {
	
	public static final int FAST_XY = 0;
	public static final int FASE_YX = 1;
	
	public static final int TYPE_SHORT = 0;
	public static final int TYPE_INT = 1;
	public static final int TYPE_LONG = 2;
	public static final int TYPE_FLOAT = 3;
	public static final int TYPE_DOUBLE = 4;
	
	private long nx;
	private long ny;
	private int numType;
	
	private int meshOrder = FAST_XY;
	
	private int numBytesPerPoint;
	
	public BinaryMesh2DCalculator(int numType, long nx, long ny) {
		
		if (numType == TYPE_SHORT) {
			numBytesPerPoint = 2;
		} else if (numType == TYPE_INT || numType == TYPE_FLOAT) {
			numBytesPerPoint = 4;
		} else if (numType == TYPE_LONG || numType == TYPE_DOUBLE) {
			numBytesPerPoint = 8;
		}
		
		this.nx = nx;
		this.ny = ny;
		
		this.numType = numType;
	}
	
	public long calcMeshIndex(long x, long y) {
		if (meshOrder == FAST_XY) {
			return nx * y + x;
		} else { // FAST_YX
			return ny * x + y;
		}
	}
	
	public long calcFileIndex(long x, long y) {
		return numBytesPerPoint * this.calcMeshIndex(x, y);
	}
	
	public long getNX() {
		return nx;
	}

	public void setNX(int nx) {
		this.nx = nx;
	}

	public long getNY() {
		return ny;
	}

	public void setNY(int ny) {
		this.ny = ny;
	}

	public int getMeshOrder() {
		return meshOrder;
	}

	public void setMeshOrder(int meshOrder) {
		this.meshOrder = meshOrder;
	}
	
	public int getType() {
		return numType;
	}

}
