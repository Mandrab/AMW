package Controller;

public interface ViewUpdater {

	void start( );

	void setPause( boolean val );

	void terminate( );

	boolean isRunning( );

	boolean isTerminated( );

}
