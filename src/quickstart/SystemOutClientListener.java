/*
 * Copyright (c) Lightstreamer Srl
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
