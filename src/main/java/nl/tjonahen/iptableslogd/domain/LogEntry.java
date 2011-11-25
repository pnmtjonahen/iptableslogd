package nl.tjonahen.iptableslogd.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * LogEntry java representation of a single line loged by the iptables ulog deamon.
 * @author Philippe Tjon-A-Hen
 *
 */
public class LogEntry {
	private final String dateTime;
	private String inInterface;
	private String outInterface;
	private String macAdress;
	private String source;
	private String destination;
//	private String len1;
//	private String tos;
//	private String prec;
//	private String ttl;
	private String id;
	private String protocol;
	private String sourcePort;
	private String destinationPort = "";
//	private String len2;
	
	private final Date date;
	private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");

	private static int year = Calendar.getInstance().get(Calendar.YEAR); 
	private static Logger logger = Logger.getLogger(LogEntry.class.getName());
	public LogEntry (String line) throws ParseException {
//Oct 23 09:25:49 nx9420 kernel: IN=eth0 OUT= MAC=ff:ff:ff:ff:ff:ff:00:14:22:f2:f9:c2:08:00 SRC=192.168.0.105 DST=192.168.0.255 LEN=78 TOS=0x00 PREC=0x00 
//		TTL=128 ID=21157 PROTO=UDP SPT=137 DPT=137 LEN=58
		
		dateTime = line.substring(0,15);
		logger.log(Level.INFO, "Current date :" + "" + year + " " + dateTime);
		date = fmt.parse("" + year + " " + dateTime);
		String[] elements = line.split(" ");
		
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].startsWith("IN")) {
				inInterface = elements[i].substring(3);
			} else if (elements[i].startsWith("OUT")) {
				outInterface = elements[i].substring(4);
			} else if (elements[i].startsWith("MAC")) {
				macAdress = elements[i].substring(4);
			} else if (elements[i].startsWith("SRC")) {
				source = elements[i].substring(4);
			} else if (elements[i].startsWith("DST")) {
				destination = elements[i].substring(4);
//			} else if (elements[i].startsWith("LEN")) {
//				if (len1 == null)
//					len1 = elements[i].substring(4);
//				else
//					len2 = elements[i].substring(4);
//			} else if (elements[i].startsWith("TOS")) {
//				tos = elements[i].substring(4);
//			} else if (elements[i].startsWith("PREC")) {
//				prec = elements[i].substring(5);
//			} else if (elements[i].startsWith("TTL")) {
//				ttl = elements[i].substring(4);
			} else if (elements[i].startsWith("ID")) {
				id = elements[i].substring(3);
			} else if (elements[i].startsWith("PROTO")) {
				protocol = elements[i].substring(6);
				if ("2".equals(protocol)) {
					protocol="IGMP"; // Internet Group Management Protocol 
				}
			} else if (elements[i].startsWith("SPT")) {
				sourcePort = elements[i].substring(4);
			} else if (elements[i].startsWith("DPT")) {
				destinationPort = elements[i].substring(4);
			}
		}
	}

	public final String getDateTime() {
		return dateTime;
	}

	public final String getSource() {
		return source;
	}

	public final String getDestinationPort() {
		return destinationPort;
	}

	public final String getProtocol() {
		return protocol;
	}
	
	
	public final String getInInterface() {
		return inInterface;
	}

	public final String getOutInterface() {
		return outInterface;
	}

	public final String getMacAdress() {
		return macAdress;
	}

	public final String getDestination() {
		return destination;
	}

	public final String getId() {
		return id;
	}

	public final String getSourcePort() {
		return sourcePort;
	}
	public final Date getDate() {
		return new Date(date.getTime());
	}
	

	public final String portDestinationName() {
		return PortNumbers.instance().getDescription(destinationPort, protocol);
	}
	public final String portSourceName() {
		return PortNumbers.instance().getDescription(sourcePort, protocol);
	}

	public final boolean canIgnore() {
		return PortNumbers.instance().canIgnorePort(destinationPort);
	}
	
	public final boolean isAttack() {
		return PortNumbers.instance().isKnownAttackPort(destinationPort);
	}
	
}
