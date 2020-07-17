package dzuchun.wingx.util;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public enum Facing {
	FORWARD {
		@Override
		public Vector3d transform(Vector3d vectorIn) {
			return vectorIn;
		}

		@Override
		public int toInt() {
			return 0;
		}
	},
	BACKWARD {
		@Override
		public Vector3d transform(Vector3d vectorIn) {
			return vectorIn.scale(-1);
		}

		@Override
		public int toInt() {
			return 1;
		}
	},
	LEFT {
		@Override
		public Vector3d transform(Vector3d vectorIn) {
			return new Vector3d(vectorIn.z, 0, -vectorIn.x);
		}

		@Override
		public int toInt() {
			return 2;
		}
	},
	RIGHT {
		@Override
		public Vector3d transform(Vector3d vectorIn) {
			return new Vector3d(-vectorIn.z, 0, vectorIn.x);
		}

		@Override
		public int toInt() {
			return 3;
		}
	},
	UP {
		@Override
		public Vector3d transform(Vector3d vectorIn) {
			return new Vector3d(0, vectorIn.length(), 0);
		}

		@Override
		public int toInt() {
			return 4;
		}
	},
	DOWN {
		@Override
		public Vector3d transform(Vector3d vectorIn) {
			return new Vector3d(0, -vectorIn.length(), 0);
		}

		@Override
		public int toInt() {
			return 5;
		}
	};

	public static Facing getByInt(int intValue) {
		switch (intValue) {
		case 0:
			return FORWARD;
		case 1:
			return BACKWARD;
		case 2:
			return LEFT;
		case 3:
			return RIGHT;
		case 4:
			return UP;
		case 5:
			return DOWN;
		default:
			return null;
		}
	}

	public abstract Vector3d transform(Vector3d vectorIn);

	public abstract int toInt();
}
