/*
 *  This is a custom-made subclass of McObject's persistent class that contains patient-related fields which are saved in the perst lite database for easy retrieval of pending SMS patient data.
 *  Copyright (C) 2011  Michael Syson
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
 * Important:
 * "Whether you’re developing open source software or a proprietary, commercial product, McObject has Perst licensing options that fit.
 * Users can redistribute and/or modify Perst under the terms of the GNU General Public License version 3 (or earlier versions) as published by the Free Software Foundation.
 * For projects that don't fit under the GPL, a commercial license for Perst may be purchased from McObject. McObject also offers Perst product support."
 * Reference: http://www.mcobject.com/perst
 * 
 * In other words:
 * You can also obtain a commercial license for Perst if your project doesn't fit under the GPL!
 * 
 */
package perst;
//Reference: PerstProScoutDemo (Root.java)-- can be downloaded from the link below
//http://www.mcobject.com/index.cfm?fuseaction=download&pageid=498&sectionid=133 ; last accessed on March 30, 2011
import org.garret.perst.*;

//In Perst Lite all object in the storage should be accessible though
//single root object. So this object should contains collections which
//keeps references to all application top-level objects.
public class PerstPatientData extends Persistent{	
    private String patientFullName;
    private String patientBirthday;
    private String dateCreated;
    private String patientAnswers;
	
	// Default constructor (constructor without parameters) should not be used for object initialization since it is
    // used to instantiate object each time when it is loaded from the database.
    // So class should either not have constructor at all (in this case it
    // will be generated automatically by compiler), either provide
    // empty default constructor and constructor used for object
    // initialization (usually it is passed reference to the Storage
    // since it is need to create Perst collections).
	public PerstPatientData() {
	}
	
    // This constructor is called once while database initialization.
    public PerstPatientData(Storage db) { 
        super(db);        
    }

    //GETTERS
    public String getPatientFullName() {
    	return patientFullName;
    }

    public String GetPatientBirthday() {
    	return patientBirthday;
    }

    public String getDateCreated() {
    	return dateCreated;
    }

    public String getPatientAnswers() {
    	return patientAnswers;
    }

    //SETTERS
    public void setPatientFullName(String s) {
    	patientFullName = s;
    }

    public void setPatientBirthday(String s) {
    	patientBirthday = s;
    }

    public void setDateCreated(String s) {
    	dateCreated = s;
    }

    public void setPatientAnswers(String s) {
    	patientAnswers = s;
    }

    // Deserialize the object
    public void readObject(IInputStream in) {     	
    	patientFullName = in.readString();
    	patientBirthday = in.readString();
    	dateCreated = in.readString();
    	patientAnswers = in.readString();
    }

    // Serialize the object
    public void writeObject(IOutputStream out) { 
    	out.writeString(patientFullName);
    	out.writeString(patientBirthday);
    	out.writeString(dateCreated);
        out.writeString(patientAnswers);
    }
}