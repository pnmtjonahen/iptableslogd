package nl.tjonahen.iptableslogd.domain;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PortNumbers is a singleton to determine if a port number is known (has a
 * description), if we can ignore it, or if it is a known attack vector.
 * 
 * @author Philippe Tjon-A-Hen
 * 
 */
public final class PortNumbers {

	private static PortNumbers instance;

	public static synchronized PortNumbers instance() {
		if (instance == null) {
			try {
				instance = new PortNumbers();
			} catch (IOException e) {
				Logger.getLogger(PortNumbers.class.getName()).log(Level.SEVERE, "Error loading PortNumbers ", e);
			}
		}
		return instance;
	}

	// read known portnumbers from properties file.
	private Properties properties = new Properties();

	private PortNumbers() throws IOException {
		properties.load(this.getClass().getResourceAsStream(
				"portnumbers.properties"));
	}

	/**
	 * Get the port/protocol description.
	 * 
	 * @param portNumber
	 * @param protocol
	 * @return
	 */
	public String getDescription(String portNumber, String protocol) {
		String key = portNumber + "/" + protocol.toLowerCase(Locale.getDefault());
		if (properties.containsKey(key)) {
			return "(" + portNumber + ") " + properties.get(key);
		}
		return portNumber;
	}

	// these portnumbers can be ignored.
	private static Set<String> ignorable = new TreeSet<String>();
	static {
		ignorable.add("67");
		ignorable.add("68");
		ignorable.add("137");
		ignorable.add("138");
		ignorable.add("139");
	}

	/**
	 * Can we ignore packages coming from or going to the given port.
	 * 
	 * @param port
	 * @return
	 */
	public boolean canIgnorePort(String port) {
		if (port == null) {
			return false;
		}
		if (ignorable.contains(port)) {
			return true;
		}
		return false;
	}

	// these portnumbers are known attack ports
	private static Set<String> attack = new TreeSet<String>();
	static {
		attack.add("21"); // FTP
		attack.add("23"); // TELNET
		attack.add("25"); // SMTP
		attack.add("80"); // HTTP
		attack.add("110"); // POP3
		attack.add("135");
		attack.add("139"); // Scan for NETBIOS suspectability (port 139)
		attack.add("445"); // Scan for Windows file sharing suspectability (port
							// 445)
		attack.add("8080"); // Scan for firewall remote login (port 8080)
		attack.add("3389"); // Microsoft Remote Desktop vulnerable (port 3389)
		attack.add("5900"); // VNC Remote Desktop vulnerable (port 5900)
		attack.add("1723"); // VPN (PPTP) service open/vulnerable (port 1723)
		attack.add("1433"); // Microsoft SQL Server open/vulnerable (port 1433)
		attack.add("1521"); // Oracle database service open/vulnerable (port
							// 1521)
		attack.add("3306"); // MySQL database open/vulnerable (port 3306)

	}

	/**
	 * Is the port a known vector of attack.
	 * 
	 * @param port
	 * @return
	 */
	public boolean isKnownAttackPort(String port) {
		if (port == null) {
			return false;
		}
		if (attack.contains(port)) {
			return true;
		}
		return false;
	}

}
