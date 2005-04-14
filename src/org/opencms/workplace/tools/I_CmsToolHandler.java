/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/I_CmsToolHandler.java,v $
 * Date   : $Date: 2005/04/14 13:11:15 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.util.I_CmsNamedObject;

import java.util.List;

/**
 * Interface for an admin tool handler.<p>
 * 
 * These handlers are created and managed by the 
 * <code>{@link org.opencms.workplace.tools.CmsToolManager}</code>.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public interface I_CmsToolHandler extends I_CmsNamedObject {

    /**
     * Returns the help text.<p>
     * 
     * @return the help text
     */
    String getHelpText();

    /**
     * Returns the path to the icon.<p>
     * 
     * @return the path to the icon
     */
    String getIconPath();

    /**
     * Returns the path to an optional small(16x16) icon.<p>
     * 
     * @return the path to an optional small(16x16) icon
     */
    String getSmallIconPath();

    /**
     * Returns a list of install points where to install the tool.<p>
     * 
     * @return a list of <code>{@link CmsToolInstallPoint}</code>'s.
     */
    List getInstallPoints();

    /**
     * Returns the link to the admin tool.<p>
     * 
     * @return the link
     */
    String getLink();

    /**
     * Returns the state of the admin tool for a given cms context.<p>
     * 
     * @param cms the cms context
     * 
     * @return <code>true</code> if enabled
     */
    boolean isEnabled(CmsObject cms);
    
    /**
     * Main method that somehow setups the admin tool handler.<p>
     * 
     * @param cms the admin context (at opencms' initialization time) 
     * @param resourcePath the resource path of the file/folder to use as admin tool
     * 
     * @throws CmsException if something goes wrong
     */
    void setup(CmsObject cms, String resourcePath) throws CmsException; 
}