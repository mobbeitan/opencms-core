/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ui.apps.sitemanager;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Manager class for the Site manager app.
 */

public class CmsSiteManager extends A_CmsWorkplaceApp {

    /**Bundel name for the sites which are used as templates for new sites.*/
    public static final String BUNDLE_NAME = "siteMacroBundle";

    /**Constant.*/
    public static final String FAVICON = "favicon.ico";

    /** The site icon path. */
    public static final String ICON = "apps/sitemanager/sites.png";

    /**The icon for adding a new site. */
    public static final String ICON_ADD = "apps/sitemanager/site-new.png";

    /**Icon for site folder.*/
    public static final String ICON_FOLDER = "apps/sitemanager/folder.png";

    /**Icon for the global site settings. */
    public static final String ICON_SITES_GLOBAL = "apps/sitemanager/site-global.png";

    /**Icon for the webserver configuration. */
    public static final String ICON_SITES_WEBSERVER = "apps/sitemanager/sites-webserver.png";

    /** Name of the macros folder for site templates.*/
    public static final String MACRO_FOLDER = ".macros";

    /** The add project path name. */
    public static final String PATH_NAME_ADD = "newSite";

    /** The edit project path name. */
    public static final String PATH_NAME_EDIT = "editSite";

    /**The global settings path name. */
    public static final String PATH_NAME_GLOBAL = "global";

    /**The webserver setting path name.  */
    public static final String PATH_NAME_WEBSERVER = "webserver";

    /**path attribute to transmit root of a site to be edited. */
    public static final String SITE_ROOT = "siteRoot";

