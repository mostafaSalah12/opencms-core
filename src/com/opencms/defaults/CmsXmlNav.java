/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/Attic/CmsXmlNav.java,v $
 * Date   : $Date: 2000/04/06 10:25:57 $
 * Version: $Revision: 1.5 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.defaults;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.*;

/**
 * This class builds the default Navigation.
 * 
 * @author Alexander Kandzior
 * @author Waruschan Babachan
 * @version $Revision: 1.5 $ $Date: 2000/04/06 10:25:57 $
 */
public class CmsXmlNav extends A_CmsNavBase {
		
	
	/** 
	 * gets the current folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFolderCurrent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
		return currentFolder.getBytes();
	}
	
	
	/** 
	 * gets the root folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getFolderRoot(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String rootFolder=((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath()+"/";
		return rootFolder.getBytes();
	}
	
	
	/** 
	 * gets the navigation of current folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavCurrent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();				
		
		Vector resources=cms.getSubFolders(currentFolder);
		Vector allFile=cms.getFilesInFolder(currentFolder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
		
		return buildNav(cms,doc,resources).getBytes();
	}
	
	/** 
	 * gets the navigation of specified level of parent folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavParent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		int level=0;
		// tagcontent determines the folder starting from parent folder.
		// if tagcontent is null, zero or negative, then the navigation of current
		// folder must be showed.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent);
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		}
		String currentFolder="";
		if (level<=0) {
			currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
		} else {
			// level is converted to negative number, so I can use the method 
			// "extractFolder" for positive and negative numbers. Negative number
			// determines the parent folder level starting from current folder and
			// positive number determines the level starting ftom root folder.
			currentFolder=extractFolder(cms,((-1)*level));
		}
		Vector resources=cms.getSubFolders(currentFolder);
		Vector allFile=cms.getFilesInFolder(currentFolder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
		
		return buildNav(cms,doc,resources).getBytes();
	}
			
	
	/** 
	 * gets the navigation of root folder or parent folder starting from root folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavRoot(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
				
		int level=0;
		// tagcontent determines the folder starting from root folder.
		// if tagcontent is null, then the navigation of root folder must be showed.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent);
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		}
		String currentFolder="";
		if (level<=0) {
			currentFolder=currentFolder=cms.rootFolder().getAbsolutePath();
		} else {
			currentFolder=extractFolder(cms,level);
		}		
		// get all resources, it means all files and folders.
		Vector resources=cms.getSubFolders(currentFolder);
		Vector allFile=cms.getFilesInFolder(currentFolder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
		
		return buildNav(cms,doc,resources).getBytes();
	}
		
	
	/** 
	 * gets the navigation of folders recursive.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getNavTree(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		int level=0;
		// if level is zero or null or negative then all folders recursive must  
		// be showed starting from root folder unless all folders stating from 
		// specified level of parent folder.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent);
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		}
		String folder="";
		if (level<=0) {
			folder=cms.rootFolder().getAbsolutePath();
		} else {
			folder=extractFolder(cms,level);
		}
		
		Vector resources=cms.getSubFolders(folder);
		Vector allFile=cms.getFilesInFolder(folder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
		
		String requestedUri = cms.getRequestContext().getUri();
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
		String servletPath = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
		CmsXmlTemplateFile xmlDataBlock=(CmsXmlTemplateFile)doc;
		
		String result="";
		// check wheather xml data blocks are defined.
		if (xmlDataBlock.hasData("navEntry")) {
			if (!xmlDataBlock.hasData("navCurrent")) {
				xmlDataBlock.setData("navCurrent", xmlDataBlock.getDataValue("navEntry"));
			}
			if (!xmlDataBlock.hasData("navTreeStart")) {
				xmlDataBlock.setData("navTreeStart", "");
			}
			if (!xmlDataBlock.hasData("navTreeEnd")) {
				xmlDataBlock.setData("navTreeEnd", "");
			}
			result=buildNavTree(cms,xmlDataBlock,resources,requestedUri,currentFolder,servletPath);
		}
		
		return result.getBytes();
	}
	
		
	/** 
	 * gets the current page.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPageCurrent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		return (cms.getRequestContext().getUri());
	}
	
	
	/** 
	 * gets the next page.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPageNext(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject)
           throws CmsException {
		
		String requestedUri = cms.getRequestContext().getUri();
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();				
		
		Vector resources=cms.getSubFolders(currentFolder);
		Vector allFile=cms.getFilesInFolder(currentFolder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
		int size = resources.size();
		
		String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        
		int pos=0;
		int max=extractNav(cms,resources,navLink,navText,navPos);
		for(int i=0; i<max; i++) {
			// Check if nav is current nav
			if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
				if ((i+1)>max) {
					pos=max;
				} else {
					pos=i+1;
				}
			}
        }
		
		return (navLink[pos]);
	}
	
	
	/** 
	 * gets the previous page.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPagePrevious(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String requestedUri = cms.getRequestContext().getUri();
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();				
		
		Vector resources=cms.getSubFolders(currentFolder);
		Vector allFile=cms.getFilesInFolder(currentFolder);
		
		resources.ensureCapacity(resources.size() + allFile.size());
		Enumeration e = allFile.elements();
		while (e.hasMoreElements()) {
			resources.addElement(e.nextElement());
		}
		int size = resources.size();
		
		String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        
		int pos=0;
		int max=extractNav(cms,resources,navLink,navText,navPos);
		for(int i=0; i<max; i++) {
			// Check if nav is current nav
			if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
				if ((i-1)>0) {
					pos=i-1;
				} else {
					pos=0;
				}
			}
        }
		
		return (navLink[pos]);
	}
	
	
	/** 
	 * gets the current page or parent page starting from current folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent The level of parent's page.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPageParent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		int level=0;
		// tagcontent determines the parent folder starting from root folder.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent);
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		}
		
		if (level>0) {
			String requestedUri = cms.getRequestContext().getUri();
			String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();				
			
			Vector resources=cms.getSubFolders(currentFolder);
			Vector allFile=cms.getFilesInFolder(currentFolder);
			
			resources.ensureCapacity(resources.size() + allFile.size());
			Enumeration e = allFile.elements();
			while (e.hasMoreElements()) {
				resources.addElement(e.nextElement());
			}
			int size = resources.size();
			
			String navLink[] = new String[size];
			String navText[] = new String[size];
			float navPos[] = new float[size];
			
			int pos=0;
			int max=extractNav(cms,resources,navLink,navText,navPos);
			for(int i=0; i<max; i++) {
				// Check if nav is current nav
				if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
					if ((i-level)<0) {
						pos=0;
					} else {
						pos=i-level;
					}					
				}
			}			
			return (navLink[pos]);
		}
		
		return cms.getRequestContext().getUri();
	}
	
	
	/** 
	 * gets the root page or parent page starting from root folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPageRoot(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
			throws CmsException {
		
		int level=0;
		// the level must be a positive number.
		// if level is zero then it means the root page unless it means the page
		// of specified level starting from root page.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent);
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		}
		if (level>0) {
			String requestedUri = cms.getRequestContext().getUri();
			String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();				
			
			Vector resources=cms.getSubFolders(currentFolder);
			Vector allFile=cms.getFilesInFolder(currentFolder);
			
			resources.ensureCapacity(resources.size() + allFile.size());
			Enumeration e = allFile.elements();
			while (e.hasMoreElements()) {
				resources.addElement(e.nextElement());
			}
			int size = resources.size();
			
			String navLink[] = new String[size];
			String navText[] = new String[size];
			float navPos[] = new float[size];
			
			int pos=0;
			int max=extractNav(cms,resources,navLink,navText,navPos);
			for(int i=0; i<max; i++) {
				// Check if nav is current nav
				if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
					if (level<i) {
						pos=level;
					} else {
						pos=i;
					}
				}
			}		
			return (navLink[pos]);
		}
		return (((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath()+"/");
	}
		
	
	/** 
	 * gets a specified property of current folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyCurrent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String property="";
		// tagcontent must contain the property definition name.
		if (tagcontent!=null) {		
			String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
			
			property=cms.readProperty(currentFolder, tagcontent);		
			if (property==null) {
				property="";
			}
		}
		return (property.getBytes());
	}
	
	
	/** 
	 * gets a specified property of specified folder starting from current folder.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyParent(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		int level=0;
		String property="";
		// tagcontent determines the parent folder starting from current folder and
		// the property definition name sparated by a comma.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent.substring(0,tagcontent.indexOf(",")));
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		
			String currentFolder="";
			if (level<=0) {
				currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
			} else {
				// level is converted to negative number, so I can use the method 
				// "extractFolder" for positive and negative numbers. Negative number
				// determines the parent folder level starting from current folder and
				// positive number determines the level starting ftom root folder.
				currentFolder=extractFolder(cms,((-1)*level));
			}
					
			property=cms.readProperty(currentFolder, tagcontent.substring(tagcontent.indexOf(",")+1));
			if (property==null) {
				property="";
			}
		}
		return (property.getBytes());
	}
	
	
	/** 
	 * gets a specified property of specified folder starting from root.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyRoot(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		int level=0;
		String property="";
		// tagcontent determines the folder starting from root folder and
		// the property definition name sparated by a comma.
		if (tagcontent!=null) {
			try {
				level=Integer.parseInt(tagcontent.substring(0,tagcontent.indexOf(",")));
			} catch(NumberFormatException e) {
				throw new CmsException(e.getMessage());
			}
		
			String currentFolder="";
			if (level<=0) {
				currentFolder=currentFolder=cms.rootFolder().getAbsolutePath();
			} else {
				currentFolder=extractFolder(cms,level);
			}
					
			property=cms.readProperty(currentFolder, tagcontent.substring(tagcontent.indexOf(",")+1));
			if (property==null) {
				property="";
			}
		}
		return (property.getBytes());
	}
	
	
	/** 
	 * gets a specified property of uri.
	 * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getPropertyUri(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String property="";
		String requestedUri = cms.getRequestContext().getUri();
		
		property=cms.readProperty(requestedUri, tagcontent);		
		if (property==null) {
			property="";
		}
		
		return (property.getBytes());
	}
	
	
	/**
	 * Builds the navigation.
	 * 
	 * @param cms A_CmsObject Object for accessing system resources.    
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.
	 * @param resources a vector that contains the elements of navigation.
	 * @param currentFolder the currenet folder.
	 * @return String that contains the navigation.
	 */	
	private String buildNav(A_CmsObject cms, A_CmsXmlContent doc, Vector resources)
		throws CmsException {
			
		String requestedUri = cms.getRequestContext().getUri();
		String currentFolder=cms.getRequestContext().currentFolder().getAbsolutePath();
		String servletPath = ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
		CmsXmlTemplateFile xmlDataBlock=(CmsXmlTemplateFile)doc;
		StringBuffer result = new StringBuffer();
				
		int size = resources.size();
				
		String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        
		int max=extractNav(cms,resources,navLink,navText,navPos);
				
        // The arrays folderNames and folderTitles now contain all folders
        // that should appear in the nav.
        // Loop through all folders and generate output
		if (xmlDataBlock.hasData("navEntry")) {
			if (!xmlDataBlock.hasData("navCurrent")) {
				xmlDataBlock.setData("navCurrent", xmlDataBlock.getDataValue("navEntry"));
			}
			for(int i=0; i<max; i++) {			
				xmlDataBlock.setData("navText", navText[i]);
				xmlDataBlock.setData("count", new Integer(i+1).toString());
				// this if condition is necessary because of url parameter,
				// if there is no filename then the parameters are ignored, so I
				// can't use e.g. ?cmsframe=body.
				if (navLink[i].endsWith("/")) {
					xmlDataBlock.setData("navLink", servletPath + navLink[i] + "index.html" );
				} else {
					xmlDataBlock.setData("navLink", servletPath + navLink[i] );
				}
				// Check if nav is current nav				
				if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
					result.append(xmlDataBlock.getProcessedDataValue("navCurrent"));
				} else {
					result.append(xmlDataBlock.getProcessedDataValue("navEntry"));
				}
			}
		}
		
