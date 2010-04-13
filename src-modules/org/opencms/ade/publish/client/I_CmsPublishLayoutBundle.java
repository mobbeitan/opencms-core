/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/I_CmsPublishLayoutBundle.java,v $
 * Date   : $Date: 2010/04/13 09:17:18 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.publish.client;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * 
 * The layout bundle used for the publish module.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public interface I_CmsPublishLayoutBundle extends ClientBundle {

    /** The instance of the layout bundle. */
    I_CmsPublishLayoutBundle INSTANCE = GWT.create(I_CmsPublishLayoutBundle.class);

    /**
     * The accessor for the CSS constants bundle.<p>
     * 
     * @return the constants bundle
     */
    @Source("org/opencms/gwt/client/ui/css/constants.css")
    I_CmsLayoutBundle.I_CmsConstantsCss constants();

    /**
     * The accessor for the CSS bundle.<p>
     *   
     * @return a css bundle 
     */
    @Source("publish.css")
    I_CmsPublishCss publishCss();

    /**
     * The "warning" image resource.<p>
     * 
     * @return an image resource
     */
    @Source("warning.png")
    ImageResource warning();

}