    /** The site icon path. */
    public static final String TABLE_ICON = "apps/sites.png";

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsSiteManager.class.getName());

    /**Path to the sites folder.*/
    static final String PATH_SITES = "/sites/";

    /** The site table. */
    CmsSitesTable m_sitesTable;

    /** The currently opened dialog window. */
    private Window m_dialogWindow;

    /** The root cms object. */
    private CmsObject m_rootCms;

    /** The file table filter input. */
    private TextField m_siteTableFilter;

    /**
     * Method to check if a folder under given path contains a bundle for macro resolving.<p>
     *
     * @param cms CmsObject
     * @param folderPathRoot root path of folder
     * @return true if macros bundle found
     */
    public static boolean isFolderWithMacros(CmsObject cms, String folderPathRoot) {

        if (!CmsResource.isFolder(folderPathRoot)) {
            folderPathRoot = folderPathRoot.concat("/");
        }
        try {
            cms.readResource(folderPathRoot + MACRO_FOLDER);
            cms.readResource(folderPathRoot + MACRO_FOLDER + "/" + BUNDLE_NAME + "_desc");
        } catch (CmsException e) {
            return false;
        }
        return true;
    }

    /**
     * Closes the current dialog window and updates the sites table if requested.<p>
     *
     * @param updateTable <code>true</code> to update the sites table
     */
    public void closeDialogWindow(boolean updateTable) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
            m_dialogWindow = null;
        }
        if (updateTable) {
            m_sitesTable.loadSites();
        }
    }

    /**
     * Returns the fav icon path for the given site.<p>
     *
     * @param siteRoot the site root
     *
     * @return the icon path
     */
    public String getFavIconPath(String siteRoot) {

        CmsResource iconResource = null;
        try {
            iconResource = getRootCmsObject().readResource(siteRoot + "/" + CmsSiteManager.FAVICON);
        } catch (CmsException e) {
            //no favicon there
        }
        if (iconResource != null) {
            return OpenCms.getLinkManager().getPermalink(getRootCmsObject(), iconResource.getRootPath());
        }
        return OpenCmsTheme.getImageLink(CmsSiteManager.TABLE_ICON);
    }

    /**
     * Opens the delete dialog for the given sites.<p>
     *
     * @param data the site roots
     */
    public void openDeleteDialog(Set<String> data) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }
        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.narrow);
        CmsDeleteSiteDialog form = new CmsDeleteSiteDialog(this, data);
        m_dialogWindow.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_DELETE_0));
        m_dialogWindow.setContent(form);
        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
    }

    /**
     * Opens the edit site dialog.<p>
     *
     * @param siteRoot the site root of the site to edit, if <code>null</code>
     */
    public void openEditDailog(String siteRoot) {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }

        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsEditSiteForm form;
        if (siteRoot != null) {
            form = new CmsEditSiteForm(this, siteRoot);
            m_dialogWindow.setCaption(
                CmsVaadinUtils.getMessageText(
                    Messages.GUI_SITE_CONFIGURATION_EDIT_1,
                    m_sitesTable.getItem(siteRoot).getItemProperty(CmsSitesTable.PROP_TITLE).getValue()));
        } else {
            form = new CmsEditSiteForm(this);
            m_dialogWindow.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0));
        }
        m_dialogWindow.setContent(form);
        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
    }

    /**
     * Opens the global settings dialog.<p>
     */
    public void openSettingsDailog() {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }

        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsGlobalForm form = new CmsGlobalForm(this);
        m_dialogWindow.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_0));
        m_dialogWindow.setContent(form);
        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
    }

    /**
     * Opens the update server configuration dialog.<p>
     */
    public void openUpdateServerConfigDailog() {

        if (m_dialogWindow != null) {
            m_dialogWindow.close();
        }

        m_dialogWindow = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        CmsWebServerConfigForm form = new CmsWebServerConfigForm(this);
        m_dialogWindow.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_0));
        m_dialogWindow.setContent(form);
        A_CmsUI.get().addWindow(m_dialogWindow);
        m_dialogWindow.center();
    }

    /**
     * Creates the table holdings all available sites.
     * @return a vaadin table component
     */

    protected CmsSitesTable createSitesTable() {

        CmsSitesTable table = new CmsSitesTable(this);
        table.loadSites();
        return table;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<String, String>();
        crumbs.put("", CmsVaadinUtils.getMessageText(Messages.GUI_SITE_MANAGER_TITLE_SHORT_0));
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(String state) {

        addToolbarButtons();
        m_sitesTable = createSitesTable();

        m_rootLayout.setMainHeightFull(true);
        m_siteTableFilter = new TextField();
        m_siteTableFilter.setIcon(FontOpenCms.FILTER);
        m_siteTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        m_siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        m_siteTableFilter.setWidth("200px");
        m_siteTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                m_sitesTable.filterTable(event.getText());
            }
        });
        m_infoLayout.addComponent(m_siteTableFilter);

        return m_sitesTable;
    }

    /**
     * Returns the root cms object.<p>
     *
     * @return the root cms object
     */
    protected CmsObject getRootCmsObject() {

        if (m_rootCms == null) {
            try {
                m_rootCms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
                m_rootCms.getRequestContext().setSiteRoot("");
            } catch (CmsException e) {
                LOG.error("Error while cloning CmsObject", e);
            }
        }
        return m_rootCms;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }

    /**
     * Adds the toolbar buttons.<p>
     */
    private void addToolbarButtons() {

        Button add = CmsToolBar.createButton(FontOpenCms.WAND, CmsVaadinUtils.getMessageText(Messages.GUI_SITE_ADD_0));
        add.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openEditDailog(null);
            }
        });
        m_uiContext.addToolbarButton(add);

        Button settings = CmsToolBar.createButton(
            FontOpenCms.SETTINGS,
            CmsVaadinUtils.getMessageText(Messages.GUI_SITE_GLOBAL_0));
        settings.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openSettingsDailog();
            }
        });
        m_uiContext.addToolbarButton(settings);
        if (OpenCms.getSiteManager().isConfigurableWebServer()) {
            Button webServer = CmsToolBar.createButton(
                FontAwesome.SERVER,
                CmsVaadinUtils.getMessageText(Messages.GUI_SITE_WEBSERVERCONFIG_0));
            webServer.addClickListener(new ClickListener() {

                private static final long serialVersionUID = 1L;

                public void buttonClick(ClickEvent event) {

                    openUpdateServerConfigDailog();
                }
            });
            m_uiContext.addToolbarButton(webServer);
        }
    }
}
