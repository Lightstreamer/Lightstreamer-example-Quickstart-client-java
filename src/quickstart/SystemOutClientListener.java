/*
 * Copyright (c) 2004-2015 Weswit s.r.l., Via Campanini, 6 - 20124 Milano, Italy.
 * All rights reserved.
 * www.lightstreamer.com
 *
 * This software is the confidential and proprietary information of
 * Weswit s.r.l.
 * You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered
 * into with Weswit s.r.l.
 */
package quickstart;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.LightstreamerClient;

public class SystemOutClientListener implements ClientListener {

    @Override
    public void onListenEnd(LightstreamerClient client) {
      System.out.println("Stops listening to client events"); 
    }

    @Override
    public void onListenStart(LightstreamerClient client) {
      System.out.println("Start listening to client events");
      
    }

    @Override
    public void onPropertyChange(String property) {
      System.out.println("Client property changed: " + property);
    }

    @Override
    public void onServerError(int code, String message) {
      System.out.println("Server error: " + code + ": " + message);
    }

    @Override
    public void onStatusChange(String newStatus) {
      System.out.println("Connection status changed to " + newStatus);
    }

  
  
}
