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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsContextMenuDialogPanel;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Component;
import com.vaadin.ui.Window;

/**
 * Dialog context for the explorer.<p>
 */
public class CmsExplorerDialogContext implements I_CmsDialogContext {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerDialogContext.class);

    /** The explorer app context. */
    private I_CmsAppUIContext m_appContext;

    /** List of selected resources. */
    private List<CmsResource> m_resources;

    /** The window used to display the dialog. */
    private Window m_window;

    /**
     * Creates a new instance.<p>
     *
     * @param appContext the app context
     * @param resources the list of selected resources
     */
    public CmsExplorerDialogContext(I_CmsAppUIContext appContext, List<CmsResource> resources) {
        m_resources = resources;
        m_appContext = appContext;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#error(java.lang.Throwable)
     */
    public void error(Throwable error) {

        if (m_window != null) {
            m_window.close();
        }
        m_window = prepareWindow();
        CmsErrorDialog err = new CmsErrorDialog(error, this);
        m_window.setContent(err);
        A_CmsUI.get().addWindow(m_window);
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#finish(java.lang.Object)
     */
    public void finish(Object result) {

        if (m_window != null) {
            m_window.close();
        }
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getCms()
     */
    public CmsObject getCms() {

        return A_CmsUI.getCmsObject();
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#getResources()
     */
    public List<CmsResource> getResources() {

        return m_resources;
    }

    /**
     * @see org.opencms.ui.I_CmsDialogContext#start(java.lang.String, com.vaadin.ui.Component)
     */
    public void start(String title, Component dialog) {

        if (dialog != null) {
            CmsContextMenuDialogPanel panel = new CmsContextMenuDialogPanel();
            panel.setContent(dialog);
            m_window = prepareWindow();
            m_window.setCaption(title);
            m_window.setContent(panel);
            A_CmsUI.get().addWindow(m_window);
            try {
                for (CmsResource resource : m_resources) {
                    CmsResourceUtil resUtil = new CmsResourceUtil(getCms(), resource);
                    Locale locale = A_CmsUI.get().getLocale();
                    panel.addResourceInfo(
                        new CmsResourceInfo(
                            resUtil.getGalleryTitle(locale),
                            resUtil.getGalleryDescription(locale),
                            resUtil.getBigIconPath()));
                }

            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }

        }
    }

    /**
     * Initializes the dialog window.<p>
     *
     * @return the window to be used by dialogs
     */
    protected Window prepareWindow() {

        Window window = new Window();
        window.setModal(true);
        window.setClosable(false);
        window.setWidth("800px");
        int height = Math.max(400, A_CmsUI.get().getPage().getBrowserWindowHeight() - 200);
        window.setHeight(height + "px");
        window.center();
        return window;

    }

}
