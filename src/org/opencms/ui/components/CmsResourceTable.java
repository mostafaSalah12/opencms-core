/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.components;

import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_MODIFIED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_DATE_RELEASED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_INSIDE_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_IS_FOLDER;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_NAVIGATION_TEXT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PERMISSIONS;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_PROJECT;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RELEASED_NOT_EXPIRED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RESOURCE_NAME;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_RESOURCE_TYPE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_SIZE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_STATE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_STATE_NAME;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_TITLE;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_TYPE_ICON;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_CREATED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_LOCKED;
import static org.opencms.ui.components.CmsResourceTableProperty.PROPERTY_USER_MODIFIED;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsCustomComponent;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.Table.TableDragMode;

/**
 * Generic table for displaying lists of resources.<p>
 */
public class CmsResourceTable extends A_CmsCustomComponent {

    /**
     * Helper class for easily configuring a set of columns to display, together with their visibility / collapsed status.<p>
     */
    public class ColumnBuilder {

        /** The column entries configured so far. */
        private List<ColumnEntry> m_columnEntries = Lists.newArrayList();

        /**
         * Sets up the table and its container using the columns configured so far.<p>
         */
        public void buildColumns() {

            List<CmsResourceTableProperty> visible = Lists.newArrayList();
            List<CmsResourceTableProperty> collapsed = Lists.newArrayList();
            for (ColumnEntry entry : m_columnEntries) {
                CmsResourceTableProperty prop = entry.getColumn();
                m_container.addContainerProperty(prop, prop.getColumnType(), prop.getDefaultValue());
                if (entry.isCollapsed()) {
                    collapsed.add(entry.getColumn());
                }
                if (entry.isVisible()) {
                    visible.add(entry.getColumn());
                }
            }
            m_fileTable.setVisibleColumns(visible.toArray(new Object[0]));
            setCollapsedColumns(collapsed.toArray(new Object[0]));
            for (CmsResourceTableProperty visibleProp : visible) {
                String headerKey = visibleProp.getHeaderKey();
                if (!CmsStringUtil.isEmptyOrWhitespaceOnly(headerKey)) {
                    m_fileTable.setColumnHeader(visibleProp, CmsVaadinUtils.getMessageText(headerKey));
                } else {
                    m_fileTable.setColumnHeader(visibleProp, "");
                }
                m_fileTable.setColumnCollapsible(visibleProp, visibleProp.isCollapsible());
                if (visibleProp.getColumnWidth() > 0) {
                    m_fileTable.setColumnWidth(visibleProp, visibleProp.getColumnWidth());
                }
                if (visibleProp.getExpandRatio() > 0) {
                    m_fileTable.setColumnExpandRatio(visibleProp, visibleProp.getExpandRatio());
                }
                if (visibleProp.getConverter() != null) {
                    m_fileTable.setConverter(visibleProp, visibleProp.getConverter());
                }
            }

        }

        /**
         * Adds a new column.<p>
         *
         * @param prop the column
         *
         * @return this object
         */
        public ColumnBuilder column(CmsResourceTableProperty prop) {

            column(prop, 0);
            return this;
        }

        /**
         * Adds a new column.<p<
         *
         * @param prop the column
         * @param flags the flags for the column
         *
         * @return this object
         */
        public ColumnBuilder column(CmsResourceTableProperty prop, int flags) {

            ColumnEntry entry = new ColumnEntry();
            entry.setColumn(prop);
            entry.setFlags(flags);
            m_columnEntries.add(entry);
            return this;
        }
    }

    /**
     * Contains the data for the given column, along with some flags to control visibility/collapsed status.<p>
     *
     */
    public static class ColumnEntry {

        /** The column. */
        private CmsResourceTableProperty m_column;

        /** The flags. */
        private int m_flags;

        /**
         * Returns the column.<p>
         *
         * @return the column
         */
        public CmsResourceTableProperty getColumn() {

            return m_column;
        }

        /**
         * Returns the collapsed.<p>
         *
         * @return the collapsed
         */
        public boolean isCollapsed() {

            return (m_flags & COLLAPSED) != 0;
        }

        /**
         * Returns the visible.<p>
         *
         * @return the visible
         */
        public boolean isVisible() {

            return 0 == (m_flags & INVISIBLE);
        }

        /**
         * Sets the column.<p>
         *
         * @param column the column to set
         */
        public void setColumn(CmsResourceTableProperty column) {

            m_column = column;
        }

        /**
         * Sets the flags.<p>
         *
         * @param flags the flags to set
         */
        public void setFlags(int flags) {

            m_flags = flags;
        }

    }

    /**
     * Extending the indexed container to make the number of un-filtered items available.<p>
     */
    protected static class ItemContainer extends IndexedContainer {

        /** The serial version id. */
        private static final long serialVersionUID = -2033722658471550506L;

        /**
         * Returns the number of items in the container, not considering any filters.<p>
         *
         * @return the number of items
         */
        protected int getItemCount() {

            return getAllItemIds().size();
        }
    }

    /** Flag to mark columns as initially collapsed.*/
    public static final int COLLAPSED = 1;

