package nl.tjonahen.iptableslogd.domain;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PortNumbers is a singleton to determine if a port number is known (has a description), if we can ignore it, or if it
 * is a known attack vector.
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
    private final Properties properties = new Properties();

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
    private static final Set<String> IGNORABLE_SET = new TreeSet<String>();

    static {
        IGNORABLE_SET.add("67");
        IGNORABLE_SET.add("68");
        IGNORABLE_SET.add("137");
        IGNORABLE_SET.add("138");
        IGNORABLE_SET.add("139");
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
        return IGNORABLE_SET.contains(port);
    }

    // these portnumbers are known attack ports
    private static final Set<String> ATTACK_SET = new TreeSet<String>();

    static {
        ATTACK_SET.add("21"); // FTP
        ATTACK_SET.add("23"); // TELNET
        ATTACK_SET.add("25"); // SMTP
        ATTACK_SET.add("80"); // HTTP
        ATTACK_SET.add("110"); // POP3
        ATTACK_SET.add("135");
        ATTACK_SET.add("139"); // Scan for NETBIOS suspectability (port 139)
        ATTACK_SET.add("445"); // Scan for Windows file sharing suspectability (port
        // 445)
        ATTACK_SET.add("8080"); // Scan for firewall remote login (port 8080)
        ATTACK_SET.add("3389"); // Microsoft Remote Desktop vulnerable (port 3389)
        ATTACK_SET.add("5900"); // VNC Remote Desktop vulnerable (port 5900)
        ATTACK_SET.add("1723"); // VPN (PPTP) service open/vulnerable (port 1723)
        ATTACK_SET.add("1433"); // Microsoft SQL Server open/vulnerable (port 1433)
        ATTACK_SET.add("1521"); // Oracle database service open/vulnerable (port
        // 1521)
        ATTACK_SET.add("3306"); // MySQL database open/vulnerable (port 3306)

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
        return ATTACK_SET.contains(port);
    }

}
