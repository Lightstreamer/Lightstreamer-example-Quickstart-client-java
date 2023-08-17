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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.ClientMessageListener;
import com.lightstreamer.client.LightstreamerClient;

public class PortfolioOrderEntry {

  /**
   * Simple order entry for the portfolio demo that reads orders from the command line.
   * 
   * It shows how to connect/disconnect and send messages to Lightstreamer
   * and receive a response to the message submission.
   * It requires that Lightstreamer Server is running with the FULLPORTFOLIODEMO
   * Adapter Set installed.
   * 
   * The test is invoked in this way:
   *    java quickstart.PortfolioOrderEntry <serverAddress> <stock> <quantity>
   * where <serverAddress> stands for the full address of Lightstreamer Server
   * (e.g.: https://push.lightstreamer.com),
   * <stock> is the name of one of the stocks supported by the PortfolioDemo,
   * and <quantity> is the quantity to buy (if positive) or to sell (if negative)
   * 
   * The effect of the operation can be seen through a concurrently running
   * Portfolio demo.
   * 
   * @param args Should specify the address of the Server, stock, and quantity
   */
  public static void main(String[] args) {
    final String serverAddress = args[0];
    final String stockName = args[1];
    final int quantity = Integer.parseInt(args[2]);
    
    new PortfolioOrderEntry().start(serverAddress, stockName, Math.abs(quantity), quantity >= 0);
  }
  
  void start(String serverAddress, String stockName, int quantity, boolean isBuy) {
    
    // the portfolio demo available @ http://demos.lightstreamer.com/PortfolioDemo/
    // connects to the DEMO adapter set on push.lightstreamer.com rather than 
    // to a FULLPORTFOLIODEMO one (obviously that DEMO adapter set contains the 
    // needed adapters). 
    // For this reason, when connecting to push.lightstreamer.com we use the DEMO
    // adapter set.
    
    String adapterSet = serverAddress.contains("push.lightstreamer.com") ? "DEMO" : "FULLPORTFOLIODEMO";
    LightstreamerClient client = new LightstreamerClient(serverAddress, adapterSet);
    
    ClientListener clientListener = new SystemOutClientListener();
    client.addListener(clientListener);
    
    client.connect();
    
    CountDownLatch cdl = new CountDownLatch(1);
    ClientMessageListener sentMessageListener = new SystemOutClientMessageListener(cdl);
    
    String PREFIX = isBuy ? "BUY|" : "SELL|";
    String s = PREFIX + "portfolio1|" + stockName + "|" + quantity;
    client.sendMessage(s, "orders", 500, sentMessageListener, true);

    try {
      cdl.await();
    } catch (InterruptedException e) {
    } 

    java.util.concurrent.Future<Void> future = client.disconnectFuture();
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }
  
  
  
  
  public static class SystemOutClientMessageListener implements ClientMessageListener {
      
    private final CountDownLatch cdl;

    public SystemOutClientMessageListener(CountDownLatch cdl) {
        this.cdl = cdl;
    }
  
    @Override
    public void onAbort(String originalMessage, boolean sentOnNetwork) {
      if (sentOnNetwork) {
        System.out.println("message \""+originalMessage+"\" was aborted; is not known if it reached the server");
      } else {
        System.out.println("message \""+originalMessage+"\" was aborted and will not be sent to the server");
      }
      cdl.countDown();
    }
  
    @Override
    public void onDeny(String originalMessage, int code, String message) {
      System.out.println("message \""+originalMessage+"\" was denied by the server because of error " + code + ": " + message);
      cdl.countDown();
    }
  
    @Override
    public void onDiscarded(String originalMessage) {
      System.out.println("message \""+originalMessage+"\" was discarded by the server because it was too late when it was received");
      cdl.countDown();
    }
  
    @Override
    public void onError(String originalMessage) {
      System.out.println("message \""+originalMessage+"\" was not correctly processed by the server");
      cdl.countDown();
    }
  
    @Override
    public void onProcessed(String originalMessage, String response) {
      System.out.println("message \""+originalMessage+"\" sent with response: " + response);
      cdl.countDown();
    }
  }
  
  
  
  
}
