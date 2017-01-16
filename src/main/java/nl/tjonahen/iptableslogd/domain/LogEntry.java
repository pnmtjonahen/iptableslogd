package nl.tjonahen.iptableslogd.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 * LogEntry java representation of a single line loged by the iptables ulog deamon.
 *
 * @author Philippe Tjon-A-Hen
 *
 */
public class LogEntry {

    private final String dateTime;
    private String inInterface = "";
    private String outInterface  = "";
    private String macAdress = "";
    private String source = "";
    private String destination = "";
//	private String len1;
//	private String tos;
//	private String prec;
//	private String ttl;
    private String id = "";
    private String protocol = "";
    private String sourcePort = "";
    private String destinationPort = "";
//	private String len2;

    private final Date date;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");

    private static final int YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final Logger LOGGER = Logger.getLogger(LogEntry.class.getName());

    public LogEntry(String line) throws ParseException {
//Oct 23 09:25:49 nx9420 kernel: IN=eth0 OUT= MAC=ff:ff:ff:ff:ff:ff:00:14:22:f2:f9:c2:08:00 SRC=192.168.0.105 DST=192.168.0.255 LEN=78 TOS=0x00 PREC=0x00 
//		TTL=128 ID=21157 PROTO=UDP SPT=137 DPT=137 LEN=58
//Jun 18 16:10:09 eb8740w  IN=wlan0 OUT= MAC=01:00:5e:00:00:fb:dc:86:d8:21:50:94:08:00 SRC=145.89.78.8 DST=224.0.0.251 LEN=248 TOS=00 PREC=0x00 
//              TTL=255 ID=33635 PROTO=UDP SPT=5353 DPT=5353 LEN=228 MARK=0 
        dateTime = line.substring(0, 15);
        LOGGER.fine(() -> String.format("Current date :%d %s", YEAR, dateTime));
        date = fmt.parse("" + YEAR + " " + dateTime);
        String[] elements = line.split(" ");

        for (String element : elements) {
            if (element.startsWith("IN")) {
                inInterface = element.substring(3);
            } else if (element.startsWith("OUT")) {
                outInterface = element.substring(4);
            } else if (element.startsWith("MAC")) {
                macAdress = element.substring(4);
            } else if (element.startsWith("SRC")) {
                source = element.substring(4);
            } else if (element.startsWith("DST")) {
                destination = element.substring(4);
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
            } else if (element.startsWith("ID")) {
                id = element.substring(3);
            } else if (element.startsWith("PROTO")) {
                protocol = element.substring(6);
                if ("2".equals(protocol)) {
                    protocol = "IGMP"; // Internet Group Management Protocol 
                }
            } else if (element.startsWith("SPT")) {
                sourcePort = element.substring(4);
            } else if (element.startsWith("DPT")) {
                destinationPort = element.substring(4);
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
