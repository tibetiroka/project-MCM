package dartproductions.mcleodmassacre.util;

public final class Pair<First, Second> {
	public First first;
	public Second second;
	
	public Pair(First first, Second second) {
		this.first = first;
		this.second = second;
	}
	
	public Pair() {
	}
	
	public First first() {
		return first;
	}
	
	public Second second() {
		return second;
	}
	
	public First setFirst(First first) {
		First prev = this.first;
		this.first = first;
		return prev;
	}
	
	public Second setSecond(Second second) {
		Second prev = this.second;
		this.second = second;
		return prev;
	}
}