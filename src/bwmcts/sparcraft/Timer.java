package bwmcts.sparcraft;

public class Timer {

	private float startTimeInMicroSec;
	private float endTimeInMicroSec; 
	private int    stopped;                             // stop flag 

	
	public Timer(){
		stopped = 0;
		startTimeInMicroSec = 0;
		endTimeInMicroSec = 0;
			
		start();
	}

	public void start(){
		startTimeInMicroSec=System.currentTimeMillis();
	}
	
	public void stop(){
		stopped=1;
		endTimeInMicroSec=System.currentTimeMillis();
	}
	
	public double getElapsedTimeInMicroSec(){
		return System.currentTimeMillis()-startTimeInMicroSec;}
	public double getElapsedTimeInMilliSec(){
		return this.getElapsedTimeInMicroSec();
	}
	public	double getElapsedTimeInSec(){
		return this.getElapsedTimeInMicroSec() * 0.001;
	}
	public	double getElapsedTime(){
		return this.getElapsedTimeInSec();
}
}
