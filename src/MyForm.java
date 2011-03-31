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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.wireless.messaging.MessageConnection;

import org.garret.perst.Storage;
import org.garret.perst.StorageFactory;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import perst.PerstPatientData;
import perst.PerstRoot;

import com.Ostermiller.util.StringTokenizer;
import com.sun.lwuit.Button;
import com.sun.lwuit.ButtonGroup;
import com.sun.lwuit.CheckBox;
import com.sun.lwuit.ComboBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Font;
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.RadioButton;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;
import com.sun.lwuit.plaf.Border;

import utils.eIMCIUtils;

public class MyForm extends Form implements ActionListener {
	//FOR DEBUGGING
	private boolean isInDebugMode=true;
	
	private final int SCREEN_W;
//	private final int SCREEN_H; 
	
	private Command selectCmd;
	private Command nextCmd;
	private Command backCmd;
	private Command exitCmd;
	private Command signInCmd;
	private Command returnToTitleCmd;
	private Command resendSMSCmd;
	private Command deletePendingDataCmd;
	
	private Button performIMCIButton;
	private Button viewPendingButton;
	private Button aboutButton;

	private final int SIGNIN_FORM=0;
	private final int ABOUT_FORM=1;	
	private final int VIEW_PENDING_FORM=2;		
	private final int TITLE_FORM=3;	
	private final int STANDARD_PATIENT_FORM=4;
	private final int EIMCI_FORM=5;
	private final int MAX_FORMS=5;
	private final int DOSAGE_FORM=6;	
	private int currForm=SIGNIN_FORM;
	
	private String titleString="";
	private String currIMCIQuestion="";
	private String nextIMCIQuestionIfYes="";
	private String nextIMCIQuestionIfNo="";
	
	private final String MALE_STRING = "Male";
	private final String FEMALE_STRING = "Female";
	
	private RadioButton yesRadioButton;
	private RadioButton noRadioButton;
	
	private boolean isUsingACheckbox;
	private Vector checkBoxContainer;
	private int totalCheckedBoxes;
	private int requiredTotalCheckedBoxes;
	
	private Vector checkedCheckBoxContainer;
	
	private boolean isAClassificationTaskNode;
	private Vector classificationContainer;
	
	private boolean isUsingATextField;
	private boolean isUsingASpecial;	
	
	private boolean hasReachedEndOfAllDecisionTrees;
	private boolean isFirstQuestionForDecisionTree;
		
	private String[] IMCICaseList;
	private final int MAX_IMCI_CASE_LIST = 5;
	private int currIMCICaseList=0;
	
	private final int GENERAL_DANGER_SIGNS = 0;
	private final int COUGH =1;
	private final int DIARRHEA = 2;
	private final int FEVER = 3;
	private final int EAR_PROBLEM = 4;
	
	private Vector imciQuestionContainer;
	private int imciQuestionContainerCounter=-1;//because I do a ++, so that the first element would be at 0
	private boolean usedBackCmd;
	
	private Vector imciAnswerContainer;
	
	private Vector imciClassificationAndManagementSummaryContainer;
	private int currImciClassificationAndManagementSummary=-1;
	
	private TextArea commentTextArea;
	private TextArea usernameTextArea;
	private TextArea passwordTextArea;
	
	private WMAManager myWMAManager;
	
	private boolean hasErrorSigningInLabel;

	private int currNumOfSigninTries=0;
	private final int MAX_SIGNIN_TRIES=3-1; //-1 because the third try should launch the alert dialog box already 
	
	private boolean isAGeneralDangerSignsCheckBox;
	private boolean hasAskedAboutGeneralDangerSigns;
	private Vector checkBoxContainerGeneralDangerSigns;
	private int totalCheckedBoxesGeneralDangerSigns=0;
		
	private boolean isSevereDehydrationCheckBox;
	private boolean isSomeDehydrationCheckBox;
	private Vector checkBoxContainerSevereDehydration;
	
	private final String GENERAL_DANGER_SIGNS_CHECKBOX_QUESTION = "Has any of the general danger signs?";
	private final String SOME_DEHYDRATION_CHECKBOX_QUESTION = "Check for SOME DEHYDRATION. Has at least two (2) of the following signs:";
	private final String SEVERE_DEHYDRATION_CHECKBOX_QUESTION = "Check for SEVERE DEHYDRATION. Has at least two (2) of the following signs:";

	private String aboutString="";
	private String aboutFilename="about.txt";

	private String[] myCalendarMonths;
	
	private String dateMonth;
	private String dateDay;
	private String dateYear;
	
	private Label date;
	private String myLocation;
	//TODO: change below to combobox for eIMCI app settings
//	private TextArea locationNameTextArea; //locationIDTextArea;
	private TextArea childsFamilyNameTextArea;
	private TextArea childsGivenNameTextArea;
	private TextArea childsMiddleNameTextArea;
	private ComboBox mySexComboBox;
	private ComboBox myBirthdayMonthComboBox;
	private TextArea myBirthdayDayTextArea;
	private TextArea myBirthdayYearTextArea;	
//	private TextArea myAgeTextArea; //changed this to Birthday
	private TextArea myWeightTextArea;
	private TextArea myTempTextArea;
	private TextArea addressTextArea;
	private TextArea mothersNameTextArea;
	private TextArea childProblemTextArea;
	private RadioButton initialVisitRadioButton;
	private RadioButton followupVisitRadioButton;
	private TextArea overallSummaryTextArea;
	
	private TextArea myNumericTextArea;
	
	private boolean hasProcessedSendSMS;
		
	private Storage viewPendingListDB;
	private final int dbPagePoolSize = 64*1024; //min is supposed to be 40K
	private final String dbName = "eIMCI_DB.dbs";
	private PerstPatientData myPerstPatientData;
	private StringBuffer mySMSMessage;

	private MIDlet myMIDlet;

	public MyForm(MIDlet myMIDlet) {
		this.myMIDlet = myMIDlet;		

		SCREEN_W = this.getWidth();
//		SCREEN_H = this.getHeight();
		
		IMCICaseList = new String[MAX_IMCI_CASE_LIST];
		IMCICaseList[GENERAL_DANGER_SIGNS] = "generalDangerSigns.xml";
		IMCICaseList[COUGH] = "cough.xml";
		IMCICaseList[DIARRHEA] = "diarrhea.xml";
		IMCICaseList[FEVER] = "fever.xml";
		IMCICaseList[EAR_PROBLEM] = "earProblem.xml";
				
		checkBoxContainer = new Vector(5,1); //initial capacity: 5, increment by: 1
		checkedCheckBoxContainer = new Vector(5,1);
		
		classificationContainer = new Vector(5,1);
		checkBoxContainerGeneralDangerSigns = new Vector(5,1);
		checkBoxContainerSevereDehydration = new Vector(5,1);
		
		imciQuestionContainer = new Vector(5,1);
		imciQuestionContainer.addElement(""+currIMCICaseList);
		imciQuestionContainerCounter++;

		imciAnswerContainer = new Vector(5,1);
		
		imciClassificationAndManagementSummaryContainer = new Vector(5,2);
		
		isUsingATextField=true;
		usedBackCmd=false;
		
		hasProcessedSendSMS=false;
		
		myWMAManager = new WMAManager();
		
		//add 20 spaces in titleString because it tickers so quickly that the first characters of the string don't get shown right away
		titleString = "                   eIMCI: Management of the sick child age 2 months up to 5 years";
		
		//Reference: Jeffrey Jongko and his readJARResource() method from his framework
    	//READ A FILE
    	try {
            if (aboutFilename.charAt(0)!='/')
            {
            	aboutFilename = "/"+aboutFilename;
            }
    		
    		byte[] b = new byte[100];    		
        	InputStream is = aboutFilename.getClass().getResourceAsStream(aboutFilename);
        	
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	int count = 0; 
        	while((count = is.read(b))!=-1)
        	{
        		//append byte[] to baos. This will allow us to have text file that is more than 100 bytes
        		baos.write(b, 0, count);
        	}        	
        	is.close();    		
        	
        	aboutString = new String(baos.toByteArray());		        	
    	}
    	catch(Exception e) {
    		System.out.println("ERROR in reading FILE.");
    		e.printStackTrace();
    	}

    	myCalendarMonths = new String[]{"Jan","Feb","March","April","May","June","July","Aug","Sept","Oct","Nov","Dec"};
		
    	viewPendingListDB = StorageFactory.getInstance().createStorage();
    	viewPendingListDB.open(dbName,dbPagePoolSize);//Storage.INFINITE_PAGE_POOL);

    	//Reference: PerstProScoutDemo (UniAr.java)-- can be downloaded from the link below
    	//http://www.mcobject.com/index.cfm?fuseaction=download&pageid=498&sectionid=133 ; last accessed on March 30, 2011
    	// There is one root object in the database. 
        PerstRoot myPerstRoot = (PerstRoot)viewPendingListDB.getRoot();
    	if (myPerstRoot == null) { // if root object was not specified, then storage is not yet initialized
            // Perform initialization:
            // ... create root object
        	myPerstRoot = new PerstRoot(viewPendingListDB);
            // ... register new root object
        	viewPendingListDB.setRoot(myPerstRoot); 
            // ... and import data from resource files
        	//importData();
        }
    	
		selectCmd = new Command("Select");
		nextCmd = new Command("Next");
		backCmd = new Command("Back");
		exitCmd = new Command("Exit");
		signInCmd = new Command("Sign in");		
		returnToTitleCmd = new Command("Return to Title");		
		resendSMSCmd = new Command("Resend");
		deletePendingDataCmd = new Command("Delete");

		this.addCommandListener(this);		
		
		setFocusable(true);
		initLoginForm();
	}
	
