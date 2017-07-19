import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.ArrayList;

import helpers.Wisard;

public class MyAutomatedAgent TimerTask{
	public static volatile long interval = 300; // 5 min default sampling rate
	public static volatile AutomatedAgent agent = null;
	public static volatile Wisard wisard;
	public static volatile int hub;
	public static volatile byte taskID_hi;
	public static volatile byte taskID_lo;
	public static volatile byte fieldID;

	public void run(){
		toggleInterval();
		scheduleTask(1);
	}

	public void toggleInterval(){
		//  - make a NetManagementCommand subclass named SetSamplingRate
		
		interval = interval == 60 ? 300:60;

		SetSamplingRate cmd = new SetSamplingRate(interval);
		AutomatedAgentController.runCommand(cmd, agent);
	}

	public static void scheduleTask(int numberOfHours){
			// get current time
			LocalDateTime currentTime = LocalDateTime.now(Clock.systemUTC());
			Calendar calendar = Calendar.getInstance();

			// get target date time
			calendar.setTime(currentTime);
			calendar.add(Calendar.HOUR_OF_DAY, numberOfHours);
			LocalDateTime target = calendar.getTime();

			// schedule task
			Timer timer = new Timer();
			timer.schedule(new MyAutomatedAgent(), target);
	}

	public static void main(String[] args){
		// to do - set wisard, hub, taskID_hi, taskID_lo, fieldID,
		if(args.length < 6){
			throw new IllegalArgumentException("You must provide site, wisard ID, hub ID, taskID_hi, taskID_lo, and fieldID");
		}

		// assign passed in command line arguments
		String site = args[0];
		int wisardID = Integer.parseInt(args[1]);
		try{
			wisard = Wisard.getByID(siteID, wisardID);
		} catch(Exception e){
			throw new IllegalArgumentException("WiSARD " + wisardID + " was not found at site " + site);
		}
		hub = Integer.parseInt(args[2]);
		taskID_hi = Integer.parseInt(args[3]);
		taskID_lo = Integer.parseInt(args[4]);
		fieldID = Integer.parseInt(args[5]);

		// empty list of subscriptions since this open-loop example does not care about sensor readings
		ArrayList<SubscriptionInfo> subscriptions = new ArrayList<SubscriptionInfo>();

		// an example of how to use the AutomatedUserController to create an AutomatedUser
		agent = AutomatedAgentController.createAutomatedAgent("username", "password", subscriptions, (Message msg) -> {
			// Do Nothing - this open-loop example does not care about sensor reading values and is never called
		}, (Exception e) -> {
			// prints exception to std error
			System.err.println(e);
		});
		new Thread(agent).start();

		scheduleTask(1);
	}
}