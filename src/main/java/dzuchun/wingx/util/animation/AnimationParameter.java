package dzuchun.wingx.util.animation;

public enum AnimationParameter {
	X_POS, Y_POS, Z_POS, X_ROT, Y_ROT, Z_ROT;

	public static AnimationParameter get(int type) {
		switch (type) {
		case 0:
			return X_POS;
		case 1:
			return Y_POS;
		case 2:
			return Z_POS;
		case 3:
			return X_ROT;
		case 4:
			return Y_ROT;
		case 5:
			return Z_ROT;
		}
		return null;
	}
}
