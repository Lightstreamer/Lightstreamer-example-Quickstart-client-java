/*
 * Copyright 2014 Weswit Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ls_proxy;

import com.lightstreamer.ls_proxy.*;
import com.lightstreamer.ls_client.ConnectionInfo;

/**
 * Demonstrates advanced Server access support, through LSProxy facade.
 *
 * Shows subscriptions and unsubscriptions of interleaving sets of items
 * with interleaving sets of fields.
 * The test also demonstrates the recovery features offered, if Lightstreamer
 * Server is shut down and restarted during the test.
 * It requires that Lightstreamer Server is running with the DEMO
 * Adapter Set installed.
 * 
 * The test is invoked in this way:
 *    java ls_proxy.Test <host> <port>
 * where <host> stands for the host name of Lightstreamer Server, while <port>
 * stands for the port number configured for the Server (the value in the
 * <port> subelement of the <http_server> block in the configuration file).
 */
public class Test {

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
    private static final String[] schema = {
        "last_price", "time", "pct_change",
        "bid_quantity", "bid", "ask", "ask_quantity",
        "min", "max", "ref_price", "open_price"
    };

	/**
	 * Opens a connection to Lightstreamer Server and performs two requests of
	 * interleaving sets of items with interleaving sets of fields.
	 * The reception of particular values can trigger a further subscription or
	 * unsubscription request.
     * Then closes the connection after some time.
     * 
     * @param args Should specify the host name of the Server and the port
     * number.
	 * @throws Exception Thrown in case of any error.
	 */
    public static void main(String args[]) throws Exception {
        final String pushServerHost = args[0];
        final int pushServerPort = Integer.parseInt(args[1]);

        ProxyInfo info = new ProxyInfo() {
            {
                this.connectionRetryTimeoutMillis = 5000;
            }
        };
        PushErrorListener errorListener = new ErrorListener();
        PushStatusListener statusListener = new StatusListener();
        LSProxy lsProxy = new LSProxy(info, errorListener, statusListener);

        BWListener bwListener = new BWListener();
        lsProxy.subscribeBandwidth(bwListener);
        sleep(1);

        ConnectionInfo connInfo = new ConnectionInfo() {
            {
                this.pushServerUrl = "http://" + pushServerHost + ":" + pushServerPort;
                this.adapter = "DEMO";
            }
        };
        lsProxy.startPushConnection(connInfo);
        sleep(1);

        String[] schema1 = getSchemaSubset(new int[]{ 1, 3, 5, 7, 9 });
        String[] schema2 = getSchemaSubset(new int[]{ 1, 2, 3, 4, 5 });
        Item[] items1 = getItemsSubset(new int[] {
            1, 2, 3, 4, 5, 6
        });
        Item[] items2 = getItemsSubset(new int[] {
            1, 2, 3, 7, 8, 9
        });

        ItemListener itemsListener1 = new ItemListener(lsProxy);
        ItemListener itemsListener2 = new ItemListener(lsProxy);

        synchronized (Test.class) {
            System.out.println("first subscription");
        }
        lsProxy.subscribeItems(itemsListener1, items1, schema1);
        sleep(30);
        synchronized (Test.class) {
            System.out.println("second subscription");
        }
        lsProxy.subscribeItems(itemsListener2, items2, schema2);

        sleep(30);

        synchronized (Test.class) {
            System.out.println("first unsubscription");
        }
        lsProxy.unsubscribeItems(itemsListener1, items1, schema1);
        sleep(30);
        synchronized (Test.class) {
            System.out.println("second unsubscription");
        }
        lsProxy.unsubscribeItems(itemsListener2, items2, schema2);

        sleep(5);
        synchronized (Test.class) {
            System.out.println("cleaning");
        }
        itemsListener1.extraEnd();
        itemsListener2.extraEnd();
        lsProxy.unsubscribeBandwidth(bwListener);
        lsProxy.stopPushConnection();
        sleep(10);
        System.exit(0);
    }

    private static void sleep(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {}
    }

    private static class ErrorListener implements PushErrorListener {

		public void onFailedConnection(PushException e) {
            synchronized (Test.class) {
                System.out.println("error " + e.getMessage() + " from push server");
            }
		}

        public void onConnectionRetry(PushException e) {
            synchronized (Test.class) {
                System.out.println("error " + e.getMessage() + " from push server");
            }
        }

		public void onFailedReconnection(PushException e) {
            synchronized (Test.class) {
                System.out.println("error " + e.getMessage() + " from push server");
            }
		}

        public void onClosedConnection(int cause) {
            synchronized (LoadTest.class) {
                System.out.println("connection closed by push server");
            }
        }

        public void onReconnection() {
            synchronized (Test.class) {
                System.out.println("reconnection successful");
            }
        }

