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

package org.opencms.ui.dialogs;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.lock.CmsLockActionRecord;
import org.opencms.lock.CmsLockUtil;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsPermalinkResourceHandler;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.I_CmsDialogContext;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsOkCancelActionHandler;
import org.opencms.ui.components.CmsResourceInfo;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Page.Styles;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedSession;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class representing a dialog for optimizing image galleries.<p>
 */
public class CmsGalleryOptimizeDialog extends CmsBasicDialog {

    /**
     * Class representing an editable gallery image.<p>
     */
    private class DataItem {

        /** The file name of this editable gallery image. */
        private String m_name;

        /** The full path of this editable gallery image. */
        private String m_path;

        /** The title of this editable gallery image. */
        private String m_title;

        /** The copyright information of this editable gallery image. */
        private String m_copyright;

        /** The description of this editable gallery image. */
        private String m_description;

        /** Date when this editable gallery image was last modified. */
        private Long m_dateLastModified;

        /** Whether this editable gallery image is used. */
        private Boolean m_isUsed;

        /** Whether this editable gallery image shall be deleted. */
        private Boolean m_deleteFlag = Boolean.valueOf(false);

        /** The CMS resource of this editable gallery image. */
        private CmsResource m_resource;

        /** The CMS resource utility of this editable gallery image. */
        private CmsResourceUtil m_resourceUtil;

        /** The main UI component of this editable gallery image. */
        private HorizontalLayout m_component;

        /** The data binder of this editable gallery image. */
        private Binder<DataItem> m_binder = new Binder<DataItem>();

        /**
         * Creates a new editable gallery image for a given CMS resource.<p>
         *
         * @param resource the CMS resource
         */
        public DataItem(CmsResource resource) {

            m_resource = resource;
            m_resourceUtil = new CmsResourceUtil(A_CmsUI.getCmsObject(), m_resource);
            List<CmsRelation> relations = null;
            try {
                relations = getCms().getRelationsForResource(resource, CmsRelationFilter.SOURCES);
                m_name = resource.getName();
                m_path = resource.getRootPath();
                m_title = getCms().readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                m_copyright = getCms().readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_COPYRIGHT,
                    false).getValue();
                m_description = getCms().readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_DESCRIPTION,
                    false).getValue();
                m_dateLastModified = Long.valueOf(resource.getDateLastModified());
                m_isUsed = Boolean.valueOf(!((relations == null) || relations.isEmpty()));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            initComponent();
        }

        /**
         * Returns the binder of this editable gallery image.<p>
         *
         * @return the binder
         */
        public Binder<DataItem> getBinder() {

            return m_binder;
        }

        /**
         * Returns the main UI component of this editable gallery image.<p>
         *
         * @return the main UI component
         */
        public HorizontalLayout getComponent() {

            return m_component;
        }

        /**
         * Returns the copyright information of this editable gallery image.<p>
         *
         * @return the copyright information
         */
        public String getCopyright() {

            return m_copyright;
        }

        /**
         * Returns the date when this editable gallery image was last modified.<p>
         *
         * @return the date
         */
        public Long getDateLastModified() {

            return m_dateLastModified;
        }

        /**
         * Returns whether this editable gallery image shall be deleted.<p>
         *
         * @return whether delete or not
         */
        public Boolean getDeleteFlag() {

            return m_deleteFlag;
        }

        /**
         * Returns the description of this editable gallery image.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_description;
        }

        /**
         * Returns whether this editable gallery image is used.<p>
         *
         * @return whether used or not
         */
        public Boolean getIsUsed() {

            return m_isUsed;
        }

        /**
         * Returns the name of this editable gallery image.<p>
         *
         * @return the name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Returns the full path of this editable gallery image.<p>
         *
         * @return the full path
         */
        public String getPath() {

            return m_path;
        }

