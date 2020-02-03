package view;

import interpackage.Item;
import interpackage.RequestDispatcher;
import interpackage.RequestHandler;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestView {

	/*@Test @SuppressWarnings( "unchecked" )
	public void listView( ) throws InterruptedException {
		new ViewImpl( new RequestDispatcher( ) {
			@Override
			public void register ( RequestHandler handler ) { }

			@Override
			public void unregister ( RequestHandler handler ) { }

			@Override
			public <T> T askFor ( Request request, String... args ) {
				CompletableFuture<Vector<?>> ret = new CompletableFuture<>( );
				ret.complete( new Vector<>( IntStream.range( 0, 100 )
						.mapToObj( i -> "Item " + i )
						.collect( Collectors.toList( ) ) ) );
				return ( T ) ret;
			}
		} );
		Thread.sleep( 20000 );
	}*/

	@Test @SuppressWarnings( "unchecked" )
	public void submitOrder( ) throws InterruptedException {
		List<Item> items = IntStream.range( 0, 25 )
				.mapToObj( i -> new Item( "Item" + i / 3,
						( int ) ( Math.random( ) * 10 ),
						( int ) ( Math.random( ) * 10 ),
						( int ) ( Math.random( ) * 100 ) ) )
				.collect( Collectors.toList( ) );



		View v = new ViewImpl( new RequestDispatcher( ) {
			@Override
			public void register ( RequestHandler handler ) { }

			@Override
			public void unregister ( RequestHandler handler ) { }

			@Override
			public <T> T askFor ( Request request, String... args ) {
				CompletableFuture<Vector<?>> ret = new CompletableFuture<>( );
				ret.complete( new Vector<>( items.stream( )
						.map( Item::getItemId ).collect( Collectors.toList( ) ) ) );
				return ( T ) ret;
			}
		} );
		v.update( items );
		Thread.sleep( 20000 );
	}
}
