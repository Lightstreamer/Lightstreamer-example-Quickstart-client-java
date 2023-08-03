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

import java.util.Arrays;
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
      case "orderentry":
        PortfolioOrderEntry.main(Arrays.copyOfRange(args, 1, args.length));
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
