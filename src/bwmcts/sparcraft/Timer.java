/**
* This file is based on and translated from the open source project: Sparcraft
* https://code.google.com/p/sparcraft/
* author of the source: David Churchill
**/
package bwmcts.sparcraft;

public class Timer {

	private long startTimeInMicroSec;
	private long endTimeInMicroSec; 
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
	
	public long getElapsedTimeInMicroSec(){
		return System.currentTimeMillis()-startTimeInMicroSec;}
	public long getElapsedTimeInMilliSec(){
		return this.getElapsedTimeInMicroSec();
	}
	public	long getElapsedTimeInSec(){
		return (long)(this.getElapsedTimeInMicroSec() * 0.001);
	}
	public	long getElapsedTime(){
		return this.getElapsedTimeInSec();
}
}
