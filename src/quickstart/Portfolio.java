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

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

/**
 * 
 */
public class Portfolio {
 
  /* 
  * @param args Should specify the address the Server
  */
 public static void main(String[] args) {
   final String serverAddress = args[0];
   
   new Portfolio().start(serverAddress);
   
   try {
     new CountDownLatch(1).await(); //just wait
   } catch (InterruptedException e) {
   } 
 }

  void start(String serverAddress) {
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
    
    Subscription sub = new Subscription("COMMAND", "portfolio1", new String[]{"key", "command", "qty"});
    sub.setRequestedSnapshot("yes");
    sub.setDataAdapter("PORTFOLIO_ADAPTER");
    sub.setCommandSecondLevelDataAdapter("QUOTE_ADAPTER");
    sub.setCommandSecondLevelFields(new String[]{"stock_name", "last_price"}); //the key values from the 1st level are used as item names for the second level
    
    SubscriptionListener subListener = new SystemOutSubscriptionListener();
    sub.addListener(subListener);
    
    client.subscribe(sub);
    client.connect();
    
    
  }
  
  private static class SystemOutSubscriptionListener implements SubscriptionListener {

    @Override
    public void onClearSnapshot(String itemName, int itemPos) {
      System.out.println("Server has cleared the current status of the portfolio");
    }
  
    @Override
    public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String key) {
      System.out.println(lostUpdates + " messages were lost ("+key+")");
    }
  
    @Override
    public void onCommandSecondLevelSubscriptionError(int code, String message, String key) {
      System.out.println("Cannot subscribe (2nd-level item "+key+") because of error " + code + ": " + message); 
    }
  
    @Override
    public void onEndOfSnapshot(String itemName, int itemPos) {
      System.out.println("Initial portfolio received");
    }
  
    @Override
    public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
      System.out.println(lostUpdates + " messages were lost");
    }
    
    @Override
    public void onItemUpdate(ItemUpdate update) {
      
      
      
      String command = update.getValue("command");
      if (command.equals("ADD")) {
        System.out.println("first update for this key ("+update.getValue("key")+"), the library is now automatically subscribing the second level item for it"); 
      } else if (command.equals("UPDATE")) {
        StringBuilder updateString = new StringBuilder("Update for ");
        updateString.append(update.getValue("stock_name")); //2nd level field
        updateString.append(", last price is ");
        updateString.append(update.getValue("last_price")); //2nd level field
        updateString.append(", we own ");
        updateString.append(update.getValue("qty")); //1st level field
        
        System.out.println(updateString);
        
        //there is the possibility that a second update for the first level is received before the first update for the second level
        //thus we might print a message that contains a few NULLs 
      } else if (command.equals("DELETE")) {
        System.out.println("key ("+update.getValue("key")+"), was removed, the library is now automatically unsubscribing the second level item for it");
      }
      
    }
  
    @Override
    public void onListenEnd(Subscription subscription) {
      System.out.println("Stop listeneing to subscription events");
    }
  
    @Override
    public void onListenStart(Subscription subscription) {
      System.out.println("Start listeneing to subscription events");
    }
  
    @Override
    public void onSubscription() {
      System.out.println("Now subscribed to the portfolio item");
    }
  
    @Override
    public void onSubscriptionError(int code, String message) {
      System.out.println("Cannot subscribe because of error " + code + ": " + message); 
    }
  
    @Override
    public void onUnsubscription() {
      System.out.println("Now unsubscribed from portfolio item");
    }
    
    public void onRealMaxFrequency(String frequency) {
        System.out.println("Frequency is " + frequency);
    }
    
  }
  
}