        public void onReconnectionAbandoned() {
            synchronized (Test.class) {
                System.out.println("reconnection abandoned");
            }
        }
    }

    private static class StatusListener implements PushStatusListener {

        public void onConnecting() {
            synchronized (Test.class) {
                System.out.println("connecting...");
            }
        }

        public void onStreaming() {
            synchronized (Test.class) {
                System.out.println("streaming...");
            }
        }

        public void onPolling() {
            synchronized (Test.class) {
                System.out.println("polling...");
            }
        }

        public void onStalled() {
            synchronized (Test.class) {
                System.out.println("stalled!");
            }
        }

        public void onDisconnected() {
            synchronized (Test.class) {
                System.out.println("not connected");
            }
        }

    }

    private static class BWListener implements BandwidthListener {

        public void bwUpdate(BandwidthEvent event) {
            synchronized (Test.class) {
                System.out.println("BANDWIDTH: " + event.getBandwidth() + " - ");
                System.out.println("CONNECTION: " + (event.isPushing() ? "ON" : "OFF"));
            }
        }
    }

    private static class ItemListener implements UpdateListener {
        private boolean on = false;
        private LSProxy refProxy;

        ItemListener(LSProxy lsProxy) {
            refProxy = lsProxy;
        }

        public void update(UpdateEvent event) {
            String itemName = event.getItem().getName();

            if (itemName.equals(items[1])) {
                // one of the most active Items
                synchronized (Test.class) {
                    System.out.print(itemName + ": ");
                    for (int i = 0; i < schema.length; i++) {
                        System.out.print(schema[i]);
                        if (event.getUpdatedValue(schema[i]) == null) {
                            System.out.print(" = ");
                        } else {
                            System.out.print(" --> ");
                        }
                        System.out.print(event.getValue(schema[i]) + ", ");
                    }
                    System.out.println();
                }
            } else {
                // reentrant subscription request of a different Item (items[1])
                String testVal = event.getUpdatedValue(schema[3]);

                if (testVal != null) {
                    int val = Integer.parseInt(testVal);

                    if (val > 60000) {
                        extraOn();
                    }
                    if (val < 20000) {
                        extraOff();
                    }
                }
            }
        }

        private synchronized void extraOn() {
            if ((! on) && refProxy != null) {
                System.out.println("Requesting a subscription for " + items[1]);
				try {
                    Item item = new Item(items[1], "QUOTE_ADAPTER", Item.MERGE);
                    refProxy.subscribeItem(this, item, schema);
				} catch (ItemException e) {
					e.printStackTrace();
				}

                on = true;
            }
        }

        private synchronized void extraOff() {
            if (on && refProxy != null) {
                System.out.println("Requesting an unsubscription for " + items[1]);
				try {
                    Item item = new Item(items[1], "QUOTE_ADAPTER", Item.MERGE);
                    refProxy.unsubscribeItem(this, item, schema);
				} catch (ItemException e) {
					e.printStackTrace();
				}

                on = false;
            }
        }

        public synchronized void extraEnd() {
            extraOff();
            refProxy = null;
        }

        public void onSnapshotEnd(Item item) {
            String itemName = item.getName();

            if (itemName.equals(items[1])) {
                // one of the most active Items
                synchronized (Test.class) {
                    System.out.print(itemName + ": ");
                    System.out.println("end of snapshot");
                }
            }
        }

        public void onLostUpdates(Item item, int lostUpdates) {
            if (lostUpdates < 0) {
                onException(item,
                        new Exception("lost or duplicated updates"));
            } else if (lostUpdates == 0) {
                onException(item,
                        new Exception("possible loss of updates"));
            } else {
                onException(item,
                        new Exception("lost " + lostUpdates + " updates"));
            }
        }

        public void onException(Item item, RequestException e) {
            onException(item, (Exception) e);
        }

        public void onException(Item item, PushException e) {
            onException(item, (Exception) e);
        }

        private void onException(Item item, Exception e) {
            synchronized (Test.class) {
                String itemName = item.getName();

                System.out.println("error " + e.getMessage() + " on " + itemName);
            }
        }
    }

    private static Item[] getItemsSubset(int[] pos) {
        Item[] subset = new Item[pos.length];

        for (int i = 0; i < pos.length; i++) {
            try {
				subset[i] = new Item(items[pos[i]], "QUOTE_ADAPTER", Item.MERGE);
			} catch (ItemException e) {
				e.printStackTrace();
			}
        }
        return subset;
    }

    private static String[] getSchemaSubset(int[] pos) {
        String[] subset = new String[pos.length];

        for (int i = 0; i < pos.length; i++) {
            subset[i] = schema[pos[i]];
        }
        return subset;
    }
}