    /** Flag to mark columns as invisible. */
    public static final int INVISIBLE = 2;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceTable.class);

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** The resource data container. */
    protected ItemContainer m_container = new ItemContainer();

    /** The table used to display the resource data. */
    protected Table m_fileTable = new Table();

    /**
     * Creates a new instance.<p>
     *
     * This constructor does *not* set up the columns of the table; use the ColumnBuilder inner class for this.
     */
    public CmsResourceTable() {
        m_fileTable.setContainerDataSource(m_container);
        setCompositionRoot(m_fileTable);
        m_fileTable.setRowHeaderMode(RowHeaderMode.HIDDEN);
    }

    /**
     * Static helper method to initialize the 'standard' properties of a data item from a given resource.<p>
     * @param resourceItem the resource item to fill
     * @param cms the CMS context
     * @param resource the resource
     * @param messages the message bundle
     * @param locale the locale
     */
    public static void fillItemDefault(
        Item resourceItem,
        CmsObject cms,
        CmsResource resource,
        CmsMessages messages,
        Locale locale) {

        if (resource == null) {
            LOG.error("Error rendering item for 'null' resource");
            return;
        }

        if (resourceItem == null) {
            LOG.error("Error rendering 'null' item for resource " + resource.getRootPath());
            return;
        }
        if (cms == null) {
            cms = A_CmsUI.getCmsObject();
            LOG.warn("CmsObject was 'null', using thread local CmsObject");
        }
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        if (resourceItem.getItemProperty(PROPERTY_TYPE_ICON) != null) {
            resourceItem.getItemProperty(PROPERTY_TYPE_ICON).setValue(
                new CmsResourceIcon(resUtil, resUtil.getBigIconPath(), resource.getState()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_TYPE_ICON.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_PROJECT) != null) {
            Image projectFlag = null;
            switch (resUtil.getProjectState().getMode()) {
                case 1:
                    projectFlag = new Image(
                        resUtil.getLockedInProjectName(),
                        new ThemeResource(OpenCmsTheme.PROJECT_CURRENT_PATH));
                    break;
                case 2:
                    projectFlag = new Image(
                        resUtil.getLockedInProjectName(),
                        new ThemeResource(OpenCmsTheme.PROJECT_OTHER_PATH));
                    break;
                case 5:
                    projectFlag = new Image(
                        resUtil.getLockedInProjectName(),
                        new ThemeResource(OpenCmsTheme.PROJECT_PUBLISH_PATH));
                    break;
                default:
            }
            resourceItem.getItemProperty(PROPERTY_PROJECT).setValue(projectFlag);
        } else {
            LOG.error("Error redering item, property " + PROPERTY_PROJECT.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT) != null) {
            resourceItem.getItemProperty(PROPERTY_INSIDE_PROJECT).setValue(Boolean.valueOf(resUtil.isInsideProject()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_INSIDE_PROJECT.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_RELEASED_NOT_EXPIRED) != null) {
            resourceItem.getItemProperty(PROPERTY_RELEASED_NOT_EXPIRED).setValue(
                Boolean.valueOf(resUtil.isReleasedAndNotExpired()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_RELEASED_NOT_EXPIRED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME) != null) {
            resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME).setValue(resource.getName());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_RESOURCE_NAME.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_TITLE) != null) {
            resourceItem.getItemProperty(PROPERTY_TITLE).setValue(resUtil.getTitle());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_TITLE.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT) != null) {
            resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT).setValue(resUtil.getNavText());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_NAVIGATION_TEXT.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE) != null) {
            resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE).setValue(
                CmsWorkplaceMessages.getResourceTypeName(locale, type.getTypeName()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_RESOURCE_TYPE.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_IS_FOLDER) != null) {
            resourceItem.getItemProperty(PROPERTY_IS_FOLDER).setValue(Boolean.valueOf(resource.isFolder()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_IS_FOLDER.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_SIZE) != null) {
            if (resource.isFile()) {
                resourceItem.getItemProperty(PROPERTY_SIZE).setValue(Integer.valueOf(resource.getLength()));
            }
        } else {
            LOG.error("Error redering item, property " + PROPERTY_SIZE.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_PERMISSIONS) != null) {
            resourceItem.getItemProperty(PROPERTY_PERMISSIONS).setValue(resUtil.getPermissionString());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_PERMISSIONS.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED) != null) {
            resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED).setValue(Long.valueOf(resource.getDateLastModified()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_DATE_MODIFIED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_USER_MODIFIED) != null) {
            resourceItem.getItemProperty(PROPERTY_USER_MODIFIED).setValue(resUtil.getUserLastModified());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_USER_MODIFIED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_CREATED) != null) {
            resourceItem.getItemProperty(PROPERTY_DATE_CREATED).setValue(Long.valueOf(resource.getDateCreated()));
        } else {
            LOG.error("Error redering item, property " + PROPERTY_DATE_CREATED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_USER_CREATED) != null) {
            resourceItem.getItemProperty(PROPERTY_USER_CREATED).setValue(resUtil.getUserCreated());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_USER_CREATED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_RELEASED) != null) {
            long release = resource.getDateReleased();
            if (release != CmsResource.DATE_RELEASED_DEFAULT) {
                resourceItem.getItemProperty(PROPERTY_DATE_RELEASED).setValue(Long.valueOf(release));
            }
        } else {
            LOG.error("Error redering item, property " + PROPERTY_DATE_RELEASED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED) != null) {
            long expire = resource.getDateExpired();
            if (expire != CmsResource.DATE_EXPIRED_DEFAULT) {
                resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED).setValue(Long.valueOf(expire));
            }
        } else {
            LOG.error("Error redering item, property " + PROPERTY_DATE_EXPIRED.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_STATE_NAME) != null) {
            resourceItem.getItemProperty(PROPERTY_STATE_NAME).setValue(resUtil.getStateName());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_STATE_NAME.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_STATE) != null) {
            resourceItem.getItemProperty(PROPERTY_STATE).setValue(resource.getState());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_STATE.getId() + " is null");
        }

        if (resourceItem.getItemProperty(PROPERTY_USER_LOCKED) != null) {
            resourceItem.getItemProperty(PROPERTY_USER_LOCKED).setValue(resUtil.getLockedByName());
        } else {
            LOG.error("Error redering item, property " + PROPERTY_USER_LOCKED.getId() + " is null");
        }

    }

    /**
     * Gets the CSS style name for the given resource state.<p>
     *
     * @param state the resource state
     * @return the CSS style name
     */
    public static String getStateStyle(CmsResourceState state) {

        String stateStyle = "";
        if (state != null) {
            if (state.isDeleted()) {
                stateStyle = OpenCmsTheme.STATE_DELETED;
            } else if (state.isNew()) {
                stateStyle = OpenCmsTheme.STATE_NEW;
            } else if (state.isChanged()) {
                stateStyle = OpenCmsTheme.STATE_CHANGED;
            }
        }
        return stateStyle;
    }

    /**
     * Clears the value selection.<p>
     */
    public void clearSelection() {

        m_fileTable.setValue(Collections.emptySet());
    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     */
    public void fillTable(CmsObject cms, List<CmsResource> resources) {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        m_container.removeAllItems();
        m_container.removeAllContainerFilters();
        for (CmsResource resource : resources) {
            fillItem(cms, resource, wpLocale);
        }
        m_fileTable.sort();
        clearSelection();
    }

    /**
     * Gets structure ids of resources for current folder in current sort order.<p>
     *
     * @return the structure ids of the current folder contents
     */
    public List<CmsUUID> getAllIds() {

        @SuppressWarnings("unchecked")
        List<CmsUUID> ids = (List<CmsUUID>)(m_fileTable.getContainerDataSource().getItemIds());
        return Lists.newArrayList(ids);
    }

    /**
     * Returns the number of currently visible items.<p>
     *
     * @return the number of currentliy visible items
     */
    public int getItemCount() {

        return m_container.getItemCount();
    }

    /**
     * Returns if the column with the given property id is visible and not collapsed.<p>
     *
     * @param propertyId the property id
     *
     * @return <code>true</code> if the column is visible
     */
    public boolean isColumnVisible(CmsResourceTableProperty propertyId) {

        return Arrays.asList(m_fileTable.getVisibleColumns()).contains(propertyId)
            && !m_fileTable.isColumnCollapsed(propertyId);
    }

    /**
     * Selects all resources.<p>
     */
    public void selectAll() {

        m_fileTable.setValue(m_fileTable.getItemIds());
    }

    /**
     * Sets the list of collapsed columns.<p>
     *
     * @param collapsedColumns the list of collapsed columns
     */
    public void setCollapsedColumns(Object... collapsedColumns) {

        Set<Object> collapsedSet = Sets.newHashSet();
        for (Object collapsed : collapsedColumns) {
            collapsedSet.add(collapsed);
        }
        for (Object key : m_fileTable.getVisibleColumns()) {
            m_fileTable.setColumnCollapsed(key, collapsedSet.contains(key));
        }
    }

    /**
     * Sets the table drag mode.<p>
     *
     * @param dragMode the drag mode
     */
    public void setDragMode(TableDragMode dragMode) {

        m_fileTable.setDragMode(dragMode);
    }

    /**
     * Sets the table drop handler.<p>
     *
     * @param handler the drop handler
     */
    public void setDropHandler(DropHandler handler) {

        m_fileTable.setDropHandler(handler);
    }

    /**
     * Fills the file item data.<p>
     *
     * @param cms the cms context
     * @param resource the resource
     * @param locale the workplace locale
     */
    protected void fillItem(CmsObject cms, CmsResource resource, Locale locale) {

        Item resourceItem = m_container.getItem(resource.getStructureId());
        if (resourceItem == null) {
            resourceItem = m_container.addItem(resource.getStructureId());
        }
        fillItemDefault(resourceItem, cms, resource, CmsVaadinUtils.getWpMessagesForCurrentLocale(), locale);
    }

}
