/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace/resources/system/workplace/resources/components/widgets/defaultadvancedgallery.js,v $
 * Date   : $Date: 2009/12/01 13:39:14 $
 * Version: $Revision: 1.5 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
/*
 * When using this script to open the default advanced gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly.
 * 
 */

var defaultAdvancedGalleryPath;
var requestData;

// opens the default advanced gallery popup window
function openDefaultAdvancedGallery(dialogMode, fieldId, idHash) {
	requestData = {};
	//parameter from the xml configuration
    // startup param as string
	var startupFolder = eval('startupFolder' + idHash);
    // startup param as array    
    var startupFolders = eval('startupFolders' + idHash);
    
    // start type from the configuration could be 'gallery' or 'category'
	var startupType = eval('startupType' + idHash);
    var resourceTypes = eval('resourceTypes' + idHash);    
    // value of the input field
    var itemFieldvalue = null;
    if (fieldId != null && fieldId != "" && fieldId != 'null') {
        var itemField = window.document.getElementById(fieldId);
        if (itemField.value != null && itemField.value != '') {
              itemFieldvalue = itemField.value;  
        }            
    }    
    var searchKeys = {'category' : 'categories', 'gallery' : 'galleries'};            	
    
    // if input field is not empty
    if (itemFieldvalue) {
        requestData['resourcepath'] = itemFieldvalue;
        requestData['types'] = resourceTypes;
    // id input field is empty
    } else {        
        requestData = {  'querydata': {},
                         'types':     resourceTypes
                      };
        requestData['querydata']['types'] =  resourceTypes;        
        requestData['querydata']['galleries'] =  [];
        requestData['querydata']['categories'] = [];
        requestData['querydata']['matchesperpage'] = 8;
        requestData['querydata']['query'] =  '';
        requestData['querydata']['tabid'] =  'tabs-result';
        requestData['querydata']['page'] =  1;                
        // check the 
        if (startupFolder != null) {       
            requestData['querydata'][searchKeys[startupType]] =  [startupFolder];    
        } else if (startupFolders != null){        
            requestData['querydata'][searchKeys[startupType]] =  startupFolders;
        }   
        // TODO: zusätzlich types hinzufügen
    }
                            		
	var paramString = "dialogmode=" + dialogMode;
	paramString += "&fieldid=" + fieldId;    
	paramString += "&data=" + JSON.stringify(requestData);
	treewin = window.open(contextPath + defaultAdvancedGalleryPath + paramString , "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=680,height=520');
    
    //edited resource has to be provided to use custom categories
	//var editedResource = "";
	//try {
	//	editedResource = document.forms["EDITOR"].elements["resource"].value;
	//} catch (e) {};
}

// opens a preview popup window to display the currently selected download
function previewDefault(fieldId) {
	var downUri = document.getElementById(fieldId).value;
	downUri = downUri.replace(/ /, "");
	if ((downUri != "") && (downUri.charAt(0) == "/")) {
		treewin = window.open(contextPath + downUri, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=680,height=520');
	}
}