	public Storage getViewPendingListDB() {
		return viewPendingListDB;
	}
	
	public void initLoginForm() {
		BoxLayout defaultLayout = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(defaultLayout);	

		this.removeAll();
		this.removeAllCommands();
		System.gc();
		
		this.setTitle("Welcome to eIMCI");		

		this.addComponent(new Label("Sign in with your account."));

		if (hasErrorSigningInLabel) {
			Label l = new Label("                   Invalid username or password.");
			l.getStyle().setBgColor(0xFF0000); //color the font red
			l.getStyle().setFgColor(0xFFFFFF); //color the font red
			Font f = Font.createSystemFont(Font.FACE_PROPORTIONAL,
	                                       Font.STYLE_BOLD,Font.SIZE_MEDIUM);
			l.getStyle().setFont(f);
			l.startTicker(0, true);
			this.addComponent(l);			
		}
		//add components horizontally
		BoxLayout boxLayout_XAxis = new BoxLayout(BoxLayout.X_AXIS);				
		//username
		Container myContainerUsername = new Container();
		myContainerUsername.setLayout(boxLayout_XAxis);
		myContainerUsername.addComponent(new Label("Username: "));
		
		usernameTextArea = new TextArea();
		usernameTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
		myContainerUsername.addComponent(usernameTextArea);		
		this.addComponent(myContainerUsername);
		this.setFocused(usernameTextArea);
		
		//password
		Container myContainerPassword = new Container();
		myContainerPassword.setLayout(boxLayout_XAxis);
		myContainerPassword.addComponent(new Label("Password: "));
		
		passwordTextArea = new TextArea();
		passwordTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
//		passwordTextArea.setConstraint(TextArea.ANY);
		passwordTextArea.setConstraint(TextArea.PASSWORD);
		myContainerPassword.addComponent(passwordTextArea);		
		this.addComponent(myContainerPassword);
		
		this.addCommand(signInCmd);//do this so that exitCmd is on the right-hand side		
		this.addCommand(exitCmd);		
	}
	
	public void initTitleForm() {		
		//Set a non-TableLayout Layout Manager before calling removeAll().
		//This will ensure that if previous form used TableLayout as the 
		//Layout Manager, removeAll() will not complain of a NullPointerException.
		BoxLayout defaultLayout = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(defaultLayout);	

		this.removeAll();
		this.removeAllCommands();
		System.gc();
		
		this.setTitle("");		

		//add components vertically
		BoxLayout boxLayout_YAxis = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(boxLayout_YAxis);	

		try {
		  Image icon = Image.createImage("/title.png");
		  Label imageLabel = new Label(icon);
	      this.addComponent(imageLabel);		  
		}
		catch(Exception e) {
			e.printStackTrace();
		}
				
		performIMCIButton = new Button("Perform IMCI");
		performIMCIButton.setAlignment(CENTER);
		performIMCIButton.addActionListener(this);
		
		viewPendingButton = new Button("View Pending");
		viewPendingButton.setAlignment(CENTER);
		viewPendingButton.addActionListener(this);
		
		aboutButton = new Button("About");
		aboutButton.setAlignment(CENTER);
		aboutButton.addActionListener(this);

		this.addComponent(performIMCIButton);
		this.addComponent(viewPendingButton);
		this.addComponent(aboutButton);

		this.addCommand(selectCmd);//do this so that exitCmd is on the right-hand side		
		this.addCommand(exitCmd);		
		
		this.setFocused(performIMCIButton);
	}

