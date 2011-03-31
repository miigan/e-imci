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
import javax.microedition.midlet.MIDlet;

import com.sun.lwuit.Display;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;

public class eIMCIMidlet extends MIDlet {
	MyForm myForm;

	public eIMCIMidlet() {
	}
	
	public void startApp() {
		try {
			//init the LWUIT Display
			Display.init(this);
			Resources r = Resources.open("/eIMCIresources.res");
			UIManager.getInstance().setThemeProps(r.getTheme(
				r.getThemeResourceNames()[0])
			);
			myForm = new MyForm(this);
			myForm.show();
			
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}		
	}
	public void pauseApp() {
	}
	public void destroyApp(boolean unconditional) {
		//be sure to close DB first before notifying myMIDLET to close
		myForm.getViewPendingListDB().close();			
	}
}