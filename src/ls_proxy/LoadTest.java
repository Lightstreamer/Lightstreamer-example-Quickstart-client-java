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

import java.util.ArrayList;
import java.util.Random;

/**
 * Demonstrates advanced Server access support, through LSProxy facade.
 *
 * Shows impacts of connections/disconnections on pushing activity
 * and the impact of transactions on subscription/unsubscription
 * management. Also, shows the impact of refused subscription requests
 * on Server and Client Library behaviour.
 * It requires that Lightstreamer Server is running with the FULLPORTFOLIODEMO Adapter Set
 * installed. Note, however, that the StockList demo Data Adapter does not
 * generate data suitable for COMMAND mode; so, error notifications will
 * appear on the Server for all COMMAND subscriptions performed.
 *
 * The test is invoked in this way:
 *    java ls_proxy.LoadTest <host> <port>
 * where <host> stands for the host name of Lightstreamer Server, while <port>
 * stands for the port number configured for the Server (the value in the
 * <port> subelement of the <http_server> block in the configuration file).
 */
public class LoadTest {

    private final LSProxy lsProxy;
    private final ConnectionInfo connInfo;
    private static final Random random = new Random();

    /**
     * Items supplied by StockList Demo Data Adapter
     */
    private static String[] items = {
        "item1", "item2", "item3",
        "item4", "item5", "item6",
        "item7", "item8", "item9",
        "item10", "item11", "item12",
        "item13", "item14", "item15"
    };

    /**
     * Fields supplied by StockList Demo Data Adapter
     */
    private static String[] fields = {
        "last_price", "time", "pct_change",
        "bid_quantity", "bid", "ask", "ask_quantity",
        "min", "max", "ref_price", "open_price"
    };

    /**
     * At random times, requests subscriptions and unsubscriptions with
     * different modes, performs connections and disconnections, clears all
     * activity and opens and closes transactions.
     * In some cases, subscription requests may be unacceptable
     * and disconnections may give rise to problematic conditions.
     * This is useful to test robustness features; in normal use, the
     * subscription requests should not be at random.
     *
     * @param args Should specify the host name of the Server and the port
     * number.
     */
    public static void main(String[] args) {
        final String pushServerHost = args[0];
        final int pushServerPort = Integer.parseInt(args[1]);

        LoadTest tester = new LoadTest(pushServerHost, pushServerPort);
        try {
			tester.lsProxy.startPushConnection(tester.connInfo);
		} catch (PushException e) {
            synchronized (LoadTest.class) {
                System.out.println("error " + e.getMessage() + " during connection");
            }
		}
        for (int i = 0; i < 10; i++) {
            tester.connectThread(45);
            tester.transactionThread(45);
            tester.subscrThread(30);
            tester.subscrThread(40);
            tester.subscrThread(50);
            tester.subscrThread(60);
            if (i == 5) {
                tester.clearThread(10);
            }
            pause(30);
        }
        tester.clearThread(10);
        pause(30);
        System.exit(0);
    }

    private LoadTest(final String pushServerHost, final int pushServerPort) {
        ProxyInfo info = new ProxyInfo();
        PushErrorListener errorListener = new ErrorListener();
        PushStatusListener statusListener = new StatusListener();
        lsProxy = new LSProxy(info, errorListener, statusListener);

        connInfo = new ConnectionInfo() {
            {
                this.pushServerUrl = "http://" + pushServerHost + ":" + pushServerPort;
                this.adapter = "FULLPORTFOLIODEMO";
            }
        };
    }

