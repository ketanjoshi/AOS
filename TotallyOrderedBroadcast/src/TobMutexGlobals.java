
public class TobMutexGlobals {
	private static int logicalclock;
	private static boolean reqGranted = false;
	
	public static int getLogicalclock() 
	{
		return logicalclock;
	}

	public static void setLogicalclock(int logicalclock) 
	{
		TobMutexGlobals.logicalclock = logicalclock;
	}
	
	public static boolean isReqGranted() {
		return reqGranted;
	}

	public static void setReqGranted(boolean reqGranted) {
		TobMutexGlobals.reqGranted = reqGranted;
	}
	
}
