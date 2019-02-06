package com.axway.apim;

import java.io.File;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class MainTest {

	
	
	
	@Test
	public void testExport(){
		
		
		File file = new File("src/test/resources/petstore.json");
		String[] args = new String[] { "--operation=export",
				 "--url=https://10.129.60.57:8075", "--username=apiadmin",
				 "--password=changeme", 
				 "--artifactlocation="+file.getAbsolutePath(),
				 "--apiname=petstore", "--version=1.0.0" };
		
		try {
			Main.main(args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