    private static void pause(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException e) {}
    }

    private static class ErrorListener implements PushErrorListener {

        public void onFailedConnection(PushException e) {
            synchronized (LoadTest.class) {
                System.out.println("error " + e.getMessage() + " from push server");
            }
        }

        public void onConnectionRetry(PushException e) {
            synchronized (LoadTest.class) {
                System.out.println("error " + e.getMessage() + " from push server");
            }
        }

        public void onFailedReconnection(PushException e) {
            synchronized (LoadTest.class) {
                System.out.println("error " + e.getMessage() + " during reconnection");
            }
        }

        public void onClosedConnection(int cause) {
            synchronized (LoadTest.class) {
                System.out.println("connection closed by push server");
            }
        }

		public void onReconnection() {
            synchronized (LoadTest.class) {
                System.out.println("reconnection successful");
            }
		}

		public void onReconnectionAbandoned() {
            synchronized (LoadTest.class) {
                System.out.println("reconnection abandoned");
            }
		}
    }

    private static class StatusListener implements PushStatusListener {

        public void onConnecting() {
            synchronized (LoadTest.class) {
                System.out.println("connecting...");
            }
        }

        public void onStreaming() {
            synchronized (LoadTest.class) {
                System.out.println("streaming...");
            }
        }

        public void onPolling() {
            synchronized (LoadTest.class) {
                System.out.println("polling...");
            }
        }

        public void onStalled() {
            synchronized (LoadTest.class) {
                System.out.println("stalled!");
            }
        }

        public void onDisconnected() {
            synchronized (LoadTest.class) {
                System.out.println("not connected");
            }
        }

    }

    private static class ItemListener implements UpdateListener {

        public void update(UpdateEvent event) {
            String itemName = event.getItem().getName();
            String mode = event.getItem().getMode();
            if (event.getItem().isUnfiltered()) {
                mode = "Unf. " +  mode;
            }

            synchronized (LoadTest.class) {
                System.out.print(itemName + " (" + mode + "): ");
                for (int i = 0; i < fields.length; i++) {
                    System.out.print(fields[i]);
                    if (event.getUpdatedValue(fields[i]) == null) {
                        System.out.print(" = ");
                    } else {
                        System.out.print(" --> ");
                    }
                    System.out.print(event.getValue(fields[i]) + ", ");
                }
                System.out.println();
            }
        }

        public void onSnapshotEnd(Item item) {
            String itemName = item.getName();
            String mode = item.getMode();
            if (item.isUnfiltered()) {
                mode = "Unf. " +  mode;
            }

            synchronized (LoadTest.class) {
                System.out.print(itemName + " (" + mode + "): ");
                System.out.println("end of snapshot");
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
            String itemName = item.getName();

            synchronized (LoadTest.class) {
                System.out.println("error " + e.getMessage() + " on " + itemName);
            }
        }
    }

    private void connectThread(int maxSecs) {
        final int cTime = random.nextInt(maxSecs);
        final int dTime = random.nextInt(maxSecs);
        new Thread() {
            public void run() {
                try {
                    pause(cTime);
                    synchronized (LoadTest.class) {
                        System.out.println("connecting");
                    }
					lsProxy.startPushConnection(connInfo);
                    synchronized (LoadTest.class) {
                        System.out.println("connected");
                    }
                    pause(dTime);
                    synchronized (LoadTest.class) {
                        System.out.println("disconnecting");
                    }
                    lsProxy.stopPushConnection();
				} catch (PushException e) {
                    synchronized (LoadTest.class) {
                        System.out.println("error " + e.getMessage() + " during connection");
                    }
				}
            }
        }.start();
    }

    private void clearThread(int maxSecs) {
        final int cTime = random.nextInt(maxSecs);
        new Thread() {
            public void run() {
                pause(cTime);
                synchronized (LoadTest.class) {
                    System.out.println("cleaning");
                }
                lsProxy.clearConnection();
                // pending unsubscriptions are going to get an error
            }
        }.start();
    }

    private void subscrThread(int maxSecs) {
        final int sTime = random.nextInt(maxSecs);
        final int uTime = random.nextInt(maxSecs);
        new Thread() {
            public void run() {
                UpdateListener localListener = new ItemListener();
                Item[] newItems = getItemsSubset(50);
                String[] newFields = getFieldsSubset(50);
                pause(sTime);
                synchronized (LoadTest.class) {
                    System.out.print("subscribing ");
                    printSubscr(newItems, newFields);
                }
                lsProxy.subscribeItems(localListener, newItems, newFields);
                pause(uTime);
                synchronized (LoadTest.class) {
                    System.out.print("unsubscribing ");
                    printSubscr(newItems, newFields);
                }
                lsProxy.unsubscribeItems(localListener, newItems, newFields);
            }
        }.start();
    }

    private void transactionThread(int maxSecs) {
        final int sTime = random.nextInt(maxSecs);
        final int eTime = random.nextInt(maxSecs);
        new Thread() {
            public void run() {
                pause(sTime);
                synchronized (LoadTest.class) {
                    System.out.println("opening transaction");
                }
                lsProxy.startTran();
                pause(eTime);
                synchronized (LoadTest.class) {
                    System.out.println("closing transaction");
                }
                lsProxy.endTran();
            }
        }.start();
    }

    private static String[] getSubset(String[] set, int prob) {
        ArrayList<String> chosen = new ArrayList<String>();
        for (int i = 0; i < set.length; i++) {
            int val = random.nextInt(100);
            if (prob > val) {
                chosen.add(set[i]);
            }
        }
        return chosen.toArray(new String[0]);
    }

    private static Item[] getItemsSubset(int prob) {
        String[] chosen = getSubset(items, prob);
        Item[] subset = new Item[chosen.length];

        for (int i = 0; i < chosen.length; i++) {
            try {
                int rawRnd = random.nextInt(10);
                int chosenNum = Integer.parseInt(chosen[i].substring(4));
                if (rawRnd == 0) {
                    subset[i] = new Item(chosen[i], "QUOTE_ADAPTER", Item.RAW);
                    subset[i].setUnique();
                } else if (chosenNum == 1) {
                    subset[i] = new Item(chosen[i], "QUOTE_ADAPTER", Item.COMMAND);
                    subset[i].requestUnfilteredDispatching();
                    subset[i].setUnique();
                } else if (chosenNum == 2) {
                    subset[i] = new Item(chosen[i], "QUOTE_ADAPTER", Item.DISTINCT);
                    subset[i].setUnique();
                } else if (chosenNum == 3) {
                    subset[i] = new Item(chosen[i], "QUOTE_ADAPTER", Item.COMMAND);
                    subset[i].setUnique();
                } else {
                    subset[i] = new Item(chosen[i], "QUOTE_ADAPTER", Item.MERGE);
                }
            } catch (NumberFormatException e) {
                // it can't happen
            } catch (ItemException e) {
                // it can't happen
            }
            try {
                int pauseRnd = random.nextInt(20);
                if (pauseRnd >= 5) {
                    subset[i].setRequestedMaxFrequency(1.0 / pauseRnd);
                }
            } catch (ItemException e) {
                // we don't care
            }
        }
        return subset;
    }

    private String[] getFieldsSubset(int prob) {
        String[] newFields = getSubset(fields, prob);
        String[] allFields = new String[newFields.length + 2];
        allFields[0] = "key";
        allFields[1] = "command";
            // needed by COMMAND subscriptions;
            // we add them to all subscriptions for simplicity.
        System.arraycopy(newFields, 0, allFields, 2, newFields.length);
        return allFields;
    }

    private static void printSubscr(Item[] subcsrItems, String[] subscrFields) {
        System.out.print("[ ");
        for (int i = 0; i < subscrFields.length; i++) {
            System.out.print(subscrFields[i]);
            System.out.print(" ");
        }
        System.out.println("] on");
        for (int i = 0; i < subcsrItems.length; i++) {
            Item item = subcsrItems[i];
            System.out.print("    ");
            System.out.print(item.getName());
            System.out.print(" in ");
            if (item.isUnfiltered()) {
                System.out.print("Unf. ");
            }
            System.out.print(item.getMode());
            System.out.print(" with ");
            System.out.println(item.getMaxFrequency());
        }
    }
}
