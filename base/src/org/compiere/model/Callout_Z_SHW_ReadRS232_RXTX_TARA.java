/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import java.io.InputStream; 
import java.io.OutputStream; 
import java.lang.Thread;
import java.util.Properties;
import java.util.Enumeration;


/**
 *	RS 232 read, write to a field Callout
 *	
 *  @author Mario Calderon
 *  @version $Id: Callout_Z_SHW_ReadRS232,v 1.0 2011/07/08 00:51:04 marcal Exp $
 */
public class Callout_Z_SHW_ReadRS232_RXTX_TARA extends CalloutEngine
{
	/**
	 *	LeerBascula 
	 *		- called from Material Receipt
	 *  @return empty string (OK) or string (error)
	 */
	public  String LeerBascula (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		// Parameters
		//String portName = "/dev/ttyS0"; //Linux. Wird nicht verwendet, da automatisch gewonnen.
		String portName = "COM1";  //Windows. Wird nicht verwendet, da automatisch gewonnen.
		String portDescriptor = "SHW: PESAR-RXTX";
		int openPortTime = 3000;
		int baudRate = 9600;
		int dataBits = SerialPort.DATABITS_7;
		int stopBits = SerialPort.STOPBITS_1;
		int parity = SerialPort.PARITY_EVEN;
		int flowControl = SerialPort.FLOWCONTROL_NONE;
		int afterOpenTime = 1000;
		
		// Temp Variables
        CommPortIdentifier portIdentifier=null;
        CommPort commPort = null;
        String portType="";
        //String portName="";
        
        // Temp Variables - read
        SerialPort serialPort = null;
        InputStream inputStream=null;
	    int inputStreamCount;
		byte[] readBuffer = new byte[255];
		String delimiter = ".";
	    int endIndex;
		int afterReadTime = 2000;

        // Temp Variables - write
	    OutputStream outputStream=null;
	    //String outputString = "987654.32 PESO astdsdsakk";  // TEST
	    String outputString = "P\r";  // in echt. \r wegen CR
		int afterWriteTime = 1000;
	    
	    // Results
	    String weightString;
	    double weightDouble = 0;  // ab 7 Stellen, lieber als float definieren, da sonst auf wissenschaftliche Notation umsteigt
        
	    // Port oeffnen
        System.out.println("RS 232: port oeffnen start");
        try {
        	// Ueberpruefung, ob Port seriell ist.
        	// Auch: BS-bedingten Name umgehen (angenommen, nur 1 serieller Port ist vorhanden)
        	/*Enumeration ports = CommPortIdentifier.getPortIdentifiers();
            while (ports.hasMoreElements()) {
              CommPortIdentifier port = (CommPortIdentifier)ports.nextElement();
              switch (port.getPortType()) {
                case CommPortIdentifier.PORT_PARALLEL:
                  //portType = "Parallel"; 
                  break;
                case CommPortIdentifier.PORT_SERIAL:
                  portType = "Serial"; 
                  portName = port.getName();
                  break;
                default:  /// Shouldn't happen
                 // portType = "Unknown"; 
                  break;
              }
              System.out.println("RS 232: Port "+ port.getName() + " is from type " + portType);
            }

            if (portType.compareTo("Serial")!=0)
    			return("Error: Serial port identifier not existent");  */  	
        	
        	//System.setProperty("gnu.io.rxtx.SerialPorts", portName);
        	portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        	if (portIdentifier==null)
    			return("Error: Port identifier not found");
        	else if (portIdentifier.isCurrentlyOwned())
    			return("Error: Port is currently in use");
    		
    		commPort = portIdentifier.open(portDescriptor, openPortTime);
    		if (commPort==null)
    			return("Error: Port could not be opened");
    		
			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
	        	if (serialPort==null) 
	    			return("Error: serialPort==null");
				serialPort.setSerialPortParams(baudRate, dataBits,stopBits, parity);
				//serialPort.setFlowControlMode(flowControl);  // WAS IST DASSSSS???
			} else 
				return("Error: only serial port allowed");
			Thread.sleep(afterOpenTime);
			
			// Port Beschreiben
			System.out.println("RS 232: port beschreiben start");
        	outputStream = serialPort.getOutputStream();
        	if (outputStream==null) 
    			return("Error: outputStream not found");

        	outputStream.write(outputString.getBytes());
        	outputStream.flush();  // HABE ICH IRGENDWO GELESEN!!!!, auch AUS DEM GOOGLE BOOK
        	System.out.println("RS 232: " + outputStream.toString());  // TEST, um zu wissen, ob die Dateun im Stream sind.
        	outputStream.close(); // AUS DEM GOOGLE BOOK
			Thread.sleep(afterWriteTime);
			
			// Port Lesen
			/* */
			System.out.println("RS 232: port lesen start");
		    inputStream = serialPort.getInputStream();
        	if (inputStream==null)
    			return("Error: inputStream not found");
        	
        	inputStreamCount = inputStream.read(readBuffer); 
        	inputStream.close();
        	if (inputStreamCount==0)
    			return("Error: no data read");
			else if (inputStreamCount==-1)
    			return("Error: stream already at EOF");
        	
            String tempString = new String(readBuffer,0,inputStreamCount,"US-ASCII");
            System.out.println("RS 232: Gelesen wurde: " + tempString);
            tempString = tempString.toLowerCase();
            endIndex = tempString.indexOf("lb");  // Geraet liefert XXXX LB oder XXXXXXX lb
            /*if(tempString.charAt(0).)*/
            
            //endIndex = tempString.indexOf(".") + 3;
            //weightString = tempString.substring(0, endIndex);
            weightString = tempString.substring(1, endIndex);
            if (weightString.isEmpty())
            	return("Error: no data read");
            else
            	weightDouble = Double.valueOf(weightString);  // neu im TEST
        	    //weightDouble = Integer.valueOf(weightString).intValue(); 
			Thread.sleep(afterReadTime);
		
			// Feld im Fenster beschreiben
			System.out.println("RS 232: Feld im Fenster aktualisieren");
			mTab.setValue("Weight_tare", weightDouble); // als Code
			//mTab.setValue("Description", "1234.55"); // als Code -TEST
			//A_Tab.setValue("Description", "1234.55"); // als Script	
    	}	// try
        catch (Exception e) {
         e.printStackTrace();
         return e.toString();
    	}
        finally{	
		    // Port Schliessen
            System.out.println("RS 232: port schliessen start");
        	commPort.close();
        } 
		
        return "";
	}  // LeerBascula
	
}  //	Callout_Z_SHW_ReadRS232
