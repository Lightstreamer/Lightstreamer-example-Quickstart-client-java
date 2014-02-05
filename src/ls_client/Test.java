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

package ls_client;

import java.util.ArrayList;

import com.lightstreamer.ls_client.ConnectionInfo;
import com.lightstreamer.ls_client.ExtendedConnectionListener;
import com.lightstreamer.ls_client.ExtendedTableInfo;
import com.lightstreamer.ls_client.HandyTableListener;
import com.lightstreamer.ls_client.LSClient;
import com.lightstreamer.ls_client.PushConnException;
import com.lightstreamer.ls_client.PushServerException;
import com.lightstreamer.ls_client.PushUserException;
import com.lightstreamer.ls_client.SimpleTableInfo;
import com.lightstreamer.ls_client.SubscrException;
import com.lightstreamer.ls_client.SubscribedTableKey;
import com.lightstreamer.ls_client.SubscriptionConstraints;
import com.lightstreamer.ls_client.UpdateInfo;

/**
 * Demonstrates basic Server access support, through LSClient facade.
 *
 * Opens a connection to Lightstreamer Server and subscribes a table,
 * defined with either a SimpleTableInfo or an ExtendedTableInfo.
 * It requires that Lightstreamer Server is running with the DEMO
 * Adapter Set installed.
 * 
 * The test can be invoked in five different ways:
 *    java ls_client.Test HOST PORT
 *       demonstrates use of SimpleTableInfo with a HandyTableListener
 *    java ls_client.Test HOST PORT extended
 *       demonstrates use of ExtendedTableInfo with a HandyTableListener
 *    java ls_client.Test HOST PORT multiple
 *       demonstrates use of ExtendedTableInfo with a HandyTableListener
 *       for simultaneous subscription of multiple items each in its own table
 *    java ls_client.Test HOST PORT command
 *       demonstrates use of SimpleTableInfo with a HandyTableListener
 *       for a subscription in COMMAND mode, to be handled in COMMAND logic
 *    java ls_client.Test HOST PORT command extended
 *       demonstrates use of ExtendedTableInfo with a HandyTableListener
 *       for a subscription in COMMAND mode, to be handled in COMMAND logic
 * HOST stands for the host name of Lightstreamer Server, while <port>
 * stands for the port number configured for the Server (the value in the
 * &lt;port&gt; subelement of the &lt;http_server&gt; block in the configuration file).
 * 
 * In order for the COMMAND based versions to produce updates, a PortfolioDemo
 * should be opened on the same Server and order entry operations should
 * be performed manually.
 */
public class Test {

