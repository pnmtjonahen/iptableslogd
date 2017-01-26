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
package nl.tjonahen.iptableslogd;

import javax.enterprise.inject.spi.CDI;
import org.jboss.weld.environment.se.Weld;

/**
 *
 * @author Philippe Tjon - A - Hen
 */
public class Application {

    public static void main(String[] args) {        
        final Weld weld = new Weld();
        weld.initialize();
        final StatisticsPageServer application = CDI.current().select(StatisticsPageServer.class).get();
        try {
            application.start();
        } finally {
            weld.shutdown();
        }
    }

}