        /**
         * Converts the form data of this editable gallery image into a list of CMS properties.<p>
         *
         * @return the CMS property list
         */
        public List<CmsProperty> getPropertyList() {

            List<CmsProperty> propertyList = new ArrayList<CmsProperty>();
            propertyList.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_TITLE, m_title, null));
            propertyList.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_COPYRIGHT, m_copyright, null));
            propertyList.add(new CmsProperty(CmsPropertyDefinition.PROPERTY_DESCRIPTION, m_description, null));
            return propertyList;
        }

        /**
         * Returns the CMS resource this editable gallery image was created from.<p>
         *
         * @return the CMS resource
         */
        public CmsResource getResource() {

            return m_resource;
        }

        /**
         * Returns the CMS resource utility class for this editable gallery image.<p>
         *
         * @return the CMS resource utility class
         */
        public CmsResourceUtil getResourceUtil() {

            return m_resourceUtil;
        }

        /**
         * Returns the title of this editable gallery image.<p>
         *
         * @return the title
         */
        public String getTitle() {

            return m_title;
        }

        /**
         * Initializes all UI components of this editable gallery image and initializes all data fields.<p>
         */
        public void initComponent() {

            m_component = new HorizontalLayout();
            m_component.setWidthFull();
            m_component.setMargin(true);
            ImageComposite imageComposite = new ImageComposite(this);
            m_component.addComponent(imageComposite);
            FormComposite formComposite = new FormComposite(this);
            m_component.addComponent(formComposite);
            m_component.setExpandRatio(formComposite, 1.0f);
            m_binder.readBean(this);
        }

        /**
         * Sets the copyright information of this editable gallery image.<p>
         *
         * @param copyright the copyright information
         */
        public void setCopyright(String copyright) {

            m_copyright = copyright;
        }

        /**
         * Sets the flag that states whether this editable gallery image shall be deleted.<p>
         *
         * @param deleteFlag the flag
         */
        public void setDeleteFlag(Boolean deleteFlag) {

            m_deleteFlag = deleteFlag;
        }

        /**
         * Sets the description of this editable gallery image.<p>
         *
         * @param description the description
         */
        public void setDescription(String description) {

            m_description = description;
        }

        /**
         * Sets the title of this editable gallery image.<p>
         *
         * @param title the title
         */
        public void setTitle(String title) {

            m_title = title;
        }
    }

    /**
     * Class representing a form composite to edit gallery image data.<p>
     */
    private class FormComposite extends VerticalLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** The data item of this form composite. */
        private DataItem m_dataItem;

        /**
         * Creates a new form composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public FormComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setHeightFull();
            setMargin(false);
            setSpacing(true);
            addStyleName("o-gallery-optimize-row");
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setMargin(false);
            verticalLayout.setSpacing(false);
            verticalLayout.setWidthFull();
            verticalLayout.setHeightUndefined();
            verticalLayout.addComponent(createResourceInfo());
            verticalLayout.addComponent(createResourceAttributes());
            FormLayout formLayout = new FormLayout();
            formLayout.setMargin(false);
            formLayout.setSpacing(false);
            formLayout.setWidthFull();
            formLayout.setHeightUndefined();
            formLayout.addComponent(createFieldTitle());
            formLayout.addComponent(createFieldCopyright());
            formLayout.addComponent(createFieldDescription());
            addComponent(verticalLayout);
            addComponent(formLayout);
            setExpandRatio(formLayout, 1.0f);
            this.setComponentAlignment(formLayout, Alignment.BOTTOM_LEFT);
        }

        /**
         * Creates the copyright form field.<p>
         *
         * @return the copyright form field.
         */
        private TextField createFieldCopyright() {

            String caption = CmsVaadinUtils.getMessageText(
                org.opencms.workplace.explorer.Messages.GUI_INPUT_COPYRIGHT_0);
            TextField field = new TextField(caption);
            field.setWidthFull();
            m_dataItem.getBinder().bind(field, DataItem::getCopyright, DataItem::setCopyright);
            return field;
        }

        /**
         * Creates the description form field.<p>
         *
         * @return the description form field.
         */
        private TextField createFieldDescription() {

            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_INPUT_DESCRIPTION_0);
            TextField field = new TextField(caption);
            field.setWidthFull();
            m_dataItem.getBinder().bind(field, DataItem::getDescription, DataItem::setDescription);
            return field;
        }

        /**
         * Creates the title form field.<p>
         *
         * @return the title form field.
         */
        private TextField createFieldTitle() {

            String caption = CmsVaadinUtils.getMessageText(org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0);
            TextField field = new TextField(caption);
            field.setWidthFull();
            m_dataItem.getBinder().bind(field, DataItem::getTitle, DataItem::setTitle);
            return field;
        }

        /**
         * Creates a label for author information.<p>
         *
         * @return the author information label.
         */
        private Label createResourceAttributes() {

            String lastModified = formatDateTime(m_dataItem.getDateLastModified().longValue());
            String lastModifiedBy = m_dataItem.getResourceUtil().getUserLastModified();
            String message = CmsVaadinUtils.getMessageText(
                Messages.GUI_GALLERY_OPTIMIZE_LASTMODIFIED_BY_2,
                lastModified,
                lastModifiedBy);
            Label label = new Label(message);
            label.addStyleNames(ValoTheme.LABEL_LIGHT, ValoTheme.LABEL_TINY);
            return label;
        }

        /**
         * Creates a resource info box for an editable gallery image.<p>
         *
         * @return the resource info box
         */
        private CmsResourceInfo createResourceInfo() {

            CmsResourceInfo resourceInfo = new CmsResourceInfo(m_dataItem.getResource());
            resourceInfo.setTopLineText(m_dataItem.getName());
            return resourceInfo;
        }

        /**
         * Utility function for date formatting.<p>
         *
         * @param date the date to format
         * @return the formatted date
         */
        private String formatDateTime(long date) {

            return CmsDateUtil.getDateTime(
                new Date(date),
                DateFormat.MEDIUM,
                OpenCms.getWorkplaceManager().getWorkplaceLocale(getCms()));
        }
    }

    /**
     * Class representing an image composite offering an image preview and a check box
     * to mark an image as deleted.<p>
     */
    private class ImageComposite extends HorizontalLayout {

        /** The default serial version UID. */
        private static final long serialVersionUID = 1L;

        /** Image scale parameters for preview images as used by the image scaler. */
        private static final String SCALE_PARAMETERS = "t:2,w:" + IMAGE_WIDTH + ",h:" + IMAGE_HEIGHT;

        /** Request query string to load a scaled preview image. */
        private static final String SCALE_QUERY_STRING = "?__scale=" + SCALE_PARAMETERS;

        /** The panel width. */
        private static final String PANEL_WIDTH = "206px";

        /** Reduced panel width. */
        private static final String PANEL_WIDTH_2 = "166px";

        /** The panel height. */
        private static final String PANEL_HEIGHT = "176px";

        /** The data item of this image composite. */
        private DataItem m_dataItem;

        /** The main panel of this image composite. */
        private AbsoluteLayout m_panel;

        /**
         * Creates a new image composite for a given data item.<p>
         *
         * @param dataItem the data item
         */
        public ImageComposite(DataItem dataItem) {

            m_dataItem = dataItem;
            setSizeUndefined();
            m_panel = new AbsoluteLayout();
            m_panel.setWidth(PANEL_WIDTH);
            m_panel.setHeight(PANEL_HEIGHT);
            m_panel.addStyleName("v-panel");
            m_panel.addComponent(createClickableVaadinImage(), "left: 2px; top: 2px;");
            addComponent(m_panel);
            CssLayout cssLayout = new CssLayout();
            cssLayout.setWidth(PANEL_WIDTH_2);
            cssLayout.setHeight(PANEL_HEIGHT);
            if (!m_dataItem.getIsUsed().booleanValue()) {
                Label labelInUse = createLabelInUseInfo();
                CheckBox fieldDeleteFlag = createFieldDeleteFlag();
                cssLayout.addComponent(labelInUse);
                cssLayout.addComponent(fieldDeleteFlag);
            }
            addComponent(cssLayout);
        }

        /**
         * Utility function to create a clickable Vaadin image.<p>
         *
         * @return the clickable Vaadin image
         */
        private Link createClickableVaadinImage() {

            CmsResource resource = m_dataItem.getResource();
            ExternalResource externalResource = new ExternalResource(getScaleUri(resource));
            Link link = new Link(null, new ExternalResource(getPermanentUri(resource)));
            link.setWidth(PANEL_WIDTH);
            link.setHeight(PANEL_HEIGHT);
            link.setIcon(externalResource);
            link.setTargetName("_blank");
            link.setStyleName("o-gallery-optimize-image-preview");
            return link;
        }

        /**
         * Creates a check box to mark an image as deleted.<p>
         *
         * @return the check box
         */
        private CheckBox createFieldDeleteFlag() {

            String caption = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_INPUT_DELETE_UNUSED_0);
            CheckBox field = new CheckBox(caption);
            field.setWidthFull();
            m_dataItem.getBinder().bind(field, DataItem::getDeleteFlag, DataItem::setDeleteFlag);
            return field;
        }

        /**
         * Creates a label informing about whether this editable gallery image is used.<p>
         *
         * @return the label
         */
        private Label createLabelInUseInfo() {

            String notInUse = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_LABEL_NOT_IN_USE_0);
            return new Label(notInUse);
        }

        /**
         * Utility function to create a permanent URI for a preview image.<p>
         *
         * @param resource the CMS resource
         * @return the permanent URI
         */
        private String getPermanentUri(CmsResource resource) {

            String structureId = resource.getStructureId().toString();
            String permalink = CmsStringUtil.joinPaths(
                OpenCms.getSystemInfo().getOpenCmsContext(),
                CmsPermalinkResourceHandler.PERMALINK_HANDLER,
                structureId);
            return permalink;
        }

        /**
         * Utility function to create a permanent URI for a scaled preview image.<p>
         *
         * @param resource the CMS resource
         * @return the scale URI
         */
        private String getScaleUri(CmsResource resource) {

            String paramTimestamp = "&timestamp=" + System.currentTimeMillis();
            return getPermanentUri(resource) + SCALE_QUERY_STRING + paramTimestamp;
        }
    }

    /**
     * Utility class to handle dialog save actions. Keeps track of the changes the
     * user has made since opening the dialog on the one hand and the changes since
     * the last save action on the other.
     */
    private class SaveHandler {

        /** Data items marked deleted since opening the dialog. */
        Set<DataItem> m_deleted = new HashSet<DataItem>();

        /** Data items marked deleted since the last save action. */
        Set<DataItem> m_deletedCurrent = new HashSet<DataItem>();

        /** Resources deleted since the last save action. */
        Set<CmsResource> m_deletedCurrentResource = new HashSet<CmsResource>();

        /** Data items modified or marked deleted since opening the dialog. */
        Set<DataItem> m_changed = new HashSet<DataItem>();

        /** Data items modified or marked deleted since the last save action. */
        Set<DataItem> m_changedCurrent = new HashSet<DataItem>();

        /** IDs of the resources modified or marked as deleted since opening the dialog. */
        Set<CmsUUID> m_changedIds = new HashSet<CmsUUID>();

        /** Resource IDs modified or marked as deleted since the last save action. */
        Set<CmsUUID> m_changedIdsCurrent = new HashSet<CmsUUID>();

        /** Whether the user has cancelled the last save action. */
        boolean m_flagCancelSave = false;

        /**
         * Creates a new save handler.<p>
         */
        public SaveHandler() {}

        /**
         * Secures and resets the current changes.
         */
        private void flush() {

            m_deleted.addAll(m_deletedCurrent);
            m_changed.addAll(m_changedCurrent);
            m_changedIds.addAll(m_changedIdsCurrent);
            m_deletedCurrent.clear();
            m_changedCurrent.clear();
            m_changedIdsCurrent.clear();
            m_deletedCurrentResource.clear();
        }

        /**
         * Returns the data items modified or marked deleted since the last save action.<p>
         *
         * @return the data items
         */
        Set<DataItem> getChangedCurrent() {

            return m_changedCurrent;
        }

        /**
         * Returns the IDs of the resources modified or marked as deleted since opening the dialog.
         *
         * @return the resource IDs
         */
        List<CmsUUID> getChangedIds() {

            return new ArrayList<CmsUUID>(m_changedIds);
        }

        /**
         * Returns the data items marked deleted since the last save action.<p>
         *
         * @return the data items
         */
        Set<DataItem> getDeletedCurrent() {

            return m_deletedCurrent;
        }

        /**
         * Returns the resources marked deleted since the last save action.<p>
         *
         * @return the resources
         */
        List<CmsResource> getDeletedCurrentResource() {

            return new ArrayList<CmsResource>(m_deletedCurrentResource);
        }

        /**
         * Whether data items have been marked as deleted since the last save action.<p>
         *
         * @return whether data items have been marked as deleted or not
         */
        boolean hasDeletedCurrent() {

            return m_deletedCurrent.size() > 0;
        }

        /**
         * Handles a save action for a given list of data items.<p>
         *
         * @param dataList the data item list
         */
        void save(List<DataItem> dataList) {

            if (!m_flagCancelSave) {
                flush();
            }
            for (DataItem dataItem : dataList) {
                boolean dataItemHasChanges = dataItem.getBinder().hasChanges();
                if (dataItemHasChanges) {
                    try {
                        dataItem.getBinder().writeBean(dataItem);
                        m_changedCurrent.add(dataItem);
                        if (dataItem.getDeleteFlag().booleanValue()) {
                            m_deletedCurrent.add(dataItem);
                            m_deletedCurrentResource.add(dataItem.getResource());
                        }
                    } catch (ValidationException e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        }

        /**
         * Marks the last save action as cancelled.<p>
         *
         * @param flagCancelSave Whether to mark as cancelled
         */
        public void setFlagCancelSave(boolean flagCancelSave) {

            m_flagCancelSave = flagCancelSave;
        }
    }

    /** Logger instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsGalleryOptimizeDialog.class);

    /** The sort order session attribute. */
    static final String GALLERY_OPTIMIZE_ATTR_SORT_ORDER = "GALLERY_OPTIMIZE_ATTR_SORT_ORDER";

    /** The default serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The width of the preview images. */
    private static final String IMAGE_WIDTH = "200";

    /** The height of the preview images. */
    private static final String IMAGE_HEIGHT = "170";

    /** The save button. */
    private Button m_buttonSave;

    /** The save and exit button. */
    private Button m_buttonSaveAndExit;

    /** The dialog context. */
    private I_CmsDialogContext m_context;

    /** The list of editable gallery images. */
    private List<DataItem> m_dataList;

    /** The UI component representing the gallery image list header view. */
    private VerticalLayout m_dataListHeaderView;

    /** The UI component representing the gallery image list view. */
    private VerticalLayout m_dataListView;

    /** The lock action record for the gallery image folder. */
    private CmsLockActionRecord m_lockActionRecord;

    /** Localized message. */
    private String m_messageSortDateLastModifiedAscending;

    /** Localized message. */
    private String m_messageSortDateLastModifiedDescending;

    /** Localized message. */
    private String m_messageSortPathAscending;

    /** Localized message. */
    private String m_messageSortPathDescending;

    /** Localized message. */
    private String m_messageSortTitleAscending;

    /** Localized message. */
    private String m_messageSortTitleDescending;

    /** Localized message. */
    private String m_messageSortUnusedFirst;

    /** The save handler. */
    private SaveHandler m_saveHandler = new SaveHandler();

    /**
     * Creates a new instance of the gallery optimize dialog.<p>
     *
     * @param context the dialog context
     */
    public CmsGalleryOptimizeDialog(I_CmsDialogContext context) {

        m_context = context;
        initMessages();
        initDialog();
        initStyles();
        initLock();
        initEvents();
        dataListLoad();
        displayDataListHeaderView();
        displayDataListViewSorted(getSessionSortOrder());
    }

    /**
     * Whether one of the editable gallery images has been modified by the user.<p>
     *
     * @return Whether has changes
     */
    private boolean dataListHasChanges() {

        boolean hasChanges = false;
        for (DataItem dataItem : m_dataList) {
            if (dataItem.getBinder().hasChanges()) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /**
     * Loads the gallery image list.<p>
     */
    private void dataListLoad() {

        m_dataList = new ArrayList<DataItem>();
        CmsResource root = m_context.getResources().get(0);
        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            m_context.getResources().get(0);
            CmsResourceFilter resourceFilter = CmsResourceFilter.IGNORE_EXPIRATION.addRequireType(
                new CmsResourceTypeImage());
            List<CmsResource> resources = cms.readResources(cms.getSitePath(root), resourceFilter);
            for (CmsResource resource : resources) {
                DataItem dataItem = new DataItem(resource);
                m_dataList.add(dataItem);
            }
        } catch (CmsException exception) {
            m_context.error(exception);
        }
    }

    /**
     * Displays the UI component representing the dialog header view.<p>
     */
    private void displayDataListHeaderView() {

        m_dataListHeaderView.setHeightUndefined();
        m_dataListHeaderView.setWidthFull();
        m_dataListHeaderView.setMargin(false);
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setHeightUndefined();
        horizontalLayout.setWidthFull();
        NativeSelect<String> selectSort = new NativeSelect<String>();
        selectSort.setEmptySelectionCaption(m_messageSortTitleAscending);
        selectSort.setItems(
            m_messageSortTitleDescending,
            m_messageSortDateLastModifiedAscending,
            m_messageSortDateLastModifiedDescending,
            m_messageSortPathAscending,
            m_messageSortPathDescending,
            m_messageSortUnusedFirst);
        selectSort.addValueChangeListener(event -> {
            if (event.isUserOriginated()) {
                displayDataListViewSorted(event.getValue());
            }
        });
        selectSort.setValue(getSessionSortOrder());
        horizontalLayout.addComponent(selectSort);
        try {
            CmsResource resource = m_context.getResources().get(0);
            List<CmsRelation> relations = getCms().getRelationsForResource(resource, CmsRelationFilter.SOURCES);
            if ((relations != null) && !relations.isEmpty()) {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setWidthFull();
                layout.addStyleNames("v-panel", "o-error-dialog");
                Label icon = new Label();
                icon.setContentMode(ContentMode.HTML);
                icon.setValue(FontOpenCms.WARNING.getHtml());
                icon.setWidthUndefined();
                icon.setStyleName("o-warning-icon");
                String galleryTitle = getCms().readPropertyObject(
                    resource,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    false).getValue();
                if (CmsStringUtil.isEmptyOrWhitespaceOnly(galleryTitle)) {
                    galleryTitle = resource.getName();
                }
                Label message = new Label(
                    CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_DIRECTLY_USED_1, galleryTitle));
                message.setWidthUndefined();
                message.setContentMode(ContentMode.HTML);
                layout.addComponent(icon);
                layout.addComponent(message);
                layout.setExpandRatio(message, 1.0f);
                layout.setComponentAlignment(message, Alignment.MIDDLE_LEFT);
                m_dataListHeaderView.addComponent(layout);
            }
        } catch (CmsException e) {
            //no-op
        }
        m_dataListHeaderView.addComponent(horizontalLayout);
    }

    /**
     * Displays the UI component representing the gallery image list view.<p>
     */
    private void displayDataListView() {

        m_dataListView.setHeightUndefined();
        m_dataListView.setWidthFull();
        m_dataListView.setMargin(false);
        m_dataListView.setSpacing(false);
        m_dataListView.removeAllComponents();
        for (int i = 0; i < m_dataList.size(); i++) {
            DataItem dataItem = m_dataList.get(i);
            dataItem.getComponent().removeStyleName("o-gallery-optimize-row-odd");
            if ((i % 2) > 0) {
                dataItem.getComponent().setStyleName("o-gallery-optimize-row-odd");
            }
            m_dataListView.addComponent(dataItem.getComponent());
        }
    }

    /**
     * Sorts the gallery image list according to a given sort order and re-renders the
     * gallery image list view.
     *
     * @param sortOrder the sort order
     */
    private void displayDataListViewSorted(String sortOrder) {

        Comparator<DataItem> titleAscending = Comparator.comparing(
            DataItem::getTitle,
            Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<DataItem> titleDescending = Comparator.comparing(
            DataItem::getTitle,
            Comparator.nullsFirst(Comparator.naturalOrder())).reversed();
        Comparator<DataItem> dateAscending = Comparator.comparing(
            DataItem::getDateLastModified,
            Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<DataItem> dateDescending = Comparator.comparing(
            DataItem::getDateLastModified,
            Comparator.nullsFirst(Comparator.naturalOrder())).reversed();
        Comparator<DataItem> pathAscending = Comparator.comparing(
            DataItem::getPath,
            Comparator.nullsLast(Comparator.naturalOrder()));
        Comparator<DataItem> pathDescending = Comparator.comparing(
            DataItem::getPath,
            Comparator.nullsFirst(Comparator.naturalOrder())).reversed();
        Comparator<DataItem> unusedFirst = Comparator.comparing(
            DataItem::getIsUsed,
            Comparator.nullsLast(Comparator.naturalOrder()));
        if ((sortOrder == null) || (sortOrder == m_messageSortTitleAscending)) {
            m_dataList.sort(titleAscending);
        } else if (sortOrder == m_messageSortTitleDescending) {
            m_dataList.sort(titleDescending);
        } else if (sortOrder == m_messageSortDateLastModifiedAscending) {
            m_dataList.sort(dateAscending);
        } else if (sortOrder == m_messageSortDateLastModifiedDescending) {
            m_dataList.sort(dateDescending);
        } else if (sortOrder == m_messageSortPathAscending) {
            m_dataList.sort(pathAscending);
        } else if (sortOrder == m_messageSortPathDescending) {
            m_dataList.sort(pathDescending);
        } else if (sortOrder == m_messageSortUnusedFirst) {
            m_dataList.sort(unusedFirst);
        } else {
            m_dataList.sort(titleAscending);
        }
        setSessionSortOrder(sortOrder);
        displayDataListView();
    }

    /**
     * Finishes this dialog.<p>
     *
     * @param changedIds list of IDs of the changed resources
     */
    void finishDialog(List<CmsUUID> changedIds) {

        m_context.finish(changedIds);
    }

    /**
     * Returns the CMS object of this dialog.<p>
     *
     * @return the CMS object
     */
    public CmsObject getCms() {

        return m_context.getCms();
    }

    /**
     * Returns the current sort order saved in the user session with lazy initialization.<p>
     *
     * @return the sort order
     */
    private String getSessionSortOrder() {

        WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
        String currentSortOrder = (String)wrappedSession.getAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER);
        if (currentSortOrder == null) {
            wrappedSession.setAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER, m_messageSortPathAscending);
        }
        return (String)wrappedSession.getAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER);
    }

    /**
     * Event handler handling the dialog attach event.
     */
    void handleDialogAttach() {

        Window window = CmsVaadinUtils.getWindow(CmsGalleryOptimizeDialog.this);
        window.removeAllCloseShortcuts(); // this is because Vaadin by default adds an ESC shortcut to every window
    }

    /**
     * Event handler that discards all data edited and closes the
     * dialog. Asks the user for confirmation beforehand.<p>
     */
    void handleDialogCancel() {

        if (dataListHasChanges()) {
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_CANCEL_TITLE_0);
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_CANCEL_0);
            CmsConfirmationDialog.show(title, message, new Runnable() {

                @Override
                public void run() {

                    finishDialog(new ArrayList<CmsUUID>());
                }
            });
        } else {
            finishDialog(new ArrayList<CmsUUID>());
        }
    }

    /**
     * Event handler that handles the dialog detach event.
     */
    void handleDialogDetach() {

        unlock();
    }

    /**
     * Event handler that saves all changes. If there are data items marked as deleted
     * the user is asked for confirmation beforehand.
     */
    void handleDialogSave() {

        m_saveHandler.save(m_dataList);
        if (m_saveHandler.hasDeletedCurrent()) {
            String title = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_DELETE_TITLE_0);
            String message = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_CONFIRM_DELETE_0);
            CmsConfirmationDialog confirmationDialog = CmsConfirmationDialog.show(title, message, new Runnable() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    saveAndDelete();
                    m_saveHandler.setFlagCancelSave(false);
                }
            }, new Runnable() {

                @SuppressWarnings("synthetic-access")
                @Override
                public void run() {

                    m_saveHandler.setFlagCancelSave(true);
                }

            });
            String caption = CmsVaadinUtils.getMessageText(org.opencms.ui.Messages.GUI_SELECTED_0);
            Panel resourceListPanel = confirmationDialog.createResourceListPanel(
                caption,
                m_saveHandler.getDeletedCurrentResource());
            confirmationDialog.addMainPanelComponent(resourceListPanel);
        } else {
            saveAndDelete();
        }
    }

    /**
     * Same as {@link #handleDialogSave()} and additionally closes the dialog.<p>
     */
    void handleDialogSaveAndExit() {

        handleDialogSave();
        finishDialog(m_saveHandler.getChangedIds());
    }

    /**
     * Initializes this dialog.<p>
     */
    private void initDialog() {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        displayResourceInfo(m_context.getResources(), "");
        Button buttonCancel = createButtonCancel();
        buttonCancel.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogCancel();
        });
        addButton(buttonCancel, false);
    }

    /**
     * Initializes the events of this dialog.
     */
    private void initEvents() {

        m_buttonSave.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogSave();
        });
        m_buttonSaveAndExit.addClickListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogSaveAndExit();
        });
        setActionHandler(new CmsOkCancelActionHandler() {

            private static final long serialVersionUID = 1L;

            @Override
            protected void cancel() {

                CmsGalleryOptimizeDialog.this.handleDialogCancel();
            }

            @Override
            protected void ok() {

                CmsGalleryOptimizeDialog.this.handleDialogSaveAndExit();
            }
        });
        addAttachListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogAttach();
        });
        addDetachListener(event -> {
            CmsGalleryOptimizeDialog.this.handleDialogDetach();
        });
    }

    /**
     * Locks the gallery folder.
     */
    private void initLock() {

        try {
            m_lockActionRecord = CmsLockUtil.ensureLock(getCms(), m_context.getResources().get(0));
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Initializes the localized messages of this dialog.
     */
    private void initMessages() {

        m_messageSortDateLastModifiedAscending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_DATE_MODIFIED_ASCENDING_0);
        m_messageSortDateLastModifiedDescending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_DATE_MODIFIED_DESCENDING_0);
        m_messageSortPathAscending = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_SORT_PATH_ASCENDING_0);
        m_messageSortPathDescending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_PATH_DESCENDING_0);
        m_messageSortTitleAscending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_TITLE_ASCENDING_0);
        m_messageSortTitleDescending = CmsVaadinUtils.getMessageText(
            Messages.GUI_GALLERY_OPTIMIZE_SORT_TITLE_DESCENDING_0);
        m_messageSortUnusedFirst = CmsVaadinUtils.getMessageText(Messages.GUI_GALLERY_OPTIMIZE_SORT_UNUSED_FIRST_0);
    }

    /**
     * Initializes some styles for this dialog.<p>
     */
    private void initStyles() {

        Styles styles = Page.getCurrent().getStyles();
        styles.add(".o-gallery-optimize-row-odd { background-color: #E0E0E2; }");
        styles.add(
            ".o-gallery-optimize-image-preview {"
                + "background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAI0AAABkCAMAAACMy0PdAAAAS1BMVEXm5ua5ubn09PTw8PDy8vLp6ens7Oz29vbu7u7IyMjNzc2+vr7ExMTBwcHKysrPz8/R0dHc3NzY2NjT09Ph4eHl5eXj4+Pe3t7V1dW2TkV8AAADjklEQVRo3u2X7XajIBBAyXZxC/ItoO//pGuSSawIo+eIrT3HO3+iglyHAVpycXFxcXFxcXFxcXGxD3t7w8iP4yYbQX4Sb0fMZCNtjkC+BdfetqAJcKzWJIPSvi30mD7uPDkEGExM5P0cAbq3oDA2HGEjPZnwMmcjCNAntlINvq4NJ1/RtxyvIYPMPhbahkNs+LJqBGPDy4YlD7FE1bcRxvqQ7k04yu+x0eWZ4j1JCHp9HcodNslp8GUqWE8yeLPqs2uFczfxZaIGUsAPuE+7w6aA7NFzFoNVt5FJKcaQHLQI3W4bXCYq2Y43Tb8pO/1+G/yFw2uR+WknKBIq2aQF7JmFhS1eFToEuKNqLymyUoZW3lqfzgwnQPtNNj0Y3MeTkAs1uUa8dGRdGzU7ztXi9G49mhxV18bOD8huUbYaO+5vrqqNeM6NS+x8m262Pm8Tq9poWEXpzHSL5AikiGvZ2EWNygB+yfe7420gE2ZZmC7dkEK2f1UbWNJsdg/SlW46Mld2VW1EZiCX/gHPptpKMQfYhDb9fyrqpTLL9PdVbVhqAzvO0CaNCosqHJobWEF+NjIv25Aj6kYku4vK7bfycBsZ04PAL4/ICNtz/mPq7zduNite5BJgMzac4NBikPa2DJiFKOH6bhfMbdaGETBe9h97UyQw2hzwdQYu1X09yXYGlA3P9I4EhSJkbUSkd6J4Xvbjb5s0gZfKTG+KgiauzeLoA/e80oRS4mYNDH3g26wNQQNBZmHQp4PLAL9fGHiu5RJBUXCbPA4eGxjBw9Dzp72QS3R9GxbhuRNvAaKT8aLIfsgOG1FAU8Bz8cBQSh4/uX29UIscfs2mKQYVJRx9NBjpDXtYEEqUUIG+6EQWOnZEAs9NEfvle7x1zvX3yaFvnMhDGzw3DYJgpWC2wYhM5Ps1OATLHENwtCnOsGMFeEPRQHPDMEws9PLIJzQ4uA2KcrnO1CpWxO+1wX08navQyBkC2WOj1tGD9ZHQu1TwhnOF0lSywemaqBWY7LL5bMrBN6Kp39auaT6xwHPDNxJ7volhPTcIhm+i7/XGhp8rEPThoDfgeqO3Qddt6kC968wan+s2H1XiSRN6h9rgb6mWG3B6QqMdugxjCxzycRgN8W4mY1e7VLHBpYIFqfiBg9hUZpy99aFGmzNBPv6eKE6Wm79n4rL5PTZ/ThTkz5m4bH6Pzb8Txcly8+9MXDZl/gM9XrODdIfTogAAAABJRU5ErkJggg==) -0px -0px  no-repeat;\n"
                + "background-position: center;\n"
                + "width: "
                + IMAGE_WIDTH
                + "px;\n"
                + "height: "
                + IMAGE_HEIGHT
                + "px;\n"
                + "}");
        styles.add(
            ".o-gallery-optimize-row .v-formlayout-contentcell, .o-gallery-optimize-row .v-formlayout-captioncell {"
                + " padding-top: 4px;\n"
                + " }");
    }

    /**
     * Persists all data changes that have not been saved yet.
     */
    private void saveAndDelete() {

        for (DataItem dataItem : m_saveHandler.getChangedCurrent()) {
            CmsResource resource = dataItem.getResource();
            try {
                getCms().writePropertyObjects(resource, dataItem.getPropertyList());
                getCms().writeResource(resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }

        }
        for (DataItem dataItem : m_saveHandler.getDeletedCurrent()) {
            CmsResource resource = dataItem.getResource();
            try {
                getCms().deleteResource(getCms().getSitePath(resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
                m_dataList.remove(dataItem);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        if (m_saveHandler.hasDeletedCurrent()) {
            displayDataListView();
        }
    }

    /**
     * Saves the selected sort order in the user session.<p>
     *
     * @param sortOrder the sort order
     */
    private void setSessionSortOrder(String sortOrder) {

        WrappedSession wrappedSession = VaadinService.getCurrentRequest().getWrappedSession();
        wrappedSession.setAttribute(GALLERY_OPTIMIZE_ATTR_SORT_ORDER, sortOrder);
    }

    /**
     * Unlocks the gallery folder.
     */
    private void unlock() {

        if (m_lockActionRecord != null) {
            CmsResource resource = m_context.getResources().get(0);
            try {
                getCms().unlockResource(resource);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

}