package dartproductions.mcleodmassacre;

class MainTest {
	
	@org.junit.jupiter.api.Test
	void main() {
		Main.main(new String[]{});
		try {
			Thread.sleep(10000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}