    /**
     * Opens a connection, performs a table subscription and unsubscription
     * and closes the connection after some time.
     * 
	 * @param args Should specify the host name of the Server, the port number
     * and optionally the "extended", "multiple" and "command" flags.
	 * @throws Exception Thrown in case of any error.
	 */
	public static void main(String[] args) throws Exception {
        final String pushServerHost = args[0];
        final int pushServerPort = Integer.parseInt(args[1]);

        ArrayList<String> opts = new ArrayList<String>();
        for (int i = 2; i < args.length; i++) {
            opts.add(args[i]);
        }
        final boolean extended = opts.contains("extended");
        final boolean multiple = opts.contains("multiple");
        final boolean command = opts.contains("command");

        Thread.sleep(2000);

        final LSClient myClient = new LSClient();
        myClient.openConnection(
            new ConnectionInfo() {
                {
                    this.pushServerUrl = "http://" + pushServerHost + ":" + pushServerPort;
                    this.adapter = "DEMO";
                    // this.maxBandwidth = new Double(1.0);
                }
            },
            new ExtendedConnectionListener() {
                private long bytes = 0;
                public void onConnectionEstablished() {
                    System.out.println("connection established");
                }
                public void onSessionStarted(boolean isPolling) {
                    //never called
                }
                public void onSessionStarted(boolean isPolling, String controlLink) {
                    String clAddendum = controlLink != null ? " to server " + controlLink : "";
                    if (isPolling) {
                        System.out.println("Session started in smart polling"+clAddendum);
                    } else {
                        System.out.println("Session started in streaming"+clAddendum);
                    }
                }
                public void onNewBytes(long newBytes) {
                    this.bytes += newBytes;
                }
                public void onDataError(PushServerException e) {
                    System.out.println("data error");
                    e.printStackTrace();
                }
                public void onActivityWarning(boolean warningOn) {
                    if (warningOn) {
                        System.out.println("connection stalled");
                    } else {
                        System.out.println("connection no longer stalled");
                    }
                }
                public void onEnd(int cause) {
                    System.out.println("connection forcibly closed with cause code " + cause);
                }
                public void onClose() {
                    System.out.println("total bytes: " + bytes);
                }
                public void onFailure(PushServerException e) {
                    System.out.println("server failure");
                    e.printStackTrace();
                }
                public void onFailure(PushConnException e) {
                    System.out.println("connection failure");
                    e.printStackTrace();
                }
            }
        );
        Thread.sleep(5000);

        ArrayList<SubscribedTableKey> subscrRefs = new ArrayList<SubscribedTableKey>();

        if (! command) {
            if (extended) {
                SubscribedTableKey tableRef = playExtended(myClient);
                subscrRefs.add(tableRef);

            } else if (multiple) {
                SubscribedTableKey[] tableRefs = playMultiple(myClient);
                for (int i = 0; i < tableRefs.length; i++) {
                    subscrRefs.add(tableRefs[i]);
                }

            } else {
                SubscribedTableKey tableRef = playSimple(myClient);
                subscrRefs.add(tableRef);

            }
        } else {
            if (extended) {
                SubscribedTableKey tableRef = playCommandExtended(myClient);
                subscrRefs.add(tableRef);

            } else {
                SubscribedTableKey tableRef = playCommandSimple(myClient);
                subscrRefs.add(tableRef);

            }
        }

        SubscribedTableKey[] subscrKeys = subscrRefs.toArray(new SubscribedTableKey[0]);

        Thread.sleep(10000);
        myClient.changeSubscriptions(subscrKeys, new SubscriptionConstraints() {
            {
                maxFrequency = new Double(0.1);
            }
        });

        Thread.sleep(10000);
        myClient.unsubscribeTables(subscrKeys);

        Thread.sleep(5000);
        myClient.closeConnection();
        Thread.sleep(2000);
        System.exit(0);
    }

    private static SubscribedTableKey playExtended(final LSClient myClient)
            throws SubscrException, PushServerException, PushUserException,
            PushConnException {
        SubscribedTableKey tableRef = myClient.subscribeTable(
            new ExtendedTableInfo(
                new String[] { "item1", "item2", "item3" },
                "MERGE",
                new String[] { "last_price", "time", "pct_change" },
                true) {
                {
                    setDataAdapter("QUOTE_ADAPTER");
                }
            },
            new HandyTableListener() {
                private String notifyUpdate(UpdateInfo update) {
                    return update.isSnapshot() ? "snapshot" : "update";
                }
                private String notifyValue(UpdateInfo update, String fldName) {
                    String notify = " " + fldName + " = " + update.getNewValue(fldName);
                    if (update.isValueChanged(fldName)) {
                        notify += " (was " + update.getOldValue(fldName) + ")";
                    }
                    return notify;
                }
                public void onUpdate(int itemPos, String itemName, UpdateInfo update) {
                    System.out.println(notifyUpdate(update) +
                            " for " + itemName + ":" +
                            notifyValue(update, "last_price") +
                            notifyValue(update, "time") +
                            notifyValue(update, "pct_change"));
                }
                public void onSnapshotEnd(int itemPos, String itemName) {
                    System.out.println("end of snapshot for " + itemName);
                }
                public void onRawUpdatesLost(int itemPos, String itemName, int lostUpdates) {
                    System.out.println(lostUpdates + " updates lost for " + itemName);
                }
                public void onUnsubscr(int itemPos, String itemName) {
                    System.out.println("unsubscr " + itemName);
                }
                public void onUnsubscrAll() {
                    System.out.println("unsubscr table");
                }
            },
            false
        );
        return tableRef;
    }

