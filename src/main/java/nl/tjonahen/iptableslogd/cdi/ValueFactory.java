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

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory to produce Values.
 * 
 * Properties are found first by key (if specified) then by fully qualified field name, and lastly by only field name.
 * If no value was found and no default was given an exception is thrown.
 * 
 * @author Philippe Tjon - A - Hen
 */
@Singleton
public class ValueFactory {

    @Inject
    private PropertyResolver resolver;

    @Produces
    @Value
    public String getStringValue(InjectionPoint ip) {

        // Trying with explicit key defined on the field
        final String key = ip.getAnnotated().getAnnotation(Value.class).key();
        if (!key.trim().isEmpty()) {
            return resolver.getValue(key);
        }

        // Falling back to fully-qualified field name resolving.
        final String fqn = ip.getMember().getDeclaringClass().getName() + "." + ip.getMember().getName();
        String value = resolver.getValue(fqn);

        // No luck... so perhaps just the field name?
        if (value == null) {
            value = resolver.getValue(ip.getMember().getName());
        }
        final String defaultValue = ip.getAnnotated().getAnnotation(Value.class).value();

        if (value == null && !defaultValue.trim().isEmpty()) {
            return defaultValue;
        }    
        // No can do - no value found and no default specified.
        if (value == null) {
            throw new IllegalStateException("No value defined for field: " + fqn);
        }

        return value;
    }

    @Produces
    @Value
    public Integer getIntegerValue(InjectionPoint ip) {
        final String value = getStringValue(ip);
        return (value != null) ? Integer.valueOf(value) : null;
    }
    
}
