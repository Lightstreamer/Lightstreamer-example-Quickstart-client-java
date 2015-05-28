package quickstart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import com.lightstreamer.client.ClientListener;
import com.lightstreamer.client.ItemUpdate;
import com.lightstreamer.client.LightstreamerClient;
import com.lightstreamer.client.Subscription;
import com.lightstreamer.client.SubscriptionListener;

public class Stocklist {
  
  /**
   * Items supplied by StockList Demo Data Adapter
   */
  private static final String[] items = {
      "item1", "item2", "item3",
      "item4", "item5", "item6",
      "item7", "item8", "item9",
      "item10", "item11", "item12",
      "item13", "item14", "item15"
  };

  /**
   * Fields supplied by StockList Demo Data Adapter
   */
  private static final String[] fields = {
      "last_price", "time", "pct_change",
      "bid_quantity", "bid", "ask", "ask_quantity",
      "min", "max", "ref_price", "open_price"
  };

  public static void main(String[] args) {
    final String serverAddress = args[0];
    
    new Stocklist().start(serverAddress);
    
    try {
      new CountDownLatch(1).await(); //just wait
    } catch (InterruptedException e) {
    } 
  }

  private void start(String serverAddress) {
    LightstreamerClient client = new LightstreamerClient(serverAddress, "DEMO");
    
    ClientListener clientListener = new SystemOutClientListener();
    client.addListener(clientListener);
    
    Subscription sub = new Subscription("MERGE", items, fields);
    sub.setRequestedSnapshot("yes");
    sub.setDataAdapter("QUOTE_ADAPTER");
    
    SubscriptionListener subListener = new SystemOutSubscriptionListener();
    sub.addListener(subListener);
    
    client.subscribe(sub);
    client.connect();
  }
  
  private static class SystemOutSubscriptionListener implements SubscriptionListener {

    @Override
    public void onClearSnapshot(String itemName, int itemPos) {
      System.out.println("Server has cleared the current status of the chat");
    }
  
    @Override
    public void onCommandSecondLevelItemLostUpdates(int lostUpdates, String item) {
      //not on this subscription
    }
  
    @Override
    public void onCommandSecondLevelSubscriptionError(int arg0, String arg1, String arg2) {
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
    
    @Override
    public void onItemUpdate(ItemUpdate update) {
      
      System.out.println("====UPDATE====> " + update.getItemName());
      
      Iterator<Entry<String,String>> changedValues = update.getChangedFieldsIterator();
      while(changedValues.hasNext()) {
        Entry<String,String> field = changedValues.next();
        System.out.println("Field " + field.getKey() + " changed: " + field.getValue());
      }
      
      System.out.println("<====UPDATE====");
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

}