	public void initViewPendingForm() {
		BoxLayout defaultLayout = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(defaultLayout);	

		this.removeAll();
		this.removeAllCommands();
		System.gc();
		
		this.setTitle("View Pending Patient Forms");	
		
		//add components vertically
		BoxLayout boxLayout_YAxis = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(boxLayout_YAxis);	

		//add components horizontally
		BoxLayout boxLayout_XAxis = new BoxLayout(BoxLayout.X_AXIS);		

        PerstRoot myPerstRoot = (PerstRoot)viewPendingListDB.getRoot();
        int size = myPerstRoot.myPerstListPatientData.size();
        if (size==0) {
			TextArea myTextArea = new TextArea("No pending messages right now. Press 'Okay' to return to the title screen.");
			myTextArea.setFocusable(false);
			myTextArea.setEditable(false);

			final Dialog myDialog = new Dialog("Alert");
			myDialog.addComponent(myTextArea);
			Command okCommand = new Command("Okay");
			myDialog.addCommandListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
				  myDialog.dispose();				  
				  //Once IMCI process has been completed, go back to the title screen
				  currForm=TITLE_FORM;
				  processExitIMCIForm();
				  processFormChange();
			  }
			});
			myDialog.addCommand(okCommand);
			myDialog.show();
        }
        else {
	        for(int i=0; i<size; i++) {
	    		Container myContainerPendingPatientData = new Container();
	    		myContainerPendingPatientData.setLayout(boxLayout_XAxis);
				Label l = new Label(((PerstPatientData)myPerstRoot.myPerstListPatientData.get(i)).getPatientFullName() + " | " +
						  ((PerstPatientData)myPerstRoot.myPerstListPatientData.get(i)).getDateCreated().toString()
						 );
				l.setFocusable(true);	

/* //I keep getting an error at createSMSMessageFromSelectedPerstPatientData() 
   //saying that there is no label in focus whenever I pre-set the first label 
   //to be focused, so I'll comment out the codes below.
				if (i==0) { //if this is the first pending sms entry
					  l.setFocus(true);
				}
*/				
				
				l.getStyle().setBorder(Border.createLineBorder(1)); 
	    		myContainerPendingPatientData.addComponent(l);
	    		this.addComponent(myContainerPendingPatientData);        	
	        }
			this.addCommand(resendSMSCmd);		
			this.addCommand(deletePendingDataCmd);		
	        this.addCommand(backCmd);		
        }
	}
	
	public void initAboutForm() {
		BoxLayout defaultLayout = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(defaultLayout);	

		this.removeAll();
		this.removeAllCommands();
		System.gc();
		
		this.setTitle("About eIMCI");	
		TextArea t = new TextArea(aboutString);
		t.setEditable(false);
		t.setFocusable(false);
		this.addComponent(t);
		
		this.addCommand(backCmd);		
	}
	public void initStandardPatientForm() {
		//Set a non-TableLayout Layout Manager before calling removeAll().
		//This will ensure that if previous form used TableLayout as the 
		//Layout Manager, removeAll() will not complain of a NullPointerException.
		BoxLayout defaultLayout = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(defaultLayout);	

		this.removeAll();
		this.removeAllCommands();
		System.gc();
		
		//add 20 spaces in titleString because it tickers so quickly that the first characters of the string don't get shown right away
		this.setTitle(titleString);		

		//add components vertically
		BoxLayout boxLayout_YAxis = new BoxLayout(BoxLayout.Y_AXIS);
		//this code below doesn't seem to be really necessary, because it's 
		//the default. However, for formality's sake and clarity, it has been added here
		this.setLayout(boxLayout_YAxis); 

		//add components horizontally
		BoxLayout boxLayout_XAxis = new BoxLayout(BoxLayout.X_AXIS);		
		Container myContainerDate = new Container();
		myContainerDate.setLayout(boxLayout_XAxis);
		
		myContainerDate.addComponent(new Label("Date:"));
		Date d = new Date();
		StringTokenizer st = new StringTokenizer(d.toString(), " ");
		st.nextToken();
		dateMonth = st.nextToken();
		dateDay = st.nextToken();
		dateYear = st.nextToken();
		for(int i=0; i<2; i++) {
		  dateYear = st.nextToken();
		}
		date = new Label(dateMonth+" "+dateDay+", "+dateYear);
		myContainerDate.addComponent(date);
		this.addComponent(myContainerDate);

		myLocation = "Pasay City";//"Old Balara";
		this.addComponent(new Label("Location: " + myLocation));
		Container myContainerChildsFamilyName = new Container();
		myContainerChildsFamilyName.setLayout(boxLayout_YAxis);
		myContainerChildsFamilyName.addComponent(new Label("Child's Family Name:"));
		
		//I had to implement setting the field values that have already been set this way,
		//because myContainerChildsFamilyName.contains(childsFamilyNameTextArea) keeps on
		//telling me that there is no childsFamilyNameTextArea, but when I add the component,
		//I get an error saying that: "java.lang.IllegalArgumentException: Component is already contained in Container"
		String childsFamilyNameString="";
		if (childsFamilyNameTextArea!=null) {
			childsFamilyNameString = childsFamilyNameTextArea.getText();
		}
		childsFamilyNameTextArea = new TextArea();
		if (!childsFamilyNameString.equals("")) {
			childsFamilyNameTextArea.setText(childsFamilyNameString);
		}		
		if (isInDebugMode) {
			childsFamilyNameTextArea.setText("Default Family Name");			
		}
		
		childsFamilyNameTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
		childsFamilyNameTextArea.setSingleLineTextArea(true);
		myContainerChildsFamilyName.addComponent(childsFamilyNameTextArea);		
		this.addComponent(myContainerChildsFamilyName);
		this.setFocused(childsFamilyNameTextArea);
		
		//=============
		Container myContainerChildsGivenName = new Container();
		myContainerChildsGivenName.setLayout(boxLayout_YAxis);
		myContainerChildsGivenName.addComponent(new Label("Child's Given Name:"));
		String childsGivenNameString="";
		if (childsGivenNameTextArea!=null) {
			childsGivenNameString = childsGivenNameTextArea.getText();
		}
		childsGivenNameTextArea = new TextArea();
		if (!childsGivenNameString.equals("")) {
			childsGivenNameTextArea.setText(childsGivenNameString);
		}
		if (isInDebugMode) {
			childsGivenNameTextArea.setText("Default Given Name");			
		}
		
		childsGivenNameTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
		childsGivenNameTextArea.setSingleLineTextArea(true);
		myContainerChildsGivenName.addComponent(childsGivenNameTextArea);	
		this.addComponent(myContainerChildsGivenName);

		//=============
		Container myContainerChildsMiddleName = new Container();
		myContainerChildsMiddleName.setLayout(boxLayout_YAxis);
		myContainerChildsMiddleName.addComponent(new Label("Child's Middle Name:"));
		String childsMiddleNameString="";
		if (childsMiddleNameTextArea!=null) {
			childsMiddleNameString = childsMiddleNameTextArea.getText();
		}
		childsMiddleNameTextArea = new TextArea();
		if (!childsMiddleNameString.equals("")) {
			childsMiddleNameTextArea.setText(childsMiddleNameString);
		}
		if (isInDebugMode) {
			childsMiddleNameTextArea.setText("Default Middle Name");			
		}

		childsMiddleNameTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
		childsMiddleNameTextArea.setSingleLineTextArea(true);
		if (!myContainerChildsMiddleName.contains(childsMiddleNameTextArea)) {
			myContainerChildsMiddleName.addComponent(childsMiddleNameTextArea);		
		}
		this.addComponent(myContainerChildsMiddleName);

		//=============
		Container myContainerSex = new Container();
		myContainerSex.setLayout(boxLayout_XAxis);
		myContainerSex.addComponent(new Label("Sex:"));
		int mySexComboBoxSelectedIndex=-1;
		if (mySexComboBox!=null) {
			mySexComboBoxSelectedIndex = mySexComboBox.getSelectedIndex();
		}
		mySexComboBox = new ComboBox(new String[]{MALE_STRING,FEMALE_STRING});
		if (mySexComboBoxSelectedIndex!=-1) {
			mySexComboBox.setSelectedIndex(mySexComboBoxSelectedIndex);
		}
		myContainerSex.addComponent(mySexComboBox);
		this.addComponent(myContainerSex);
		
		//=============
		this.addComponent(new Label("Birthday:"));
		Container myContainerBirthday = new Container();
		myContainerBirthday.setLayout(boxLayout_XAxis);
		//=============		
		int myBirthdayMonthComboBoxSelectedIndex=-1;
		if (myBirthdayMonthComboBox!=null) {
			myBirthdayMonthComboBoxSelectedIndex = myBirthdayMonthComboBox.getSelectedIndex();
		}
		myBirthdayMonthComboBox = new ComboBox(myCalendarMonths);
		if (myBirthdayMonthComboBoxSelectedIndex!=-1) {
			myBirthdayMonthComboBox.setSelectedIndex(myBirthdayMonthComboBoxSelectedIndex);
		}
		myContainerBirthday.addComponent(myBirthdayMonthComboBox);

		//=============		
		String myBirthdayDayString="";
		if (myBirthdayDayTextArea!=null) {
			myBirthdayDayString = myBirthdayDayTextArea.getText();
		}
		myBirthdayDayTextArea = new TextArea();
		if (!myBirthdayDayString.equals("")) { 
			myBirthdayDayTextArea.setText(myBirthdayDayString);
		}
		else {
			myBirthdayDayTextArea.setText("1");	//default is 1		
		}
		myBirthdayDayTextArea.setColumns(2);
		myBirthdayDayTextArea.setMaxSize(2);
		myBirthdayDayTextArea.setConstraint(TextArea.NUMERIC);
		myContainerBirthday.addComponent(myBirthdayDayTextArea);

		//=============		
		String myBirthdayYearString="";
		if (myBirthdayYearTextArea!=null) {
			myBirthdayYearString = myBirthdayYearTextArea.getText();
		}
		myBirthdayYearTextArea = new TextArea();
		if (!myBirthdayYearString.equals("")) { 
			myBirthdayYearTextArea.setText(myBirthdayYearString);
		}
		else {
			myBirthdayYearTextArea.setText("2010");			
		}
		myBirthdayYearTextArea.setColumns(4);
		myBirthdayYearTextArea.setMaxSize(4);
		myBirthdayYearTextArea.setConstraint(TextArea.NUMERIC);
		myContainerBirthday.addComponent(myBirthdayYearTextArea);		
		this.addComponent(myContainerBirthday);

		//=============		
		Container myContainerWeight = new Container();
		myContainerWeight.addComponent(new Label("Weight:"));
		//use TextArea instead of TextField, as a workaround to the LWUIT bug,
		//where the user can still input non-numeric characters even though
		//the constraint, TextArea.Numeric, has been set
		String myWeightString="";
		if (myWeightTextArea!=null) {
			myWeightString = myWeightTextArea.getText();
		}
		myWeightTextArea = new TextArea();
		if (!myWeightString.equals("")) {
			myWeightTextArea.setText(myWeightString);
		}
		if (isInDebugMode) {
			myWeightTextArea.setText("7");			
		}

		myWeightTextArea.setConstraint(TextArea.DECIMAL);
		myWeightTextArea.setColumns(4);
		myWeightTextArea.setMaxSize(4);//4 digits only for weight
		myWeightTextArea.setGrowByContent(false);

		if (!myContainerWeight.contains(myWeightTextArea)) {
			myContainerWeight.addComponent(myWeightTextArea);
		}
		myContainerWeight.addComponent(new Label("Kg"));
		this.addComponent(myContainerWeight);

		//=============		
		Container myContainerTemp = new Container();
		myContainerTemp.addComponent(new Label("Temp:"));
		//use TextArea instead of TextField, as a workaround to the LWUIT bug,
		//where the user can still input non-numeric characters even though
		//the constraint, TextArea.Numeric, has been set
		String myTempString = "";
		if (myTempTextArea!=null) {
			myTempString = myTempTextArea.getText();
		}
		myTempTextArea = new TextArea();
		if (!myTempString.equals("")) {
			myTempTextArea.setText(myTempString);
		}
		if (isInDebugMode) {
			myTempTextArea.setText("37.5");			
		}

		myTempTextArea.setConstraint(TextArea.DECIMAL);
		myTempTextArea.setColumns(4);
		myTempTextArea.setMaxSize(4);//4 digits only for weight
		myTempTextArea.setGrowByContent(false);
		myContainerTemp.addComponent(myTempTextArea);	

		myContainerTemp.addComponent(new Label("°C"));
		this.addComponent(myContainerTemp);

		//=============		
		Container myContainerAddress = new Container();
		myContainerAddress.setLayout(boxLayout_XAxis);
		myContainerAddress.addComponent(new Label("Address:"));
		String addressString="";
		if (addressTextArea!=null) {
			addressString = addressTextArea.getText();
		}
		addressTextArea = new TextArea();
		if (!addressString.equals("")) {
			addressTextArea.setText(addressString);
		}
		if (isInDebugMode) {
			addressTextArea.setText("Default Address");			
		}

		addressTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
		addressTextArea.setSingleLineTextArea(true);
		myContainerAddress.addComponent(addressTextArea);		
		this.addComponent(myContainerAddress);

		//=============		
		Container myContainerMothersName = new Container();
		myContainerMothersName.setLayout(boxLayout_XAxis);
		myContainerMothersName.addComponent(new Label("Mother's Name:"));
		String mothersNameString="";
		if (mothersNameTextArea!=null) {
			mothersNameString = mothersNameTextArea.getText();
		}
		mothersNameTextArea = new TextArea();
		if (!mothersNameString.equals("")) {
			mothersNameTextArea.setText(mothersNameString);
		}
		if (isInDebugMode) {
			mothersNameTextArea.setText("Default Mother's Name");			
		}
		
		mothersNameTextArea.setColumns(SCREEN_W/Font.getDefaultFont().charWidth('L')); //any character (other than 'L') will do 
		mothersNameTextArea.setSingleLineTextArea(true);		
		myContainerMothersName.addComponent(mothersNameTextArea);	
		this.addComponent(myContainerMothersName);

		//=============		
		Label childProblemLabel = new Label("What are the child's problems?");
		childProblemLabel.startTicker(0, true);//setTickerEnabled(true);
		childProblemLabel.setFocusable(true);
		this.addComponent(childProblemLabel);
		String childProblemString=""; 
		if (childProblemTextArea!=null) {
			childProblemString = childProblemTextArea.getText();
		}
		childProblemTextArea = new TextArea();
		if (!childProblemString.equals("")) {
			childProblemTextArea.setText(childProblemString);
		}		
		if (isInDebugMode) {
			childProblemTextArea.setText("Default Child's Problem");			
		}

		childProblemTextArea.setSingleLineTextArea(true);
		this.addComponent(childProblemTextArea);

		initialVisitRadioButton = new RadioButton("Initial Visit");
		initialVisitRadioButton.setSelected(true);
		followupVisitRadioButton = new RadioButton("Follow-up Visit");
		
		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(initialVisitRadioButton);
		radioButtonGroup.add(followupVisitRadioButton);
		
		this.addComponent(initialVisitRadioButton);
		this.addComponent(followupVisitRadioButton);

		this.addCommand(nextCmd);
		this.addCommand(backCmd);		
		
		currIMCICaseList=0;
		currIMCIQuestion="";
		
		//do this to fix the order bug when textareas/comboboxes are inside one row
		myBirthdayMonthComboBox.setNextFocusDown(myBirthdayDayTextArea);
		myBirthdayDayTextArea.setNextFocusDown(myBirthdayYearTextArea);
		myBirthdayYearTextArea.setNextFocusDown(myWeightTextArea);
		myWeightTextArea.setNextFocusDown(myTempTextArea);
		myTempTextArea.setNextFocusDown(addressTextArea);

		myBirthdayDayTextArea.setNextFocusUp(myBirthdayMonthComboBox);
		myBirthdayYearTextArea.setNextFocusUp(myBirthdayDayTextArea);
		myWeightTextArea.setNextFocusUp(myBirthdayYearTextArea);
		myTempTextArea.setNextFocusUp(myWeightTextArea);
		addressTextArea.setNextFocusUp(myTempTextArea);
	}
	
	public void parseYesNoAnswers(KXmlParser parser) {
		try {
			if (parser.getName().equals("transition")) {
				  //check if the first transition's name is "No"
				  if (parser.getAttributeValue(1).toString().equals("No")) {
					  nextIMCIQuestionIfNo = parser.getAttributeValue(0).toString();
					  //do two nextTag()'s, because <transition> has a closing tag
					  parser.nextTag();
					  parser.nextTag();						  
					  nextIMCIQuestionIfYes = parser.getAttributeValue(0).toString();						  
				  }
				  else if (parser.getAttributeValue(1).toString().equals("Yes")) { // if it is "Yes"
					  nextIMCIQuestionIfYes = parser.getAttributeValue(0).toString();
					  //do two nextTag()'s, because <transition> has a closing tag
					  parser.nextTag();
					  parser.nextTag();						  
					  nextIMCIQuestionIfNo = parser.getAttributeValue(0).toString();						  							  
				  }
				  else { // if it is "Any"
					  nextIMCIQuestionIfYes = parser.getAttributeValue(0).toString();
					  nextIMCIQuestionIfNo = parser.getAttributeValue(0).toString();						  							  							  
				  }
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
	}

	//Reference: 
	//http://wiki.forum.nokia.com/index.php/How_to_parse_an_XML_file_in_Java_ME_with_kXML ;Last accessed on: June 2,2010
	//http://kxml.sourceforge.net/kxml2/ ;Last accessed on: June 2,2010
	public void initParser() {
		hasReachedEndOfAllDecisionTrees=false;
		isUsingACheckbox=false;
		isAClassificationTaskNode=false;
		isUsingATextField=false;
		isUsingASpecial=false;		
		isAGeneralDangerSignsCheckBox=false;
		isSomeDehydrationCheckBox=false;
		isSevereDehydrationCheckBox=false;
		isFirstQuestionForDecisionTree=false;
		
		InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(IMCICaseList[currIMCICaseList]));//"generalDangerSigns.xml"));//cough.xml"));//earProblem.xml"));//diarrhea.xml"));//		 
		KXmlParser parser = new KXmlParser();		 
		try {
		  parser.setInput(reader);		  

		  while(parser.nextTag() != XmlPullParser.END_DOCUMENT) {
			  //if this tag does not have an attribute; e.g. END_TAG
			  if (parser.getAttributeCount()==-1) {
				  continue;
			  }
			  else if (parser.getAttributeCount()==1) {
				  //if currIMCIQuestion is still blank
				  if ((currIMCIQuestion.equals("")) && (parser.getName().equals("start-state"))) {
					  //the next tag would be transition
					  //Example:
				      //<start-state name="start-state1">
					  //  <transition to="Is there tender swelling behind the ear?"></transition>
					  //</start-state>

					  //do two next()'s to skip the "\n", and proceed to <transition>...
//					  parser.next();
//					  parser.next();
					  //or do one nextTag();
					  parser.nextTag();
					  currIMCIQuestion=parser.getAttributeValue(0).toString();
					  
					  isFirstQuestionForDecisionTree=true;
					  continue;
				  }
				  if ((!currIMCIQuestion.equals("")) && (parser.getAttributeValue(0).toString().equals(currIMCIQuestion)) &&
						  !(parser.getName().equals("transition"))) { //make sure that the tag is not a transition node 
					  //if this is a decision tag and the next tag is transition...
					  //Example:
					  //<decision name="Is there tender swelling behind the ear?">
					  //  <transition to="Is there pus draining from the ear?" name="No"></transition>
					  //  <transition to="MASTOIDITIS" name="Yes"></transition>
					  //</decision>
					  if (parser.getName().equals("decision")) {//transition")) {//(parser.getAttributeCount()>1) {						  
						  parser.nextTag(); //go to the next tag
						  parseYesNoAnswers(parser);
					  }
					  else if (parser.getName().equals("end-state")) { 
						currIMCICaseList++;		
						if (currIMCICaseList>=MAX_IMCI_CASE_LIST) {
							hasReachedEndOfAllDecisionTrees=true;
							currIMCIQuestion="ASSESSMENT COMPLETE!";
							currIMCICaseList=0;//back to start in case IMCI diagnosis is performed again
							
							//added by Mike, Jan. 24, 2011
							//add ~ at the end of this tree (not necessarily the classification)
							//i.e. ear problem (last decision tree)
							imciAnswerContainer.addElement("~");
						}
						else {						
							nextIMCIQuestionIfYes = "";
							nextIMCIQuestionIfNo = ""; 		
							currIMCIQuestion = "";
							
							imciQuestionContainer.addElement(""+currIMCICaseList);
							imciQuestionContainerCounter++;
							
							//added by Mike, Dec. 17, 2010
							//add ~ at the end of this tree (not necessarily the classification)
							imciAnswerContainer.addElement("~");
							
							initParser();
							return; //add this so that current initPasser will no longer reach the end of its method
						}
						
					  }
					  else if (parser.getName().equals("task-node")) { 
							StringTokenizer st = new StringTokenizer(currIMCIQuestion, "~");
							String myStringToken = st.nextToken();
							if (myStringToken.equals(currIMCIQuestion)) {//if this is the task-node for classification and treatment/management plan								
								//<task-node name="SOME DEHYDRATION">
								//	<task name="Give fluid, zinc supplements and food for some dehydration (Plan B)"></task>
								//	<task name="If child also has a severe classification: 1) Refer URGENTLY to hospital with mother giving frequent sips of ORS on the way, 2) Advise the mother to continue breastfeeding"></task>
								//	<task name="Advise mother when to return immediately"></task>
								//	<task name="Follow-up in 5 days if not improving"></task>
								//	<transition to="end-state1" name="to-end"></transition>
								//</task-node>
								parser.nextTag(); //go to task tag
								isAClassificationTaskNode=true;
								classificationContainer.removeAllElements();
								while(!parser.getName().equals("transition")) {
									  classificationContainer.addElement(parser.getAttributeValue(0).toString());
									  //do two nextTag()'s, because <task> has a closing tag
									  parser.nextTag();
									  parser.nextTag();		
								}
								nextIMCIQuestionIfYes = parser.getAttributeValue(0).toString();
								nextIMCIQuestionIfNo = parser.getAttributeValue(0).toString();						  							  							  
							}
							else { //this is a task-node that has "-"								
								if (myStringToken.equals("checkList")) {
									//<task-node name="checkList-2-Check for SEVERE DEHYDRATION. Has at least two (2) of the following signs:">
									//	<task name="Is lethargic or unconscious."></task>
									//	<task name="Has sunken eyes."></task>
									//	<task name="Is not able to drink or is drinking poorly."></task>
									//	<task name="Skin pinch goes back VERY slowly (longer than 2 seconds)."></task>
									//	<transition to="SEVERE DEHYDRATION" name="Yes"></transition>
									//	<transition to="checkList-Check for SOME DEHYDRATION. Has at least two (2) of the following signs:" name="No"></transition>
									//</task-node>
									parser.nextTag(); //go to task tag
									requiredTotalCheckedBoxes = Integer.parseInt(st.nextToken());
																		
									String t=st.nextToken();
									//do this so that we are sure that the token that we are going to check is the last token
									while(st.hasMoreTokens()) {
										t=st.nextToken();
									}
									//added by Mike, Aug. 20, 2010; st.nextToken()
									if (t.equals(GENERAL_DANGER_SIGNS_CHECKBOX_QUESTION)) { //Does the question pertain to general danger signs?
										isAGeneralDangerSignsCheckBox=true;
									}
									//added by Mike, Sept. 17, 2010
									else if (t.equals(SOME_DEHYDRATION_CHECKBOX_QUESTION)) {
										isSomeDehydrationCheckBox=true;
									}
									else if (t.equals(SEVERE_DEHYDRATION_CHECKBOX_QUESTION)) {
										isSevereDehydrationCheckBox=true;
									}
									
									isUsingACheckbox=true;
									checkBoxContainer.removeAllElements();
									while(!parser.getName().equals("transition")) {
										  checkBoxContainer.addElement(parser.getAttributeValue(0).toString());
										  
										  //do two nextTag()'s, because <task> has a closing tag
										  parser.nextTag();
										  parser.nextTag();		
									}
									parseYesNoAnswers(parser);
								}
								else if (myStringToken.equals("textField")) { 
									//<task-node name="textField-For how many days?">
									//  <transition to="Does the child have wheezing? (child must be calm)" name="Any"></transition>
									//</task-node>
									parser.nextTag(); //go to transition tag
									isUsingATextField=true;
									parseYesNoAnswers(parser);
								}
								else if (myStringToken.equals("special")) { //special?
									//<task-node name="special-Give a trial of rapid acting inhaled bronchodilator for up to 3 times 15-20 minutes apart. Count the breaths and look for chest indrawing again, and then classify.">
									//  <transition to="Does the child have wheezing? (child must be calm)" name="Any"></transition>
									//</task-node>
									parser.nextTag(); //go to transition tag
									isUsingASpecial=true;
									parseYesNoAnswers(parser);
								}
							}
					  }
					  else { //this is a currIMCICaseList number
						imciQuestionContainerCounter++;
						currIMCIQuestion=(String)imciQuestionContainer.elementAt(imciQuestionContainerCounter);
						continue;
					  }
					  break;
				  }
				  //TODO dosage guide/table
				  //It would be great if someone adds the above TODO as well.
			  }
		  }
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
		
		if (!usedBackCmd) {
			imciQuestionContainer.addElement(currIMCIQuestion);
			imciQuestionContainerCounter++;
		}
		else {
			//if it IS a "general danger signs" checkbox
			//do a "usedBackCmd=false" later in initEIMCIForm()
			//we need to do this, so that we'll be able to press BACK and return to the previous imci question, and not get stuck
			if (!isAGeneralDangerSignsCheckBox) {
			  usedBackCmd=false;
			}
		}					
		initEIMCIForm();
	}
	
	//this method uses currIMCIQuestion to determine what the string should be put inside its TextArea
	public void initEIMCIForm() {
		//Set a non-TableLayout Layout Manager before calling removeAll().
		//This will ensure that if previous form used TableLayout as the 
		//Layout Manager, removeAll() will not complain of a NullPointerException.
		BoxLayout defaultLayout = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(defaultLayout);	
		
		this.removeAll();
		this.removeAllCommands();
		System.gc();
		
		this.setTitle(titleString);		

		this.addCommand(nextCmd);

		if (!hasReachedEndOfAllDecisionTrees) {
			this.addCommand(returnToTitleCmd);		
			this.addCommand(backCmd);	
		}

		//add components vertically
		BoxLayout boxLayout_YAxis = new BoxLayout(BoxLayout.Y_AXIS);
		this.setLayout(boxLayout_YAxis);	

		StringTokenizer st = new StringTokenizer(currIMCIQuestion, "~");
		String myStringToken = st.nextToken();
		while (st.hasMoreTokens()) {
			myStringToken = st.nextToken(); 
		}

		TextArea myTextArea = new TextArea(1, 2, TextArea.ANY);
		myTextArea.setText(myStringToken);//currIMCIQuestion (e.g. "Is there tender swelling behind the ear?")
		myTextArea.setEditable(false);
		myTextArea.setFocusable(false);
		this.addComponent(myTextArea);

		if (isUsingACheckbox) {			
			totalCheckedBoxes=0;
			int checkBoxContainerSize = checkBoxContainer.size();
			int checkBoxContainerGeneralDangerSignsSize = checkBoxContainerGeneralDangerSigns.size();
			int checkBoxContainerSevereDehydrationSize = checkBoxContainerSevereDehydration.size();
			checkedCheckBoxContainer.removeAllElements();
			
			for(int i=0; i<checkBoxContainerSize; i++) {				
				  final CheckBox cb = new CheckBox(checkBoxContainer.elementAt(i).toString());
				  
				  if (isAGeneralDangerSignsCheckBox) {
					  for(int k=0; k<checkBoxContainerGeneralDangerSignsSize; k++) {
						  if (checkBoxContainerGeneralDangerSigns.elementAt(k).toString().equals((checkBoxContainer.elementAt(i)).toString())) {
							  cb.setSelected(true);
							  break;
						  }
					  }
				  }				  
				  if (isSevereDehydrationCheckBox || isSomeDehydrationCheckBox) {
					  for(int h=0; h<checkBoxContainerSevereDehydrationSize; h++) {
					  	  if (checkBoxContainerSevereDehydration.elementAt(h).toString().equals((checkBoxContainer.elementAt(i)).toString())) {
							  cb.setSelected(true);
							  break;
						  }					  
					  }					  
				  }
				  
				  cb.addActionListener(new ActionListener() {
					  public void actionPerformed(ActionEvent evt) {
						  if(cb.isSelected()) {
							  totalCheckedBoxes++;
							  if (isAGeneralDangerSignsCheckBox) {
								  checkBoxContainerGeneralDangerSigns.addElement(cb.getText());
							  }
							  else if (isSevereDehydrationCheckBox) {
								  checkBoxContainerSevereDehydration.addElement(cb.getText());
							  }
							  checkedCheckBoxContainer.addElement(cb.getText());
						  } else {
							  totalCheckedBoxes--;
							  if (isAGeneralDangerSignsCheckBox) {
								  checkBoxContainerGeneralDangerSigns.removeElement(cb.getText());
							  }
							  else if (isSevereDehydrationCheckBox) {
								  checkBoxContainerSevereDehydration.removeElement(cb.getText());
							  }							  
							  checkedCheckBoxContainer.removeElement(cb.getText());
						  }
					  }
				  });
				  this.addComponent(cb);				
			}
		 }
		 else if (isAClassificationTaskNode) {
			int classificationContainerSize = classificationContainer.size();
			for(int i=0; i<classificationContainerSize; i++) {
				TextArea ta = new TextArea(classificationContainer.elementAt(i).toString());
				ta.setEditable(false);
				ta.setFocusable(true);
				this.addComponent(ta);
			}
			commentTextArea = new TextArea("EDIT THIS TO ADD COMMENT.");
			commentTextArea.setFocusable(true);
			this.addComponent(commentTextArea);
		 }
		 else if (isUsingATextField) {
			myNumericTextArea = new TextArea("");
			myNumericTextArea.setConstraint(TextArea.NUMERIC);
			myNumericTextArea.setFocus(true);
			this.addComponent(myNumericTextArea);
		 }
		 else if (isUsingASpecial) {
			 //do nothing
		 }
		 else if (hasReachedEndOfAllDecisionTrees) {
			 int size=imciClassificationAndManagementSummaryContainer.size();
			 String startString="";
			 for(int i=0; i<size; i++) {
				 startString = imciClassificationAndManagementSummaryContainer.elementAt(i).toString();
				 do {
					 String curr=imciClassificationAndManagementSummaryContainer.elementAt(i).toString();
					 if (curr.equals("EDIT THIS TO ADD COMMENT.")) {
						 //since the comment was not changed, don't display this anymore
					 }
					 else if (curr.equals("Continue with the assessment of the child.")) {
						 //don't display this anymore						 
					 }
					 else {
						 TextArea ta = new TextArea(curr);						 
						 //don't put any borders if this is not the task-name
						 if (!startString.equals(curr)) {
							 ta.getStyle().setBorder(null); 
						 }						 						 						 
						 ta.setEditable(false);
						 this.addComponent(ta);
					 }
					 i++;
				 } while(i<size && !startString.equals(imciClassificationAndManagementSummaryContainer.elementAt(i).toString()));				 
			 }
			 TextArea ta = new TextArea("OVERALL SUMMARY");
			 ta.setEditable(false);
			 this.addComponent(ta);
			 overallSummaryTextArea = new TextArea("EDIT THIS TO ADD COMMENT.");
			 overallSummaryTextArea.setEditable(true);
			 this.addComponent(overallSummaryTextArea);	
		 }
		 else {
			yesRadioButton = new RadioButton("Yes");
			yesRadioButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae){
					currIMCIQuestion = nextIMCIQuestionIfYes;
					imciAnswerContainer.addElement("Y;");

					initParser();
				}
			});
			noRadioButton = new RadioButton("No");
			noRadioButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae){
					currIMCIQuestion = nextIMCIQuestionIfNo;
					imciAnswerContainer.addElement("N;");

					initParser();
				}
			});
			
			ButtonGroup radioButtonGroup = new ButtonGroup();
			radioButtonGroup.add(yesRadioButton);
			radioButtonGroup.add(noRadioButton);
			
			this.addComponent(yesRadioButton);
			this.addComponent(noRadioButton);

			this.setFocused(yesRadioButton);
		 }		
		
		if ((isAGeneralDangerSignsCheckBox) && 
		     ((hasAskedAboutGeneralDangerSigns) || (!checkBoxContainerGeneralDangerSigns.isEmpty())) 
		    ) {		
			for (int i=0; i<checkBoxContainerGeneralDangerSigns.size(); i++) {
				checkedCheckBoxContainer.addElement(checkBoxContainerGeneralDangerSigns.elementAt(i));
			}
			totalCheckedBoxes=totalCheckedBoxesGeneralDangerSigns;
			
			//if NEXT was pressed...
			if (!usedBackCmd) {
				usedBackCmd=false;
				ActionEvent ae = new ActionEvent(nextCmd);
				actionPerformed(ae);
			}
			//if BACK was pressed...
			else {
				usedBackCmd=false;
				ActionEvent ae = new ActionEvent(backCmd);
				actionPerformed(ae);				
			}
		}
		else { //if this is the first time general danger signs are being asked...
			hasAskedAboutGeneralDangerSigns=true;
		}
	}
	
	public void resetStandardPatientFormValues() {
		if (childsFamilyNameTextArea!=null) {
			childsFamilyNameTextArea.setText(""); 
		}				
		if (childsGivenNameTextArea!=null) {
			childsGivenNameTextArea.setText("");
		}
		if (childsMiddleNameTextArea!=null) {
			childsMiddleNameTextArea.setText("");
		}		
		if (myBirthdayDayTextArea!=null) {
			myBirthdayDayTextArea.setText(""); 
		}
		if (myBirthdayYearTextArea!=null) {
			myBirthdayYearTextArea.setText(""); 
		}
		if (myWeightTextArea!=null) {
			myWeightTextArea.setText(""); 
		}
		if (myTempTextArea!=null) {
			myTempTextArea.setText(""); 
		}
		if (addressTextArea!=null) {
			addressTextArea.setText("");
		}
		if (mothersNameTextArea!=null) {
			mothersNameTextArea.setText("");
		}
		if (childProblemTextArea!=null) {
			childProblemTextArea.setText(""); 
		}
	}
	
	public void processFormChange() {
		if (currForm<0) {
			currForm=0;
		}		
		if (currForm>MAX_FORMS) {
			currForm=MAX_FORMS;
		}
			
		switch(currForm) {
			case SIGNIN_FORM:
				initLoginForm();
				break;
			case TITLE_FORM:
				initTitleForm();
				break;
			case VIEW_PENDING_FORM:
				initViewPendingForm();
				break;
			case ABOUT_FORM:
				initAboutForm();
				break;
			case STANDARD_PATIENT_FORM:
				initStandardPatientForm();
				break;
			case EIMCI_FORM:
				initParser();
				break;
			case DOSAGE_FORM:
				//TODO: add something later (e.g. initDosageForm();)
				break;
		}
	}

	public StringBuffer createSMSMessageFromIMCIAnswerContainer() {
		StringBuffer smsMessageFromIMCIAnswerContainer = new StringBuffer();		
		int size = imciAnswerContainer.size();
		for(int i=0; i<size; i++) {
			smsMessageFromIMCIAnswerContainer.append(imciAnswerContainer.elementAt(i));
		}
		
		return smsMessageFromIMCIAnswerContainer;
	}
	
	public StringBuffer createSMSMessageFromSelectedPerstPatientData() {		
		String selectedString = null;
		if (this.getFocused()!=null) {
			selectedString = ((Label)this.getFocused()).getText();			
		}
		else {
			return null;
		}
		
		StringTokenizer st = new StringTokenizer(selectedString, "|");
		String myFullNameStringToken = st.nextToken().trim();
		String myDateStringToken = st.nextToken().trim();

		PerstRoot myPerstRoot = (PerstRoot)viewPendingListDB.getRoot();
		int perstListPatientDataSize = myPerstRoot.getPerstListPatientDataSize();
		for(int i=0; i<perstListPatientDataSize; i++) {
			if ((((PerstPatientData)myPerstRoot.myPerstListPatientData.get(i)).getPatientFullName().equals(myFullNameStringToken)) &&
			    (((PerstPatientData)myPerstRoot.myPerstListPatientData.get(i)).getDateCreated().equals(myDateStringToken))		
			)
			{
				return new StringBuffer(((PerstPatientData)myPerstRoot.myPerstListPatientData.get(i)).getPatientAnswers().toString());
			}
		}
		return null;
	}
	
	public void removePendingPatientData(StringBuffer mySMSMessageBF) {
        PerstRoot myPerstRoot = (PerstRoot)viewPendingListDB.getRoot();		              
		int perstListPatientDataSize = myPerstRoot.getPerstListPatientDataSize();
		for(int i=0; i<perstListPatientDataSize; i++) {
			if ((((PerstPatientData)myPerstRoot.myPerstListPatientData.get(i)).getPatientAnswers().equals(mySMSMessageBF.toString()))
			)
			{
		        myPerstRoot.myPerstListPatientData.remove(i);
		        break;
			}
		}
		viewPendingListDB.commit();
	}
	
	public void processSendSMS() {
		//add this here to prevent multiple key presses of send SMS to occur
		hasProcessedSendSMS=true;
		mySMSMessage = null;
		if (currForm!=VIEW_PENDING_FORM) {
			//this should be on top, since WMA calls will occur after this
			mySMSMessage = createSMSMessageFromIMCIAnswerContainer();
		}
		else {
			//put value to mySMSMessage if isInViewPending
			mySMSMessage = createSMSMessageFromSelectedPerstPatientData();
		}

		if (mySMSMessage==null) {
			eIMCIUtils.launchAlertDialog("No pending patient data selected. Please select one first, before you press 'Resend'.");
			return;
		}
		
		try {			
			//put this outside so that we can catch any exception that may occur (e.g. user says no to SMS send)
			MessageConnection mc = myWMAManager.newMessageConnection("sms://+639997600670"); //this SMS number is the number of the SIM card in the GSM Modem that is attached to the server
			//for emulator debugging purposes the number below can be used
			//myWMAManager.newMessageConnection("sms://+5550000"), 
			
			myWMAManager.sendTextMessage(
					mc,
					mySMSMessage.toString(),
					null);
			
			TextArea myTextArea = new TextArea("Message successfully sent. Press 'Okay' to continue.");
			myTextArea.setFocusable(false);
			myTextArea.setEditable(false);

			final Dialog myDialog = new Dialog("Message Status");
			myDialog.addComponent(myTextArea);
			Command okCommand = new Command("Okay");
			myDialog.addCommandListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
				  myDialog.dispose();
				  
				  //Delete pending message if sms send successful
				  if (currForm==VIEW_PENDING_FORM) {
					  removePendingPatientData(mySMSMessage);
				  }
				  //Once IMCI process has been completed, go back to the title screen
				  currForm=TITLE_FORM;
				  processExitIMCIForm();
			  }
			});
			myDialog.addCommand(okCommand);
			myDialog.show();
		}
		catch(Exception e) {			
			TextArea myTextArea = new TextArea("Failed to send message. Saving message to pending list. Press 'Okay' to continue.");
			myTextArea.setFocusable(false);
			myTextArea.setEditable(false);

			final Dialog myDialog = new Dialog("Message Status");
			myDialog.addComponent(myTextArea);
			Command okCommand = new Command("Okay");
			myDialog.addCommandListener(new ActionListener() {
		      public void actionPerformed(ActionEvent ae) {
				  myDialog.dispose();

					if (currForm!=VIEW_PENDING_FORM) {
					  //Save patient data to perst database for future resending
					  myPerstPatientData = new PerstPatientData(viewPendingListDB);
					  myPerstPatientData.setPatientFullName(childsFamilyNameTextArea.getText().trim()+", "+
							  			 childsGivenNameTextArea.getText().trim()+" "+
							  			 childsMiddleNameTextArea.getText().trim());
					  myPerstPatientData.setPatientBirthday(myBirthdayMonthComboBox.getSelectedItem().toString()+
											  myBirthdayDayTextArea.getText().trim()+", "+
											  myBirthdayYearTextArea.getText().trim());
					  myPerstPatientData.setDateCreated(date.getText());				  
					  myPerstPatientData.setPatientAnswers(mySMSMessage.toString());
	
		              PerstRoot myPerstRoot = (PerstRoot)viewPendingListDB.getRoot();
		              myPerstRoot.myPerstListPatientData.add(myPerstPatientData);
		              viewPendingListDB.commit();
				  }
				  //Once IMCI process has been completed, go back to the title screen
				  currForm=TITLE_FORM;
				  processExitIMCIForm();
		      }
			});
			myDialog.addCommand(okCommand);
			myDialog.show();

			e.printStackTrace();
		}
	}
	
	public boolean validateStandardPatientForm() {
		if (childsFamilyNameTextArea.getText().trim().equals("")) {
			return false;
		}
		if (childsGivenNameTextArea.getText().trim().equals("")) {
			return false;
		}
		if (childsMiddleNameTextArea.getText().trim().equals("")) {
			return false;
		}
		//there is already a value for myBirthdayMonthTextArea, so 
		//no need to add that here anymore
		if (myBirthdayDayTextArea.getText().trim().equals("")) {
			return false;
		}
		if (myBirthdayYearTextArea.getText().trim().equals("")) {
			return false;
		}
		if (addressTextArea.getText().trim().equals("")) {
			return false;
		}
		if (mothersNameTextArea.getText().trim().equals("")) {
			return false;
		}
		if (myWeightTextArea.getText().trim().equals("")) {
			return false;
		}
		if (myTempTextArea.getText().trim().equals("")) {
			return false;
		}
		if (childProblemTextArea.getText().trim().equals("")) {
			return false;
		}
		return true;
	}
	
	//do this so that left soft key(i.e. "select") and FIRE button execute the same methods
	public void processPerformIMCIButtonPress() {
		resetStandardPatientFormValues();
		currForm++;
	}

	public void actionPerformed(ActionEvent ae) {
		//HANDLE COMMANDS
		if (ae.getSource()==nextCmd) {
			if (currForm==EIMCI_FORM) {
				if (!hasReachedEndOfAllDecisionTrees) {					
					if (isUsingACheckbox) {
						if (totalCheckedBoxes>=requiredTotalCheckedBoxes) {
							currIMCIQuestion = nextIMCIQuestionIfYes;

							StringBuffer sb = new StringBuffer("Y");
							int checkedCheckBoxSize = checkedCheckBoxContainer.size();
							int checkBoxSize = checkBoxContainer.size();							
							for(int i=0; i<checkedCheckBoxSize; i++) {
								for(int h=0; h<checkBoxSize; h++) {
									if (checkedCheckBoxContainer.elementAt(i).equals(checkBoxContainer.elementAt(h))) {
										sb.append(","+h);
									}
								}								
							}
							sb.append(";");
							imciAnswerContainer.addElement(sb.toString());
						}
						else {
							currIMCIQuestion = nextIMCIQuestionIfNo;
							imciAnswerContainer.addElement("N;");															
						}
						
						//Added by Mike, Aug. 16, 2010
						if ((isAGeneralDangerSignsCheckBox)/* && (!hasAskedAboutGeneralDangerSigns)*/) {
							totalCheckedBoxesGeneralDangerSigns=totalCheckedBoxes;
						}
					}
					else if (isUsingATextField) {
						currIMCIQuestion = nextIMCIQuestionIfYes; //= nextIMCIQuestionIfNo will also do
						if (myNumericTextArea.getText().trim().equals("")) {
							imciAnswerContainer.addElement("0;");							
						}
						else {
							imciAnswerContainer.addElement(myNumericTextArea.getText()+";");							
						}
					}
					else if (isAClassificationTaskNode) {
						//add these to imciClassificationAndManagementSummaryContainer,
						//while following this format
						//currIMCIQuestion (e.g. "PNEUMONIA")
						//task (e.g. "Give oral antibiotic for 3 days")
						//task (e.g. "If wheezing (even if disappeared after rapidly acting bronchodilator) give an inhaled bronchodilator for 5 days (in settings where inhaled bronchodilator is not available, oral salbutamol may be the second choice)")
						//task (e.g. "Soothe the throat and relieve the cough with a safe remedy"")
						//task (e.g. "If coughing for more than 3 weeks or if having recurrent wheezing, refer for assessment for TB or asthma")
						//task (e.g. "Advise the mother when to return immediately")
						//task (e.g. "Follow-up in 2 days")

						//comment 
						//currIMCIQuestion (e.g. "PNEUMONIA")
						//note: the first and last elements that will be inserted should be the currIMCIQuestion
						imciClassificationAndManagementSummaryContainer.addElement(currIMCIQuestion);
						currImciClassificationAndManagementSummary++;
						int classificationContainerSize = classificationContainer.size();

						for(int i=0; i<classificationContainerSize; i++) {
							imciClassificationAndManagementSummaryContainer.addElement(classificationContainer.elementAt(i).toString());
							currImciClassificationAndManagementSummary++;
						}
						imciClassificationAndManagementSummaryContainer.addElement(commentTextArea.getText());
						imciClassificationAndManagementSummaryContainer.addElement(currIMCIQuestion);
						currImciClassificationAndManagementSummary+=2;
						
						currIMCIQuestion = nextIMCIQuestionIfYes; //= nextIMCIQuestionIfNo will also do
						if (commentTextArea.getText().equals("EDIT THIS TO ADD COMMENT.")) {
							imciAnswerContainer.addElement("Y,;");
						}
						else {
							imciAnswerContainer.addElement("Y,"+commentTextArea.getText()+";");							
						}
					}
					else if (isUsingASpecial) {
						currIMCIQuestion = nextIMCIQuestionIfYes; //= nextIMCIQuestionIfNo will also do
						imciAnswerContainer.addElement("Y;");
					}
					else { //if this is a yes/no node
						if (this.getFocused().equals(noRadioButton)) {
							currIMCIQuestion = nextIMCIQuestionIfNo;
							if (isFirstQuestionForDecisionTree) {
							  imciAnswerContainer.addElement("N;");
							}
							else {
							  imciAnswerContainer.addElement("N;");								
							}
						}
						else if (this.getFocused().equals(yesRadioButton)) {
							currIMCIQuestion = nextIMCIQuestionIfYes;						
							imciAnswerContainer.addElement("Y;");
						}
					}
					
					initParser();
					return;
				}
				else {			
					if (!hasProcessedSendSMS) {//this would prevent the user from pressing send SMS multiple times on the same patient	
						if (overallSummaryTextArea.getText().equals("EDIT THIS TO ADD COMMENT.")) {
							imciAnswerContainer.addElement(";~");					
						}
						else {
							imciAnswerContainer.addElement(overallSummaryTextArea.getText()+";~");					
						}
						processSendSMS();						
					}
				}
			}
			else if (currForm==STANDARD_PATIENT_FORM) {
				//check if all the fields have values
				if (validateStandardPatientForm()) {
					//the following order is different to what is displayed, 
					//because doing this would make inserting entries to the openmrs database easier by grouping the encounters and patient data
					StringBuffer sb = new StringBuffer();
					sb.append(dateMonth + " " + dateDay + " " + dateYear + ";");
					sb.append("4" + ";"); //3 = Old Balara; 4 = Pasay City
					sb.append(childsFamilyNameTextArea.getText()+";");
					sb.append(childsGivenNameTextArea.getText()+";");
					sb.append(childsMiddleNameTextArea.getText()+";");
					if (mySexComboBox.getSelectedItem().toString().equals(MALE_STRING)){
						sb.append("M;");				
					}
					else {
						sb.append("F;");									
					}
					sb.append(myBirthdayMonthComboBox.getSelectedItem().toString()+" "+myBirthdayDayTextArea.getText()+" "+myBirthdayYearTextArea.getText()+";");
					sb.append(addressTextArea.getText()+";");
					sb.append(mothersNameTextArea.getText()+";");
					sb.append("~");
	
					if (myWeightTextArea.getText().trim().equals("")) {
						sb.append("0;");					
					}
					else {
						sb.append(myWeightTextArea.getText()+";");
					}
					if (myTempTextArea.getText().trim().equals("")) {
						sb.append("0;");					
					}
					else {
						sb.append(myTempTextArea.getText()+";");
					}
	
					if (initialVisitRadioButton.isSelected()) {
						sb.append("Y;");					
					}
					else {
						sb.append("N;");										
					}
					sb.append(childProblemTextArea.getText()+";");				
	
					imciAnswerContainer.addElement(sb.toString()+"~");
					currForm++;					
				}
				else {
					TextArea myTextArea = new TextArea("There is at least one field in the standard patient form that has not yet been filled up. You must first answer the remaining field/s to be able to proceed to the next screen.");
					myTextArea.setFocusable(false);
					myTextArea.setEditable(false);

					final Dialog myDialog = new Dialog("Validation Status");
					myDialog.addComponent(myTextArea);
					Command okCommand = new Command("Okay");
					myDialog.addCommandListener(new ActionListener() {
				      public void actionPerformed(ActionEvent ae) {
						  myDialog.dispose();
				      }
					});
					myDialog.addCommand(okCommand);
					myDialog.show();
				}
			}
			else {
				currForm++;					
			}
		}
		else if (ae.getSource()==backCmd){
			if (currForm==ABOUT_FORM) {
				currForm=TITLE_FORM;
			}
			else if (currForm==VIEW_PENDING_FORM) {
				currForm=TITLE_FORM;
			}
			else if (currForm==EIMCI_FORM) {
				usedBackCmd=true;
				//added by Mike, Sept. 29, 2010
				imciAnswerContainer.removeElementAt(imciAnswerContainer.size()-1);
				
				imciQuestionContainer.removeElementAt(imciQuestionContainerCounter);				
				imciQuestionContainerCounter--;
				if (imciQuestionContainerCounter>=0) {
					currIMCIQuestion=(String)imciQuestionContainer.elementAt(imciQuestionContainerCounter);
				}
				else { //if imciQuestionContainerCounter is negative, that means that imciQuestionContainer is now empty, and currIMCIQuestion should now be 0 or GENERAL_DANGER_SIGNS
					currIMCIQuestion = ""+GENERAL_DANGER_SIGNS;
				}
								
				try {
					int temp = Integer.parseInt(currIMCIQuestion);
					
					switch(temp) {
						case GENERAL_DANGER_SIGNS:
							currForm--;		
							currIMCICaseList=GENERAL_DANGER_SIGNS;
							processExitIMCIForm();
							
							usedBackCmd=false;
							processFormChange();
							return;
						default: //for any other currIMCIQuestion
							currIMCICaseList--;
							imciQuestionContainer.removeElementAt(imciQuestionContainerCounter);
							imciQuestionContainerCounter--;
							currIMCIQuestion=(String)imciQuestionContainer.elementAt(imciQuestionContainerCounter);
							break;
					}

				}
				catch(Exception e) { //if currIMCIQuestion is not an int (i.e. not a currIMCICaseList)
					currIMCIQuestion=(String)imciQuestionContainer.elementAt(imciQuestionContainerCounter);					
				}
				
				//if currIMCIQuestion is all upper case (i.e. name of a task-node)...
				if(currIMCIQuestion.equals(currIMCIQuestion.toUpperCase())) { 
					//only do below if imciClassificationAndManagementSummaryContainer is not empty AND
					//currIMCIQuestion has already been added to imciClassificationAndManagementSummaryContainer
					//this would prevent users from getting an ArrayIndexOutOfBounds error when they get to a classification/management screen, and hit BACK
					//if users hit NEXT, and then BACK, this block of code will be executed.
					if (!imciClassificationAndManagementSummaryContainer.isEmpty() &&
						imciClassificationAndManagementSummaryContainer.elementAt(currImciClassificationAndManagementSummary).equals(currIMCIQuestion)) {
						imciClassificationAndManagementSummaryContainer.removeElementAt(currImciClassificationAndManagementSummary);
						currImciClassificationAndManagementSummary--;
						int temp;
						for(temp=currImciClassificationAndManagementSummary; 
							!imciClassificationAndManagementSummaryContainer.elementAt(currImciClassificationAndManagementSummary).equals(currIMCIQuestion);
							temp--) {
							imciClassificationAndManagementSummaryContainer.removeElementAt(temp);
							currImciClassificationAndManagementSummary--;
						}
						currImciClassificationAndManagementSummary=temp;
						imciClassificationAndManagementSummaryContainer.removeElementAt(currImciClassificationAndManagementSummary);
						currImciClassificationAndManagementSummary--;
					}
				}		

				//this means that the imciQuestion before this would be the very first general danger sign,
				//so don't skip (i.e. process without displaying) that--which is what we do whenever the parser finds another general danger signs
				if (imciQuestionContainer.size()==2) { 
				     hasAskedAboutGeneralDangerSigns=false; 					
				     checkBoxContainerGeneralDangerSigns.removeAllElements();
				}
								
				initParser();
				return;
			}
			else {
				currForm--;				
				currIMCIQuestion="";
			}
		}
		else if (ae.getSource()==exitCmd) {		
			myMIDlet.notifyDestroyed();
		}
		else if (ae.getSource()==returnToTitleCmd) {	
			//Once IMCI process has been completed, go back to the title screen
			currForm=TITLE_FORM;

			processExitIMCIForm();
		}
		else if (ae.getSource()==selectCmd) {
			if (currForm==TITLE_FORM) {
				if (this.getFocused()==performIMCIButton) {
					processPerformIMCIButtonPress();
				}
				else if (this.getFocused()==viewPendingButton){				
					currForm=VIEW_PENDING_FORM;
				}
				else if (this.getFocused()==aboutButton){				
					currForm=ABOUT_FORM;
				}
			}
		}			
		else if (ae.getSource()==signInCmd) {
			if ((usernameTextArea.getText().equals("ADMIN")) && (passwordTextArea.getText().equals("ADMIN"))) {
			  currForm=TITLE_FORM;						
			}
			else {
			  hasErrorSigningInLabel=true;
			  if (currNumOfSigninTries<MAX_SIGNIN_TRIES) {
				  currNumOfSigninTries++;
			  }
			  else {
				  TextArea myTextArea = new TextArea("You have failed to sign in 3 consecutive times already. As a form of security, the application will now shutdown.");
				  myTextArea.setFocusable(false);
				  myTextArea.setEditable(false);
				  myTextArea.getStyle().setBgColor(0xFF0000); //color the font red
				  myTextArea.getStyle().setFgColor(0xFFFFFF); //color the font red
				  Font f = Font.createSystemFont(Font.FACE_PROPORTIONAL,
		                                       Font.STYLE_BOLD,Font.SIZE_MEDIUM);
				  myTextArea.getStyle().setFont(f);
	
				  final Dialog myDialog = new Dialog("Alert!");
				  myDialog.addComponent(myTextArea);
				  Command okCommand = new Command("Okay");
				  myDialog.addCommand(okCommand);
				  myDialog.addCommandListener(new ActionListener() {
				    public void actionPerformed(ActionEvent ae) {
				    	  myDialog.dispose();
				    	  myMIDlet.notifyDestroyed();
					  }
				  });
				  myDialog.show();
			  }
			}
		}
		else if (ae.getSource()==resendSMSCmd) {
			processSendSMS();
		}
		else if (ae.getSource()==deletePendingDataCmd) {
			//put value to mySMSMessage if isInViewPending
			mySMSMessage = createSMSMessageFromSelectedPerstPatientData();
			if (mySMSMessage==null) {
				eIMCIUtils.launchAlertDialog("No pending patient data selected. Please select one first, before you press 'Delete'.");
				return;
			}
			removePendingPatientData(mySMSMessage);
			
		}

		//HANDLE BUTTONS
		else if (ae.getSource()==performIMCIButton) {
			processPerformIMCIButtonPress();
			
		}			
		else if (ae.getSource()==viewPendingButton) {
			currForm=VIEW_PENDING_FORM;
		}
		else if (ae.getSource()==aboutButton) {
			currForm=ABOUT_FORM;
		}
		
		processFormChange();
	}
	
	public void processExitIMCIForm() {
		currIMCIQuestion="";
		imciQuestionContainer.removeAllElements();
		imciQuestionContainer.addElement(""+currIMCICaseList); //default value of imciQuestionContainer is 0
		imciQuestionContainerCounter=0;

		imciClassificationAndManagementSummaryContainer.removeAllElements();
		currImciClassificationAndManagementSummary=-1;
		
		checkBoxContainerGeneralDangerSigns.removeAllElements();
		
		//added by Mike, Dec. 15, 2010
		totalCheckedBoxesGeneralDangerSigns=0;
		totalCheckedBoxes=0;
		checkedCheckBoxContainer.removeAllElements();		
		hasAskedAboutGeneralDangerSigns=false;
		imciAnswerContainer.removeAllElements();
		hasProcessedSendSMS=false;		
	}
}