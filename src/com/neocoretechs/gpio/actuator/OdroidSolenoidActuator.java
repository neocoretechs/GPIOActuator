package com.neocoretechs.gpio.actuator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
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


public class OdroidSolenoidActuator {
	   private static boolean DEBUG = true;;
	   private static boolean shouldRun = true;
	/**
     *
     * @param args
     * @throws InterruptedException
     * @throws PlatformAlreadyAssignedException
     */
    public static void main(String[] args) throws InterruptedException, PlatformAlreadyAssignedException {

        // ####################################################################
        //
        // since we are not using the default Raspberry Pi platform, we should
        // explicitly assign the platform as the Odroid platform.
        //
        // ####################################################################
        PlatformManager.setPlatform(Platform.ODROID);

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // ####################################################################
        //
        // IF YOU ARE USING AN ODROID C1/C1+ PLATFORM, THEN ...
        //    When provisioning a pin, use the OdroidC1Pin class.
        //
        // ####################################################################
        Pin[] pins = new Pin[6];
        // by default we will use gpio pin #01; however, if an argument
        // has been provided, then lookup the pin by address
        pins[0] = CommandArgumentParser.getPin(
                OdroidC1Pin.class,    // pin provider class to obtain pin instance from
                OdroidC1Pin.GPIO_01  // default pin if no pin argument found
                );                // argument array to search in
        pins[1] = CommandArgumentParser.getPin(
                OdroidC1Pin.class,    // pin provider class to obtain pin instance from
                OdroidC1Pin.GPIO_04  // default pin if no pin argument found
                );                // argument array to search in
        pins[2] = CommandArgumentParser.getPin(
                OdroidC1Pin.class,    // pin provider class to obtain pin instance from
                OdroidC1Pin.GPIO_05  // default pin if no pin argument found
                );                // argument array to search in
        pins[3] = CommandArgumentParser.getPin(
                OdroidC1Pin.class,    // pin provider class to obtain pin instance from
                OdroidC1Pin.GPIO_06  // default pin if no pin argument found
                );                // argument array to search in
        pins[4] = CommandArgumentParser.getPin(
                OdroidC1Pin.class,    // pin provider class to obtain pin instance from
                OdroidC1Pin.GPIO_10  // default pin if no pin argument found
                );                // argument array to search in
        pins[5] = CommandArgumentParser.getPin(
                OdroidC1Pin.class,    // pin provider class to obtain pin instance from
                OdroidC1Pin.GPIO_11  // default pin if no pin argument found
                );                // argument array to search in
        // provision gpio pin as an output pin and turn on
        GpioPinDigitalOutput output[] = new GpioPinDigitalOutput[6];
        for(int i = 0; i < output.length; i++) {
        	output[i] = gpio.provisionDigitalOutputPin(pins[i], ("Output"+i), PinState.LOW);
        	// set shutdown state for this pin: keep as output pin, set to low state
        	output[i].setShutdownOptions(false, PinState.LOW);
        }
        // create a pin listener to print out changes to the output gpio pin state
        // debug mode..
        if(DEBUG) {
        	 // create Pi4J console wrapper/helper
             // (This is a utility class to abstract some of the boilerplate code)
             final Console console = new Console();
        	 // print program title/header
             console.title("<-- The Bardy Project -->", "Chicken Actuator");
             // allow for user to exit program using CTRL-C
             console.promptForExit();
             for(int i = 0; i < pins.length; i++) {
             output[i].addListener(new GpioPinListenerDigital() {
                	@Override
                	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                		// display pin state on console
                		console.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " +
                        ConsoleColor.conditional(
                                event.getState().isHigh(), // conditional expression
                                ConsoleColor.GREEN,        // positive conditional color
                                ConsoleColor.RED,          // negative conditional color
                                event.getState()));        // text to display
                	}
                });
                // prompt user that we are ready
                console.println(" ... Successfully provisioned output pin: " + output[i].toString());
                console.emptyLine();
                console.box("The GPIO output pin states will cycle HIGH and LOW states now.");
                console.emptyLine();
                // notify user of current pin state
                console.println("--> [" + output[i].toString() + "] state was provisioned with state = " +
                		ConsoleColor.conditional(
                        output[i].getState().isHigh(), // conditional expression
                        ConsoleColor.GREEN,         // positive conditional color
                        ConsoleColor.RED,           // negative conditional color
                        output[i].getState()));        // text to display
             }

             // wait
             Thread.sleep(500);
             console.emptyLine();
        }
        // test mode..
      	if(args.length > 0 && args[0].equals("test")) {
    		// yozzle the actuators to make sure they are properly connected
    		// do it forever until we break and restart normally
    		for(;;) {
    			for(int i = 0; i < output.length; i++) {
    		      	for(int j=0; j < 4; j++) {
    	        		output[i].high(); // fire it off
    	        		Thread.sleep(250);
    	        		output[i].low();
    	        	}
    				output[i].high();
    				Thread.sleep(2000); // pull it for 2 seconds
    				output[i].low();
    			}
    		}
    	}
      	// main production code...
     	File f = new File("/home/odroid/lastFeedDrop.dat");
     	if(f.exists())
     		f.delete();
    	int actuator = 0;
        while(shouldRun) {
        	if(args.length > 0 && args[0].equals("test2")) {
        		Thread.sleep(5000); // 5 seconds sleep
        	} else {
        		Thread.sleep(86400000); // 24 hours sleep
        	}
        	// read last actuation when we start or at every iteration
        	f = new File("/home/odroid/lastFeedDrop.dat");
      		FileInputStream fis = null;
        	if(f.exists()) {
				try {
					fis = new FileInputStream(f);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
        		try {
					actuator = fis.read();
					fis.close();
    			} catch (IOException e) {
					e.printStackTrace();
				}
          		++actuator; // bump to next
        	}
        	if(actuator >= pins.length) {
             	// stop all GPIO activity/threads by shutting down the GPIO controller
            	// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
            	gpio.shutdown();
        		System.exit(0); // we are all done, no more food to drop
        	}
        	// vibrate it
        	for(int j=0; j < 4; j++) {
        		output[actuator].high(); // fire it off
        		Thread.sleep(250);
        		output[actuator].low();
        	}
    		output[actuator].high(); // fire it off
        	Thread.sleep(2000); // hold it for 2 seconds
        	output[actuator].low();
        	// write the actuator we just fired, in case we fail while waiting and restart
        	FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        	// write it out and record that we fired it
        	try {
				fos.write(actuator);
				fos.flush();
				fos.close();
    		} catch (IOException e) {
				System.out.println("UNABLE TO WRITE ACUATOR FILE!!! "+f);
				e.printStackTrace();
				// if we cant write it, we have to increment actuator anyway to activate next one
				++actuator;
			}
        }
    }
}
