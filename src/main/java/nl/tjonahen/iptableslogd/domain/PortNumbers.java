/*
 * Copyright (C) 2017 Philippe Tjon - A - Hen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.tjonahen.iptableslogd.domain;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Singleton;

/**
 * PortNumbers is a singleton to determine if a port number is known (has a description), if we can ignore it, or if it
 * is a known attack vector.
 *
 * @author Philippe Tjon-A-Hen
 *
 */
@Singleton
public final class PortNumbers {


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
        ATTACK_SET.add("445"); // Scan for Windows file sharing suspectability (port 445)
        ATTACK_SET.add("8080"); // Scan for firewall remote login (port 8080)
        ATTACK_SET.add("3389"); // Microsoft Remote Desktop vulnerable (port 3389)
        ATTACK_SET.add("5900"); // VNC Remote Desktop vulnerable (port 5900)
        ATTACK_SET.add("1723"); // VPN (PPTP) service open/vulnerable (port 1723)
        ATTACK_SET.add("1433"); // Microsoft SQL Server open/vulnerable (port 1433)
        ATTACK_SET.add("1521"); // Oracle database service open/vulnerable (port 1521)
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
