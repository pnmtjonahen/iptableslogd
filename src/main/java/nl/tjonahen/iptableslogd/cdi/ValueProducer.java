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
 *
 * @author Philippe Tjon - A - Hen
 */
@Singleton
public class ValueProducer {

    @Inject
    private PropertyResolver resolver;

    /**
    * Main producer method - tries to find a property value using following keys:
    *
    * <ol>
    * <li><code>key</code> property of the {@link Value} annotation (if defined but no key is
    * found - returns null),</li>
    * <li>fully qualified field class name, e.g. <code>eu.awaketech.MyBean.myField</code> (if value is null,
    * go along with the last resort),</li>
    * <li>field name, e.g. <code>myField</code> for the example above (if the value is null, no can do -
    * return null)</li>
    * </ol>
    *
    * @param ip
    * @return value of the injected property or null if no value could be found.
    */
    @Produces
    @Value
    public String getStringConfigValue(InjectionPoint ip) {


        // Trying with explicit key defined on the field
        final String key = ip.getAnnotated().getAnnotation(Value.class).key();
        final String defaultValue = ip.getAnnotated().getAnnotation(Value.class).value();
        
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
        if (value == null && !defaultValue.trim().isEmpty()) {
            return defaultValue;
        }    
        // No can do - no value found and no default specified.
        if (value == null) {
            throw new IllegalStateException("No value defined for field: " + fqn);
        }

        return value;
    }

    /**
    * Produces {@link Double} type of property from {@link String} type.
    *
    * <p>
    * Will throw {@link NumberFormatException} if the value cannot be parsed into a {@link Double}
    * </p>
    *
    * @param ip
    * @return value of the injected property or null if no value could be found.
    *
    * @see ValueProducer#getStringConfigValue(InjectionPoint)
    */
    @Produces
    @Value
    public Double getDoubleConfigValue(InjectionPoint ip) {
        String value = getStringConfigValue(ip);

        return (value != null) ? Double.valueOf(value) : null;
    }

    /**
    * Produces {@link Integer} type of property from {@link String} type.
    *
    * <p>
    * Will throw {@link NumberFormatException} if the value cannot be parsed into an {@link Integer}
    * </p>
    *
    * @param ip
    * @return value of the injected property or null if no value could be found.
    *
    * @see ValueProducer#getStringConfigValue(InjectionPoint)
    */
    @Produces
    @Value
    public Integer getIntegerConfigValue(InjectionPoint ip) {
        String value = getStringConfigValue(ip);

        return (value != null) ? Integer.valueOf(value) : null;
    }
    
    
}