    private static SubscribedTableKey[] playMultiple(final LSClient myClient)
            throws SubscrException, PushServerException, PushUserException,
            PushConnException {
        SubscribedTableKey[] tableRefs = myClient.subscribeItems(
            new ExtendedTableInfo(
                new String[] { "item1", "item2", "item3" },
                "MERGE",
                new String[] { "last_price", "time", "pct_change" },
                true) {
                {
                    setDataAdapter("QUOTE_ADAPTER");
                }
            },
            new HandyTableListener() {
                private String notifyUpdate(UpdateInfo update) {
                    return update.isSnapshot() ? "snapshot" : "update";
                }
                private String notifyValue(UpdateInfo update, String fldName) {
                    String notify = " " + fldName + " = " + update.getNewValue(fldName);
                    if (update.isValueChanged(fldName)) {
                        notify += " (was " + update.getOldValue(fldName) + ")";
                    }
                    return notify;
                }
                public void onUpdate(int itemPos, String itemName, UpdateInfo update) {
                    System.out.println(notifyUpdate(update) +
                            " for " + itemName + ":" +
                            notifyValue(update, "last_price") +
                            notifyValue(update, "time") +
                            notifyValue(update, "pct_change"));
                }
                public void onSnapshotEnd(int itemPos, String itemName) {
                    System.out.println("end of snapshot for " + itemName);
                }
                public void onRawUpdatesLost(int itemPos, String itemName, int lostUpdates) {
                    System.out.println(lostUpdates + " updates lost for " + itemName);
                }
                public void onUnsubscr(int itemPos, String itemName) {
                    System.out.println("unsubscr " + itemName);
                }
                public void onUnsubscrAll() {
                    System.out.println("onUnsubscrAll invoked ???");
                }
            }
        );
        return tableRefs;
    }

    private static SubscribedTableKey playSimple(final LSClient myClient)
            throws SubscrException, PushServerException, PushUserException,
            PushConnException {
        // Group and Schema names have to be manageable by
        // the LiteralBasedProvider used for the StockListDemo
        String groupName = "item1 item2 item3";
        String schemaName = "last_price time pct_change";
        SubscribedTableKey tableRef = myClient.subscribeTable(
            new SimpleTableInfo(groupName, "MERGE", schemaName, true) {
                {
                    setDataAdapter("QUOTE_ADAPTER");
                }
            },
            new HandyTableListener() {
                // will only receive positional information
                private String notifyUpdate(UpdateInfo update) {
                    return update.isSnapshot() ? "snapshot" : "update";
                }
                private String notifyValue(UpdateInfo update, int fldPos, String fldText) {
                    String notify = " " + fldText + " = " + update.getNewValue(fldPos);
                    if (update.isValueChanged(fldPos)) {
                        notify += " (was " + update.getOldValue(fldPos) + ")";
                    }
                    return notify;
                }
                public void onUpdate(int itemPos, String itemName, UpdateInfo update) {
                    System.out.println(notifyUpdate(update) +
                            " for " + itemPos + ":" +
                            notifyValue(update, 1, "last_price") +
                            notifyValue(update, 2, "time") +
                            notifyValue(update, 3, "pct_change"));
                }
                public void onSnapshotEnd(int itemPos, String itemName) {
                    System.out.println("end of snapshot for " + itemPos);
                }
                public void onRawUpdatesLost(int itemPos, String itemName, int lostUpdates) {
                    System.out.println(lostUpdates + " updates lost for " + itemPos);
                }
                public void onUnsubscr(int itemPos, String itemName) {
                    System.out.println("unsubscr " + itemPos);
                }
                public void onUnsubscrAll() {
                    System.out.println("unsubscr table");
                }
            },
            false
        );
        return tableRef;
    }

