/*
 * Copyright (C) 2017 ordina
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
package nl.tjonahen.iptableslogd;

import nl.tjonahen.iptableslogd.input.IPTablesLogHandler;
import nl.tjonahen.iptableslogd.jmx.Configuration;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 *
 * @author Philippe Tjon - A - Hen
 */
public class Application {

    public static void main(String[] args) {
        int port = 4080;
        int poolSize = 5;
        String ulog = "/var/log/ulogd.syslogemu";
        // Parse commandline params
        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            if ("--port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            }
            if ("-t".equals(args[i])) {
                poolSize = Integer.parseInt(args[++i]);
            }
            if ("--threads".equals(args[i])) {
                poolSize = Integer.parseInt(args[++i]);
            }
            if ("-i".equals(args[i])) {
                ulog = args[++i];
            }
            if ("--input".equals(args[i])) {
                ulog = args[++i];
            }
        }
        
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        // configure the application
        Configuration config = container.instance().select(Configuration.class).get();
        config.setPoolSize(poolSize);
        config.setUlog(ulog);
        config.setPort(port);
        // start the loog tail thread
        IPTablesLogHandler iPTablesLogHandler  = container.instance().select(IPTablesLogHandler.class).get();
        new Thread(iPTablesLogHandler).start();

        // start the user interface
        StatisticsPageServer application = container.instance().select(StatisticsPageServer.class).get();
        application.run();
        weld.shutdown();
    }

}