		return result.toString();
		
	}
	
	
	/**
	 * Builds the tree of navigation.
	 * 
	 * @param cms A_CmsObject Object for accessing system resources.    
     * @param doc Reference to the CmsXmTemplateFile object of the initiating XLM document.
	 * @param resources a vector that contains the elements of navigation.
	 * @param requestedUri The absolute path of current requested file. 
	 * @param currentFolder The currenet folder.
	 * @param servletPath The absolute path of servlet
	 * @return String that contains the navigation.
	 */	
	private String buildNavTree(A_CmsObject cms, CmsXmlTemplateFile xmlDataBlock, Vector resources, String requestedUri, String currentFolder, String servletPath)
		throws CmsException {
		
		StringBuffer result = new StringBuffer();
		
		int size = resources.size();		
		String navLink[] = new String[size];
        String navText[] = new String[size];
        float navPos[] = new float[size];
        
		int max=extractNav(cms,resources,navLink,navText,navPos);
		if (max>0) {
			result.append(xmlDataBlock.getProcessedDataValue("navTreeStart"));
			for(int i=0; i<max; i++) {
				xmlDataBlock.setData("navText", navText[i]);
				xmlDataBlock.setData("count", new Integer(i+1).toString());
				// this if condition is necessary because of url parameter,
				// if there is no filename then the parameters are ignored, so I
				// can't use e.g. ?cmsframe=body.
				if (navLink[i].endsWith("/")) {
					xmlDataBlock.setData("navLink", servletPath + navLink[i] + "index.html" );
				} else {
					xmlDataBlock.setData("navLink", servletPath + navLink[i] );
				}
				// Check if nav is current nav				
				if (navLink[i].equals(currentFolder) || navLink[i].equals(requestedUri)) {
					result.append(xmlDataBlock.getProcessedDataValue("navCurrent"));
				} else {
					result.append(xmlDataBlock.getProcessedDataValue("navEntry"));
				}
				// choose only folders.
				if (navLink[i].endsWith("/")) {
					Vector all=cms.getSubFolders(navLink[i]);
					Vector files=cms.getFilesInFolder(navLink[i]);
					all.ensureCapacity(all.size() + files.size());
					Enumeration e = files.elements();
					while (e.hasMoreElements()) {
						all.addElement(e.nextElement());
					}
					result.append(buildNavTree(cms,xmlDataBlock,all,requestedUri,currentFolder,servletPath));
				}
			}
			result.append(xmlDataBlock.getProcessedDataValue("navTreeEnd"));
		}
		return result.toString();
		
	}
	
	
	/**
	 * Builds the link to folder determined by level.
	 * 
	 * @param cms A_CmsObject Object for accessing system resources.
	 * @param level The level of folder.
	 * @return String that contains the path of folder determind by level.
	 */	
	private String extractFolder(A_CmsObject cms, int level)
		throws CmsException {
					
		String currentFolder="/";		
		StringTokenizer st = new StringTokenizer(cms.getRequestContext().currentFolder().getAbsolutePath(),"/");
		int count=st.countTokens();
		
		if (level<0) {
			level=(-1)*level;
			level=count-level;
		}
		while (st.hasMoreTokens()) {
			if (level>0) {
				currentFolder=currentFolder+st.nextToken()+"/";
				level--;
			} else {
				break;
			}
		}
		return currentFolder;
	}
	
	
	/**
	 * Extracts the navbar.
	 * 
	 * @param cms A_CmsObject Object for accessing system resources.    
     * @param resources a vector that contains the elements of navigation.
     * @param navLink an array of navigation's link.
     * @param navText an array of navigation's Text.
     * @param navPos an array of position of navbar.
	 * @return The maximum number of navbars in navigation.
	 */	
	private int extractNav(A_CmsObject cms, Vector resources, String[] navLink, String[] navText, float[] navPos)
		throws CmsException {
		
		String requestedUri = cms.getRequestContext().getUri();
		
		int size = resources.size();
        int max = 0;
				
        // First scan all subfolders of the root folder
        // for any navigation metainformations and store
        // the maximum position found
        for(int i=0; i<size; i++) {
            A_CmsResource currentResource = (A_CmsResource)resources.elementAt(i);
            String path = currentResource.getAbsolutePath();
            String pos = cms.readProperty(path, C_PROPERTY_NAVPOS);
            String text = cms.readProperty(path, C_PROPERTY_NAVTEXT);
			// Only list folders in the nav bar if they are not deleted!
            if (currentResource.getState() != C_STATE_DELETED) { 
                // don't list the temporary folders in the nav bar!
                if (pos != null && text != null && (!"".equals(pos)) && (!"".equals(text))
                     && ((!currentResource.getName().startsWith(C_TEMP_PREFIX)) || path.equals(requestedUri))) {
                    navLink[max] = path;
					navText[max] = text;
                    navPos[max] = new Float(pos).floatValue();
                    max++;
                }
            }
        }	
		
		// Sort the navigation
		sortNav(max, navLink, navText, navPos);
		
		return max;
	}	
}