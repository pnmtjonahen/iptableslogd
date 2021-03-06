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
package nl.tjonahen.iptableslogd.output;

import java.io.OutputStream;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.tjonahen.iptableslogd.domain.LogEntryCollector;
import nl.tjonahen.iptableslogd.domain.LogEntryStatistics;
import nl.tjonahen.iptableslogd.domain.PortNumbers;
import nl.tjonahen.iptableslogd.jmx.Configuration;

/**
 *
 * @author Philippe Tjon - A - Hen
 */
@Singleton
public class HttpRequestHandlerFactory {

    @Inject
    private LogEntryCollector logEntryCollector;

    @Inject
    private LogEntryStatistics logEntryStatistics;

    @Inject
    private Configuration config;
    
    @Inject
    private PortNumbers portNumbers;

    public HttpRequestHandler createHandler(OutputStream outputStream) {
        return new HttpRequestHandler(config.getUseReverseLookup(), outputStream, logEntryCollector, logEntryStatistics, portNumbers);
    }

}
