
public class TobMutexGlobals {
	private static int logicalclock;

	public static int getLogicalclock() {
		return logicalclock;
	}

	public static void setLogicalclock(int logicalclock) {
		TobMutexGlobals.logicalclock = logicalclock;
	}
}
