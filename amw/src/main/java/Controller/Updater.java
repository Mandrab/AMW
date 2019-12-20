package Controller;

public interface Updater {

	void start();

	void setPause(boolean val);

	void terminate();

	boolean isRunning();

	boolean isTerminated();

}
