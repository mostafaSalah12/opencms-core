/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/Attic/CmsLeavePageDialog.java,v $
 * Date   : $Date: 2010/04/27 13:56:00 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.gwt.client.ui.CmsPopupDialog;
import org.opencms.gwt.client.ui.CmsTextButton;
import org.opencms.gwt.client.util.CmsDebugLog;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * Dialog to prevent the user from leaving the page unsaved.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsLeavePageDialog extends CmsPopupDialog {

    /** The save button. */
    CmsTextButton m_saveButton;

    /** The cancel button. */
    CmsTextButton m_cancelButton;

    /** The leave button. */
    CmsTextButton m_leaveButton;

    /**
     * Constructor. Taking the URI the user tried to open.<p>
     * 
     * @param targetUri the target URI
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    public CmsLeavePageDialog(
        final String targetUri,
        final CmsContainerpageController controller,
        final CmsContainerpageHandler handler) {

        super();
        CmsDebugLog.getInstance().printLine("Dialog created for uri: " + targetUri);
        Label content = new Label(Messages.get().key(Messages.GUI_DIALOG_PAGE_NOT_SAVED_0));
        setContent(content);
        this.setText(Messages.get().key(Messages.GUI_DIALOG_PAGE_NOT_SAVED_TITLE_0));
        this.setGlassEnabled(true);
        m_saveButton = new CmsTextButton(
            Messages.get().key(Messages.GUI_BUTTON_SAVE_TEXT_0),
            null,
            CmsTextButton.ButtonStyle.cmsButtonSmall);
        m_saveButton.useMinWidth(true);
        m_saveButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                disableButtons();
                controller.saveAndLeave(targetUri);

            }
        });
        m_cancelButton = new CmsTextButton(
            Messages.get().key(Messages.GUI_BUTTON_CANCEL_TEXT_0),
            null,
            CmsTextButton.ButtonStyle.cmsButtonSmall);
        m_cancelButton.useMinWidth(true);
        m_cancelButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                disableButtons();
                hide();
                if (handler != null) {
                    handler.deactivateCurrentButton();
                    handler.activateSelection();
                }
            }
        });
        m_leaveButton = new CmsTextButton(
            Messages.get().key(Messages.GUI_BUTTON_LEAVEPAGE_TEXT_0),
            null,
            CmsTextButton.ButtonStyle.cmsButtonSmall);
        m_leaveButton.useMinWidth(true);
        m_leaveButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                disableButtons();
                controller.leaveUnsaved(targetUri);
            }
        });
        addButton(m_leaveButton);
        addButton(m_cancelButton);
        addButton(m_saveButton);
    }

    /**
     * Disables all dialog buttons.<p>
     */
    /*DEFAULT*/void disableButtons() {

        m_saveButton.setEnabled(false);
        m_cancelButton.setEnabled(false);
        m_leaveButton.setEnabled(false);
    }

}
