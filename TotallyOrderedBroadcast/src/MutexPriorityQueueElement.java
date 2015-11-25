
public class MutexPriorityQueueElement implements Comparable<MutexPriorityQueueElement>{

	private int nodeId;
	private int logicalClock;
	
	
	public MutexPriorityQueueElement(int nodeId, int logicalClock) {
		this.nodeId = nodeId;
		this.logicalClock = logicalClock;
	}
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public int getLogicalClock() {
		return logicalClock;
	}
	public void setLogicalClock(int logicalClock) {
		this.logicalClock = logicalClock;
	}
	@Override
	public int compareTo(MutexPriorityQueueElement o) {
		if(this.logicalClock < o.logicalClock)
			return -1;
		else if(this.logicalClock > o.logicalClock)
			return 1;
		else if(this.nodeId < o.nodeId)
			return -1;
		else 
			return 1;
	}

	@Override
	public boolean equals(Object obj) {
		MutexPriorityQueueElement e = (MutexPriorityQueueElement) obj;
		if(this.nodeId == e.nodeId)
			return true;
		return false;
	}
}
