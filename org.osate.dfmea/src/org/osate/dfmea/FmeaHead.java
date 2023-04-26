package org.osate.dfmea;

public class FmeaHead {

	public Structure ref_component;

	public String Company_Name;
	public String Engineering_Location;
	public String Customer_Name;
	public String Model_Year_Program;

	public String Subject;
	public String DFMEA_Start_Data;
	public String DFMEA_Revision_Data;
	public String Cross_Func_Team;

	public String DFMEA_ID;
	public String Design_Responsibility;
	public String Confidentiality_Level;

	public String Focus_component_name;

	public void Print(String indent) {
		System.out.print(indent + "FMEA Head of \"" + ref_component.getName() + "\"::");

		if (Company_Name != null) {
			System.out.print("  |Company_Name:: " + Company_Name);
		}
		if (Engineering_Location != null) {
			System.out.print("  |Engineering_Location:: " + Engineering_Location);
		}
		if (Customer_Name != null) {
			System.out.print("  |Customer_Name:: " + Customer_Name);
		}
		if (Model_Year_Program != null) {
			System.out.print("  |Model_Year_Program:: " + Model_Year_Program);
		}
		if (Subject != null) {
			System.out.print("  |Subject:: " + Subject);
		}
		if (DFMEA_Start_Data != null) {
			System.out.print("  |DFMEA_Start_Data:: " + DFMEA_Start_Data);
		}
		if (DFMEA_Revision_Data != null) {
			System.out.print("  |DFMEA_Revision_Data:: " + DFMEA_Revision_Data);
		}
		if (Cross_Func_Team != null) {
			System.out.print("  |Cross_Func_Team:: " + Cross_Func_Team);
		}
		if (DFMEA_ID != null) {
			System.out.print("  |DFMEA_ID:: " + DFMEA_ID);
		}
		if (Design_Responsibility != null) {
			System.out.print("  |Design_Responsibility:: " + Design_Responsibility);
		}
		if (Confidentiality_Level != null) {
			System.out.print("  |Confidentiality_Level:: " + Confidentiality_Level);
		}
		if (Focus_component_name != null) {
			System.out.print("  |Focus_component_name:: " + Focus_component_name);
		}
	}

}
