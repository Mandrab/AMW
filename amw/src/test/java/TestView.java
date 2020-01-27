import Controller.Controller;
import View.ViewImpl;
import org.junit.Test;

import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestView {

	@Test @SuppressWarnings( "unchecked" )
	public void submitOrder( ) throws InterruptedException {
		new ViewImpl( new Controller( ) {
			@Override
			public <T> T askFor ( Request request, String... args ) {
				return ( T ) new Vector<>( IntStream.range( 0, 100 )
						.mapToObj( i -> "Item " + i )
						.collect( Collectors.toList( ) ) );
			}
		} );
		Thread.sleep( 20000 );
	}
}
