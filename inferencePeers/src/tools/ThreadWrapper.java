package tools;

public class ThreadWrapper extends Thread{
	
	ThreadWrappable _tw;
	public ThreadWrapper(ThreadWrappable tw){
		_tw= tw;
	}
	
	public void run(){
		_tw.runInThread();
	}

}
