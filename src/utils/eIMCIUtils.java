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
package utils;
import com.sun.lwuit.Command;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

public class eIMCIUtils {
	public static void launchAlertDialog(String s) {
		TextArea myTextArea = new TextArea(s);
		myTextArea.setFocusable(false);
		myTextArea.setEditable(false);

		final Dialog myAlertDialog = new Dialog("Alert");
		myAlertDialog.addComponent(myTextArea);
		Command okCommand = new Command("Okay");
		myAlertDialog.addCommandListener(new ActionListener() {
	      public void actionPerformed(ActionEvent ae) {
	    	  myAlertDialog.dispose();				  
		  }
		});
		myAlertDialog.addCommand(okCommand);
		myAlertDialog.show();
		return;
	}
}