package interpackage.utils;

public class Utils {

	public static void sleep( long ms ) {
		try {
			Thread.sleep( ms );
		} catch ( InterruptedException ie ) {
			ie.printStackTrace( );
		}
	}
}
