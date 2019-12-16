import org.junit.Test;

public class TestOrders {

	TestOrdersAgent agent;

	@Test
	public void testFindItems( ) throws InterruptedException {
		new Thread( ( ) -> TestOrdersAgent.startAg( this ) ).start( );
		Thread.sleep( 1000 );
		agent.makeOrder( "" );
		Thread.sleep( 1000 );
		agent.stop( );
		Thread.sleep( 20000000 );
	}

}
