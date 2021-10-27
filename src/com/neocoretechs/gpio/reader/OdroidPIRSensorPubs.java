package com.neocoretechs.gpio.reader;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.ros.concurrent.CancellableLoop;
import org.ros.internal.node.server.ThreadPoolManager;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.OdroidC1Pin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.platform.Platform;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.platform.PlatformManager;
import com.pi4j.util.CommandArgumentParser;
import com.pi4j.util.Console;
import com.pi4j.util.ConsoleColor;


public class OdroidPIRSensorPubs extends AbstractNodeMain {
	   private static boolean DEBUG = true;;
	   private volatile static boolean shouldRun = true;
		private CountDownLatch awaitStart = new CountDownLatch(1);
		public ConcurrentLinkedQueue<String> pubdata = new ConcurrentLinkedQueue<String>();
	    GpioPinDigitalInput input;
		@Override
		public GraphName getDefaultNodeName() {
			return GraphName.of("pubs_pirsensor1");
		} 

		@Override
		public void onStart(final ConnectedNode connectedNode) {
			ThreadPoolManager.init(new String[] {"SYSTEM"}, true);
			//final Log log = connectedNode.getLog();
			final Publisher<std_msgs.String> statpub =
					connectedNode.newPublisher("robocore/alerts", std_msgs.String._TYPE);
		    try {
				PlatformManager.setPlatform(Platform.ODROID);
			} catch (PlatformAlreadyAssignedException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		    // create gpio controller
		    final GpioController gpio = GpioFactory.getInstance();
		    // ####################################################################
		    //
		    // IF YOU ARE USING AN ODROID C1/C1+ PLATFORM, THEN ...
		    //    When provisioning a pin, use the OdroidC1Pin class.
		    //
		    // ####################################################################
		    Pin pin;
		    // by default we will use gpio pin #01;
		    pin = OdroidC1Pin.GPIO_01;  // default pin if no pin argument found                           
		    // provision gpio pin as an output pin and turn on
		    input = gpio.provisionDigitalInputPin(pin, "Input");		   
			PIRPing up = new PIRPing();
			ThreadPoolManager.getInstance().spin(up, "SYSTEM");
			// tell the waiting constructors that we have registered publishers if we are intercepting the command line build process
			awaitStart.countDown();
			// This CancellableLoop will be canceled automatically when the node shuts
			// down.
			connectedNode.executeCancellableLoop(new CancellableLoop() {
				std_msgs.String statmsg = statpub.newMessage();
				@Override
				protected void setup() {
					
				}

				@Override
				protected void loop() throws InterruptedException {
				    try {
						awaitStart.await();
					} catch (InterruptedException e) {}
					if( !pubdata.isEmpty()) {
						String sDist = pubdata.poll();
						if(sDist != null) {
							statmsg.setData(sDist);
							statpub.publish(statmsg);
						}
					}
					Thread.sleep(1);
				}
			});
		}
		
	/**
     *
     * @param args
     * @throws InterruptedException
     * @throws PlatformAlreadyAssignedException
     */
    //public static void main(String[] args) throws InterruptedException, PlatformAlreadyAssignedException {
	class PIRPing implements Runnable {	
		@Override
		public void run() {
			// main production code...
			PinState pirState = PinState.LOW;
			PinState val;
			while(shouldRun) {
				val = input.getState();
				if(val.equals(PinState.HIGH)) {
					if(pirState.equals(PinState.LOW)) {
						//alert
						if(DEBUG)
							System.out.println("ALERT!!");
						pubdata.add("99"); // dummy value in typical ultrasonic range
						pirState = PinState.HIGH;
					}
				} else {
					if(pirState.equals(PinState.HIGH)) {
						pirState = PinState.LOW;
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					shouldRun = false;
				}
			}
		}
	}
}
