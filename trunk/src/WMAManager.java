/*
 * Copyright 2011 Michael Syson
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
import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;


public class WMAManager {
    public WMAManager() {	  
    }
    
    /**
     *  newMessageConnection returns a new MessageConnection
     *  @param addr is the address (local or remote)
     *  @return MessageConnection that was created (client 
     *  or server)
     *  @throws Exception if an error is encountered
     *  
     *  Reference: http://developers.sun.com/mobility/midp/articles/wma/
     *  Date: May 20, 2010
     */
    public MessageConnection newMessageConnection(String addr)
      throws Exception {
        return((MessageConnection)Connector.open(addr));
    }

    /**
     *  sendTextMessage sends a TextMessage on the specified 
     *  connection
     *  @param mc the MessageConnection
     *  @param msg the message to send
     *  @param url the destination address, typically used in 
     *  server mode
     *  
     *  Reference: http://developers.sun.com/mobility/midp/articles/wma/
     *  Date: May 20, 2010
     */
    public void sendTextMessage(MessageConnection mc, String 
      msg, String url) throws Exception {
//        try {
            TextMessage tmsg =
                (TextMessage)mc.newMessage
                  (MessageConnection.TEXT_MESSAGE);
            if (url!= null)
                tmsg.setAddress(url);
            tmsg.setPayloadText(msg);
            int segcount = mc.numberOfSegments(tmsg);
            if (segcount == 0) {
//                alertUser(SEGMENTATIONERROR); // can't send, 
//                				  // alert the user
            	System.out.println("error: Segment count is 0");
            }
            else {
                mc.send(tmsg);
            }
//        }
//        catch(Exception e) {
//            //  Handle the exception...
//            System.out.println("sendTextMessage " + e);
//        }
    }    
}
