/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/upload/Attic/CmsUploadBean.java,v $
 * Date   : $Date: 2011/03/03 18:01:42 $
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

package org.opencms.ade.upload;

import org.opencms.ade.upload.shared.I_CmsUploadConstants;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.CmsImportFolder;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.gwt.client.util.CmsCollectionUtil;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspBean;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * Bean to be used in JSP scriptlet code that provides 
 * access to the upload functionality.<p>
 * 
 * @author  Ruediger Kurz 
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0 
 */
public class CmsUploadBean extends CmsJspBean {

    /** The default delay for slow uploads. */
    public static final int DEFAULT_SLOW_DELAY_MILLIS = 0;

    /** The default upload timeout. */
    public static final int DEFAULT_UPLOAD_TIMEOUT = 20000;

    /** Key name for the session attribute that stores the id of the current listener. */
    public static final String SESSION_ATTRIBUTE_LISTENER_ID = "__CmsUploadBean.LISTENER";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUploadBean.class);

    /** A static map of all listeners. */
    private static Map<CmsUUID, CmsUploadListener> m_listeners = new HashMap<CmsUUID, CmsUploadListener>();

    /** The gwt message bundle. */
    private CmsMessages m_bundle = org.opencms.ade.upload.Messages.get().getBundle();

    /** Signals that the start method is called. */
    private boolean m_called;

    /** A list of the file items to upload. */
    private List<FileItem> m_multiPartFileItems;

    /** The map of parameters read from the current request. */
    private Map<String, String[]> m_parameterMap;

    /** The names of the resources that have been created successfully. */
    private List<String> m_resourcesCreated = new ArrayList<String>();

    /**
     * Constructor, with parameters.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsUploadBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super();
        init(context, req, res);
    }

    /**
     * Returns the listener for given CmsUUID.<p>
     * 
     * @param listenerId the uuid
     * 
     * @return the according listener
     */
    public static CmsUploadListener getCurrentListener(CmsUUID listenerId) {

        return m_listeners.get(listenerId);
    }

    /**
     * Returns the VFS path for the given filename and folder.<p>
     * 
     * @param cms the cms object
     * @param fileName the filename to combine with the folder
     * @param folder the folder to combine with the filename
     * 
     * @return the VFS path for the given filename and folder
     */
    public static String getNewResourceName(CmsObject cms, String fileName, String folder) {

        String newResname = CmsResource.getName(fileName.replace('\\', '/'));
        newResname = cms.getRequestContext().getFileTranslator().translateResource(newResname);
        newResname = folder + newResname;
        return newResname;
    }

    /**
     * Starts the upload.<p>
     * 
     * @return the response String (JSON)
     */
    public String start() {

        // ensure that this method can only be called once
        if (m_called) {
            throw new UnsupportedOperationException();
        }
        m_called = true;

        // create a upload listener
        CmsUploadListener listener = createListener();
        try {
            // try to parse the request
            parseRequest(listener);
            // try to create the resources on the VFS
            createResources();
        } catch (CmsException e) {
            // an error occurred while creating the resources on the VFS, create a special error message
            LOG.error(e.getMessage(), e);
            return generateResponse(Boolean.FALSE, getCreationErrorMessage(), formatStackTrace(e));
        } catch (CmsUploadException e) {
            // an expected error occurred while parsing the request, the error message is already set in the exception
            LOG.debug(e.getMessage(), e);
            return generateResponse(Boolean.FALSE, e.getMessage(), formatStackTrace(e));
        } catch (Exception e) {
            // an unexpected error occurred while parsing the request, create a non-specific error message 
            LOG.error(e.getMessage(), e);
            String message = m_bundle.key(org.opencms.ade.upload.Messages.ERR_UPLOAD_UNEXPECTED_0);
            return generateResponse(Boolean.FALSE, message, formatStackTrace(e));
        } finally {
            removeListener(listener.getId());
        }
        // the upload was successful inform the user about success
        return generateResponse(Boolean.TRUE, m_bundle.key(org.opencms.ade.upload.Messages.INFO_UPLOAD_SUCCESS_0), "");
    }

    /**
     * Creates a upload listener and puts it into the static map.<p>
     * 
     * @return the listener
     */
    private CmsUploadListener createListener() {

        CmsUploadListener listener = new CmsUploadListener(getRequest().getContentLength());
        m_listeners.put(listener.getId(), listener);
        getRequest().getSession().setAttribute(SESSION_ATTRIBUTE_LISTENER_ID, listener.getId());
        return listener;
    }

    /**
     * Creates the resources.<p>
     * 
     * @throws UnsupportedEncodingException 
     */
    private void createResources() throws CmsException, UnsupportedEncodingException {

        // get the target folder
        String targetFolder = getTargetFolder();

        boolean isEncoded = isFileNameEncoded();
        List<String> filesToUnzip = getFilesToUnzip(isEncoded);

        // iterate over the list of files to upload and create each single resource
        for (FileItem fi : m_multiPartFileItems) {
            if ((fi != null) && (!fi.isFormField())) {

                // read the content of the file
                byte[] content = fi.get();
                fi.delete();

                // determine the new resource name
                String fileName = fi.getName();
                if (isEncoded) {
                    fileName = URLDecoder.decode(fi.getName(), "UTF-8");
                }
                if (filesToUnzip.contains(CmsResource.getName(fileName.replace('\\', '/')))) {
                    new CmsImportFolder(content, targetFolder, getCmsObject(), false);
                } else {
                    // create the resource
                    String newResname = getNewResourceName(getCmsObject(), fileName, targetFolder);
                    createSingleResource(newResname, content);
                    // add the name of the created resource to the list of successful created resources
                    m_resourcesCreated.add(newResname);
                }
            }
        }
    }

    /**
     * Creates a single resource.<p>
     * 
     * @param newResname the name of the resource to create
     * @param content the content of the resource to create
     * 
     * @throws CmsException if something goes wrong
     * @throws CmsLoaderException if something goes wrong
     * @throws CmsDbSqlException if something goes wrong
     */
    private void createSingleResource(String newResname, byte[] content)
    throws CmsException, CmsLoaderException, CmsDbSqlException {

        int resTypeId = OpenCms.getResourceManager().getDefaultTypeForName(newResname).getTypeId();
        int plainId = OpenCms.getResourceManager().getResourceType(CmsResourceTypePlain.getStaticTypeName()).getTypeId();

        // determine Title property value to set on new resource
        String title = CmsResource.getName(newResname);
        if (title.lastIndexOf('.') != -1) {
            title = title.substring(0, title.lastIndexOf('.'));
        }
        List<CmsProperty> properties = new ArrayList<CmsProperty>(1);
        CmsProperty titleProp = new CmsProperty();
        titleProp.setName(CmsPropertyDefinition.PROPERTY_TITLE);
        if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
            titleProp.setStructureValue(title);
        } else {
            titleProp.setResourceValue(title);
        }
        properties.add(titleProp);

        if (!getCmsObject().existsResource(newResname, CmsResourceFilter.IGNORE_EXPIRATION)) {
            // if the resource does not exist, create it
            try {
                // create the resource
                getCmsObject().createResource(newResname, resTypeId, content, properties);
            } catch (CmsSecurityException e) {
                // in case of not enough permissions, try to create a plain text file
                getCmsObject().createResource(newResname, plainId, content, properties);
            } catch (CmsDbSqlException sqlExc) {
                // SQL error, probably the file is too large for the database settings, delete file
                getCmsObject().lockResource(newResname);
                getCmsObject().deleteResource(newResname, CmsResource.DELETE_PRESERVE_SIBLINGS);
                throw sqlExc;
            }
        } else {
            // if the resource already exists, replace it
            CmsResource res = getCmsObject().readResource(newResname, CmsResourceFilter.ALL);
            if (!getCmsObject().getLock(res).isOwnedBy(getCmsObject().getRequestContext().getCurrentUser())) {
                getCmsObject().lockResource(res);
            }
            CmsFile file = getCmsObject().readFile(res);
            byte[] contents = file.getContents();
            try {
                getCmsObject().replaceResource(newResname, resTypeId, content, null);
            } catch (CmsSecurityException e) {
                // in case of not enough permissions, try to create a plain text file
                getCmsObject().replaceResource(newResname, plainId, content, null);
            } catch (CmsDbSqlException sqlExc) {
                // SQL error, probably the file is too large for the database settings, restore content
                file.setContents(contents);
                getCmsObject().writeFile(file);
                throw sqlExc;
            }
        }
    }

    /**
     * Returns the stacktrace of the given exception as String.<p>
     * 
     * @param e the exception
     * 
     * @return the stacktrace as String
     */
    private String formatStackTrace(Exception e) {

        StringBuffer result = new StringBuffer(64);
        for (String s : new ThrowableInformation(e).getThrowableStrRep()) {
            result.append(s + "<br />\n");
        }
        return result.toString();
    }

    /**
     * Generates a JSON object and returns its String representation for the response.<p>
     * 
     * @return the the response String
     */
    private String generateResponse(Boolean success, String message, String stacktrace) {

        JSONObject result = new JSONObject();
        try {
            result.put(I_CmsUploadConstants.KEY_SUCCESS, success);
            result.put(I_CmsUploadConstants.KEY_MESSAGE, message);
            result.put(I_CmsUploadConstants.KEY_STACKTRACE, stacktrace);
            result.put(I_CmsUploadConstants.KEY_REQUEST_SIZE, getRequest().getContentLength());
        } catch (JSONException e) {
            LOG.error(m_bundle.key(org.opencms.ade.upload.Messages.ERR_UPLOAD_JSON_0), e);
        }
        return result.toString();
    }

    /**
     * Returns the error message if an error occurred during the creation of resources in the VFS.<p>
     * 
     * @return the error message
     */
    private String getCreationErrorMessage() {

        String message = new String();
        if (!m_resourcesCreated.isEmpty()) {
            // some resources have been created, tell the user which resources were created successfully
            StringBuffer buf = new StringBuffer(64);
            for (String name : m_resourcesCreated) {
                buf.append("<br />");
                buf.append(name);
                buf.append("<br />");
            }
            message = m_bundle.key(org.opencms.ade.upload.Messages.ERR_UPLOAD_CREATING_1, buf.toString());
        } else {
            // no resources have been created on the VFS
            message = m_bundle.key(org.opencms.ade.upload.Messages.ERR_UPLOAD_CREATING_0);
        }
        return message;
    }

    /**
     * Gets the list of file names that should be unziped.<p>
     * 
     * @return the list of file names that should be unziped
     * 
     * @throws UnsupportedEncodingException if something goes wrong
     */
    private List<String> getFilesToUnzip(boolean isEncoded) throws UnsupportedEncodingException {

        if (m_parameterMap.get(I_CmsUploadConstants.UPLOAD_UNZIP_FILES_FIELD_NAME) != null) {
            String[] filesToUnzip = m_parameterMap.get(I_CmsUploadConstants.UPLOAD_UNZIP_FILES_FIELD_NAME);
            if (filesToUnzip != null) {
                List<String> result = new ArrayList<String>();
                for (String filename : filesToUnzip) {
                    if (isEncoded) {
                        result.add(URLDecoder.decode(filename, "UTF-8"));
                    } else {
                        result.add(filename);
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the target folder for the new resource, 
     * if the given folder does not exist root folder
     * of the current site is returned.<p>
     * 
     * @return the target folder for the new resource
     * 
     * @throws CmsException if something goes wrong
     */
    private String getTargetFolder() throws CmsException {

        // get the target folder on the vfs
        CmsResource target = getCmsObject().readResource("/", CmsResourceFilter.IGNORE_EXPIRATION);
        if (m_parameterMap.get(I_CmsUploadConstants.UPLOAD_TARGET_FOLDER_FIELD_NAME) != null) {
            String targetFolder = m_parameterMap.get(I_CmsUploadConstants.UPLOAD_TARGET_FOLDER_FIELD_NAME)[0];
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(targetFolder)) {
                if (getCmsObject().existsResource(targetFolder)) {
                    CmsResource tmpTarget = getCmsObject().readResource(
                        targetFolder,
                        CmsResourceFilter.IGNORE_EXPIRATION);
                    if (tmpTarget.isFolder()) {
                        target = tmpTarget;
                    }
                }
            }
        }
        String targetFolder = getCmsObject().getRequestContext().removeSiteRoot(target.getRootPath());
        if (!targetFolder.endsWith("/")) {
            // add folder separator to currentFolder
            targetFolder += "/";
        }
        return targetFolder;
    }

    /**
     * Gets the encoding flag from the request parameters and returns <code>true</code>
     * if the value of the according field is set to <code>true</code>.<p> 
     * 
     * @return <code>true</code> if the flag is set to true
     */
    private boolean isFileNameEncoded() {

        if (m_parameterMap.get(I_CmsUploadConstants.UPLOAD_FILE_NAME_URL_ENCODED_FLAG) != null) {
            String flag = m_parameterMap.get(I_CmsUploadConstants.UPLOAD_FILE_NAME_URL_ENCODED_FLAG)[0];
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(flag)) {
                if (flag.equalsIgnoreCase(Boolean.TRUE.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Parses the request.<p>
     * 
     * Stores the file items and the request parameters in a local variable if present.<p>
     */
    private void parseRequest(CmsUploadListener listener) throws Exception {

        // check if the request is a multipart request
        if (!ServletFileUpload.isMultipartContent(getRequest())) {
            // no multipart request: Abort the upload
            throw new CmsUploadException(m_bundle.key(org.opencms.ade.upload.Messages.ERR_UPLOAD_NO_MULTIPART_0));
        }

        // this was indeed a multipart form request, read the files
        m_multiPartFileItems = readMultipartFileItems(listener);

        // check if there were any multipart file items in the request
        if (CmsCollectionUtil.isEmptyOrNull(m_multiPartFileItems)) {
            // no file items found stop process
            throw new CmsUploadException(m_bundle.key(org.opencms.ade.upload.Messages.ERR_UPLOAD_NO_FILEITEMS_0));
        }

        // there are file items in the request, get the request parameters
        m_parameterMap = CmsRequestUtil.readParameterMapFromMultiPart(
            getCmsObject().getRequestContext().getEncoding(),
            m_multiPartFileItems);
    }

    /**
     * Parses a request of the form <code>multipart/form-data</code>.<p>
     * 
     * The result list will contain items of type <code>{@link FileItem}</code>.
     * If the request has no file items, then <code>null</code> is returned.<p>
     * 
     * @param listener the upload listener
     * 
     * @return the list of <code>{@link FileItem}</code> extracted from the multipart request,
     *      or <code>null</code> if the request has no file items
     */
    private List<FileItem> readMultipartFileItems(CmsUploadListener listener) throws Exception {

        DiskFileItemFactory factory = new DiskFileItemFactory();
        // maximum size that will be stored in memory
        factory.setSizeThreshold(4096);
        // the location for saving data that is larger than the threshold
        factory.setRepository(new File(OpenCms.getSystemInfo().getPackagesRfsPath()));

        // create a file upload servlet
        ServletFileUpload fu = new ServletFileUpload(factory);
        // set the listener
        fu.setProgressListener(listener);
        // set encoding to correctly handle special chars (e.g. in filenames)
        fu.setHeaderEncoding(getRequest().getCharacterEncoding());
        // set the maximum size for a single file (value is in bytes)
        long maxFileSizeBytes = OpenCms.getWorkplaceManager().getFileBytesMaxUploadSize(getCmsObject());
        if (maxFileSizeBytes > 0) {
            fu.setFileSizeMax(maxFileSizeBytes);
        }

        // try to parse the request
        try {
            return CmsCollectionsGenericWrapper.list(fu.parseRequest(getRequest()));
        } catch (SizeLimitExceededException e) {
            // request size is larger than maximum allowed request size, throw an error
            Integer actualSize = new Integer((int)(e.getActualSize() / 1024));
            Integer maxSize = new Integer((int)(e.getPermittedSize() / 1024));
            throw new CmsUploadException(m_bundle.key(
                org.opencms.ade.upload.Messages.ERR_UPLOAD_REQUEST_SIZE_LIMIT_2,
                actualSize,
                maxSize), e);
        } catch (FileSizeLimitExceededException e) {
            // file size is larger than maximum allowed file size, throw an error
            Integer actualSize = new Integer((int)(e.getActualSize() / 1024));
            Integer maxSize = new Integer((int)(e.getPermittedSize() / 1024));
            throw new CmsUploadException(m_bundle.key(
                org.opencms.ade.upload.Messages.ERR_UPLOAD_FILE_SIZE_LIMIT_3,
                actualSize,
                e.getFileName(),
                maxSize), e);
        }
    }

    /**
     * Remove the listener active in this session.
     * 
     * @param listenerId the id of the listener to remove
     */
    private void removeListener(CmsUUID listenerId) {

        getRequest().getSession().removeAttribute(SESSION_ATTRIBUTE_LISTENER_ID);
        m_listeners.remove(listenerId);
    }
}
