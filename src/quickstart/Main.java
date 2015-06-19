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

import java.util.concurrent.CountDownLatch;

public class Main {

  public static void main(String[] args) {
    
    switch (args[0]) {
      case "chat":
        new Chat().start(args[1]);
        break;
      case "stocklist": 
        new Stocklist().start(args[1]);
        slumber();
        break;
      case "portfolio":
        new Portfolio().start(args[1]);
        slumber();
        break;
    }
  }
  
  private static void slumber() {
    try {
      new CountDownLatch(1).await(); 
    } catch (InterruptedException e) {
    } 
  }

}
