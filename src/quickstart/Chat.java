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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.ClientMessageListener;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

public class Chat {

  /**
   * Simple chat that reads/prints chat messages from/to the command line.
   * 
   * It shows how to connect/disconnect, subscribe/unsubscribe and 
   * send messages to Lightstreamer.
   * It requires that Lightstreamer Server is running with the CHAT
   * Adapter Set installed.
   * 
   * The test is invoked in this way:
   *    java quickstart.Chat <serverAddress>
   * where <serverAddress> stands for the full address of Lightstreamer Server
   * (e.g.: https://push.lightstreamer.com)
   * 
   * 
   * Some special commands can be issues to see how the client behaves:
   * STOP - disconnects the client
   * START - reconnects the client
   * UNSUB - unsubscribes from the chat
   * SUB - subscribes to the chat 
   * EXIT - exits the application
   * (the application starts by connecting and subscribing)
   * 
   * 
   * @param args Should specify the address the Server
   */
  public static void main(String[] args) {
    final String serverAddress = args[0];
    
    new Chat().start(serverAddress);
  }
  
  void start(String serverAddress) {
    
    // the chat demo available @ http://demos.lightstreamer.com/ChatDemo/
    // connects to the DEMO adapter set on push.lightstreamer.com rather than 
    // to a CHAT one (obviously that DEMO adapter set contains the CHAT_ROOM
    // adapters). 
    // For this reason, when connecting to push.lightstreamer.com we use the DEMO
    // adapter set, so you can open a browser to the above address and chat
    // with yourself.
    
    String adapterSet = serverAddress.contains("push.lightstreamer.com") ? "DEMO" : "CHAT";
    LightstreamerClient client = new LightstreamerClient(serverAddress, adapterSet);
    
    ClientListener clientListener = new SystemOutClientListener();
    client.addListener(clientListener);
    
    Subscription sub = new Subscription("DISTINCT","chat_room",new String[] {"raw_timestamp","message", "IP"});
    sub.setRequestedSnapshot("yes");
    sub.setDataAdapter("CHAT_ROOM");
    
    SubscriptionListener subListener = new SystemOutSubscriptionListener();
    sub.addListener(subListener);
    
    client.subscribe(sub);
    client.connect();
    
    ClientMessageListener sentMessageListener = new SystemOutClientMessageListener();
    
    String PREFIX = "CHAT|";
    System.out.println("========NOW READING FROM SYSTEM IN=========");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    boolean queueWhileDisconnected = true;
    while(true) {
      String mex = null;
      try {
         mex = br.readLine();
      } catch (IOException e) {
        //something wrong with the console?
        return;
      }
      if (!mex.trim().isEmpty()) {
        try {
          //none of these calls blocks
          switch (mex) {
          
            // connect/disconnect commands
            case "STOP":
              client.disconnect();
              break;
            case "START":
              client.connect();
              break;
              
            // subscribe / unsubscribe commands  
            case "UNSUB":
              client.unsubscribe(sub);
              break;
            case "SUB":
              client.subscribe(sub);
              break;
              
            // specifiying true as last parameter, if a message is sent while not connected
            // to Lightstreamer, the client will queue it and will send as soon as
            // a new connection is available. Use these commands to switch the flag for the 
            // next messages
            case "NOQUEUE":
              queueWhileDisconnected = false;
              System.out.println("=== Not queuing messages if disconnected");
              break;
            case "QUEUE":
              System.out.println("=== Queuing messages if disconnected");
              queueWhileDisconnected = true;
              break;
              
            // exiting this loop there will be no more non-daemon threads and the application
            // will terminate  
            case "EXIT":
              return;
              
              
            default:
              String s = PREFIX + mex;
              client.sendMessage(s, "chat", 500, sentMessageListener, queueWhileDisconnected);
              
          }
        } catch(IllegalArgumentException | IllegalStateException e) {
          //some of the above calls might throw exceptions, see API docs for details
          System.err.println(e.getMessage());
          continue;
        }
      }
     
    }
    
    
  }
  
  
  
  
  public static class SystemOutSubscriptionListener implements SubscriptionListener {
    
    @Override
    public void onClearSnapshot(String itemName, int itemPos) {
      System.out.println("Server has cleared the current status of the chat");
    }
  
    @Override
    public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String key) {
      //not on this subscription
    }
  
    @Override
    public void onCommandSecondLevelSubscriptionError(int code, String message, String key) {
      //not on this subscription
    }
  
    @Override
    public void onEndOfSnapshot(String arg0, int arg1) {
      System.out.println("Snapshot is now fully received, from now on only real-time messages will be received");
    }
  
    @Override
    public void onItemLostUpdates(String itemName, int itemPos, int lostUpdates) {
      System.out.println(lostUpdates + " messages were lost");
    }
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("E hh:mm:ss"); 
  
    @Override
    public void onItemUpdate(ItemUpdate update) {
      long timestamp = Long.parseLong(update.getValue("raw_timestamp"));
      Date time = new Date(timestamp);
      System.out.println("MESSAGE @ " + dateFormatter.format(time) + " |" + update.getValue("IP") + ": " + update.getValue("message"));
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
      System.out.println("Now subscribed to the chat item, messages will now start coming in");
    }
  
    @Override
    public void onSubscriptionError(int code, String message) {
      System.out.println("Cannot subscribe because of error " + code + ": " + message); 
    }
  
    @Override
    public void onUnsubscription() {
      System.out.println("Now unsubscribed from chat item, no more messages will be received");
    }
    
  }
  
  public static class SystemOutClientMessageListener implements ClientMessageListener {
  
    @Override
    public void onAbort(String originalMessage, boolean sentOnNetwork) {
      if (sentOnNetwork) {
        System.out.println("message \""+originalMessage+"\" was aborted; is not known if it reached the server");
      } else {
        System.out.println("message \""+originalMessage+"\" was aborted and will not be sent to the server");
      }
      
    }
  
    @Override
    public void onDeny(String originalMessage, int code, String message) {
      System.out.println("message \""+originalMessage+"\" was denied by the server because of error " + code + ": " + message);
    }
  
    @Override
    public void onDiscarded(String originalMessage) {
      System.out.println("message \""+originalMessage+"\" was discarded by the server because it was too late when it was received");
    }
  
    @Override
    public void onError(String originalMessage) {
      System.out.println("message \""+originalMessage+"\" was not correctly processed by the server");
    }
  
    @Override
    public void onProcessed(String originalMessage) {
      System.out.println("message \""+originalMessage+"\" processed correctly");
    }
  }
  
  
  
  
}
