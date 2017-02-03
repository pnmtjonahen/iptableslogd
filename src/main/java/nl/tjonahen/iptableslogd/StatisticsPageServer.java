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

import nl.tjonahen.iptableslogd.jmx.Configuration;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.tjonahen.iptableslogd.cdi.Value;


import nl.tjonahen.iptableslogd.output.HttpRequestHandlerFactory;
import nl.tjonahen.iptableslogd.output.RequestThreadPool;
/**
 * StatisticsPage server is a http server that renders a iptables dropped packages statistics view.
 * 
 * @author Philippe Tjon - A - Hen philippe@tjonahen.nl
 */
@Singleton
public final class StatisticsPageServer {

    private static final Logger LOGGER = Logger.getLogger(StatisticsPageServer.class.getName());

    @Inject
    private HttpRequestHandlerFactory handlerFactory;
    
    @Inject
    private RequestThreadPool pool;

    @Inject
    @Value("4080")
    private int port;

    private boolean canContinue = true;
    
    /**
     * Starts the server socket accept connection loop
     */
    public void start() {
        final ServerSocket serverSocket = openServerSocket();

        while (canContinue) {
            try {
                serverSocket.setSoTimeout(10);
                final Socket socket = serverSocket.accept();
                LOGGER.fine(() -> String.format("New connection accepted %s:%s", socket.getInetAddress(), socket.getPort()));
                final OutputStream outputStream = socket.getOutputStream();

                // Construct handler to process the HTTP request message.
                pool.runAsync(handlerFactory.createHandler(outputStream)).thenAccept((Void) -> silentlyClose(socket, outputStream));
            } catch (SocketTimeoutException e) {
                // ignore time outs
            } catch (IOException e) {
                LOGGER.severe(() -> "Fire worker thread:" + e.getMessage());
            }
        }
        LOGGER.info("Exit.");
    }

    private ServerSocket openServerSocket() throws IllegalStateException {
        try {
            final ServerSocket serverSocket = new ServerSocket(port);
            LOGGER.info(() -> String.format("http listner running on port %s", serverSocket.getLocalPort()));
            return serverSocket;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create server socket ", e);
        }
    }

    private void silentlyClose(final Socket socket, final OutputStream outputStream) {
        try {
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error close socket ", e);
        }

    }
    
    public void update(final @Observes Configuration c) {
        canContinue = c.canContinue();
    }
}