    private static SubscribedTableKey playCommandExtended(
            final LSClient myClient) throws SubscrException,
            PushServerException, PushUserException, PushConnException {
        SubscribedTableKey tableRef = myClient.subscribeTable(
            new ExtendedTableInfo(
                new String[] { "portfolio1" },
                "COMMAND",
                new String[] { "key", "command", "qty" },
                true) {
                {
                    setDataAdapter("PORTFOLIO_ADAPTER");
                }
            },
            new HandyTableListener() {
                private String notifyUpdate(UpdateInfo update) {
                    return update.isSnapshot() ? "snapshot" : "update";
                }
                private String notifyValue(UpdateInfo update, String fldName) {
                    String notify = " " + fldName + " = " + update.getNewValue(fldName);
                    if (update.isValueChanged(fldName)) {
                        notify += " (was " + update.getOldValue(fldName) + ")";
                    }
                    return notify;
                }
                public void onUpdate(int itemPos, String itemName, UpdateInfo update) {
                    System.out.println(notifyUpdate(update) +
                            " for " + itemName + ":" +
                            " key = " + update.getNewValue("key") +
                            " command = " + update.getNewValue("command") +
                            notifyValue(update, "qty"));
                }
                public void onSnapshotEnd(int itemPos, String itemName) {
                    System.out.println("end of snapshot for " + itemName);
                }
                public void onRawUpdatesLost(int itemPos, String itemName, int lostUpdates) {
                    System.out.println(lostUpdates + " updates lost for " + itemName);
                }
                public void onUnsubscr(int itemPos, String itemName) {
                    System.out.println("unsubscr " + itemName);
                }
                public void onUnsubscrAll() {
                    System.out.println("unsubscr table");
                }
            },
            true
        );
        return tableRef;
    }

    private static SubscribedTableKey playCommandSimple(final LSClient myClient)
            throws SubscrException, PushServerException, PushUserException,
            PushConnException {
        // Group and Schema names have to be manageable by
        // the LiteralBasedProvider used for the StockListDemo
        String groupName = "portfolio1";
        String schemaName = "key command qty";
        SubscribedTableKey tableRef = myClient.subscribeTable(
            new SimpleTableInfo(groupName, "COMMAND", schemaName, true) {
                {
                    setDataAdapter("PORTFOLIO_ADAPTER");
                }
            },
            new HandyTableListener() {
                private String notifyUpdate(UpdateInfo update) {
                    return update.isSnapshot() ? "snapshot" : "update";
                }
                private String notifyValue(UpdateInfo update, int fldPos, String fldText) {
                    String notify = " " + fldText + " = " + update.getNewValue(fldPos);
                    if (update.isValueChanged(fldPos)) {
                        notify += " (was " + update.getOldValue(fldPos) + ")";
                    }
                    return notify;
                }
                public void onUpdate(int itemPos, String itemName, UpdateInfo update) {
                    System.out.println(notifyUpdate(update) +
                            " for " + itemPos + ":" +
                            " key = " + update.getNewValue(1) +
                            " command = " + update.getNewValue(2) +
                            notifyValue(update, 3, "qty"));
                }
                public void onSnapshotEnd(int itemPos, String itemName) {
                    System.out.println("end of snapshot for " + itemPos);
                }
                public void onRawUpdatesLost(int itemPos, String itemName, int lostUpdates) {
                    System.out.println(lostUpdates + " updates lost for " + itemPos);
                }
                public void onUnsubscr(int itemPos, String itemName) {
                    System.out.println("unsubscr " + itemPos);
                }
                public void onUnsubscrAll() {
                    System.out.println("unsubscr table");
                }
            },
            true
        );
        return tableRef;
    }

}
