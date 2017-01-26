/*
 * Copyright (C) 2017 Philippe Tjon - A - Hen philippe@tjonahen.nl
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

package nl.tjonahen.iptableslogd.cdi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;

/**
 *
 * @author Philippe Tjon - A - Hen
 */
@Singleton
public class PropertyResolver {
    private static final Logger LOGGER = Logger.getLogger(PropertyResolver.class.getName());


    /**
    * Returns property held under specified <code>key</code>. If the value is supposed to be of any other
    * type than {@link String}, it's up to the client to do appropriate casting.
    *
    * @param key the search key
    * @return value for specified <code>key</code> or null if not defined.
    */
    public String getValue(String key) {
        LOGGER.fine(() -> String.format("Resolv %s", key));
        return System.getProperty(key);
    }
}
