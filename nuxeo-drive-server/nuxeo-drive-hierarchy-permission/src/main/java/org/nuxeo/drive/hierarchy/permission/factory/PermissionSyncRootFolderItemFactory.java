/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Antoine Taillefer <ataillefer@nuxeo.com>
 */
package org.nuxeo.drive.hierarchy.permission.factory;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.drive.adapter.FileSystemItem;
import org.nuxeo.drive.adapter.FolderItem;
import org.nuxeo.drive.adapter.impl.DefaultSyncRootFolderItem;
import org.nuxeo.drive.service.FileSystemItemFactory;
import org.nuxeo.drive.service.impl.DefaultSyncRootFolderItemFactory;
import org.nuxeo.drive.service.impl.FileSystemItemFactoryDescriptor;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

/**
 * Permission based implementation of {@link FileSystemItemFactory} for a
 * synchronization root {@link FolderItem}.
 *
 * @author Antoine Taillefer
 */
public class PermissionSyncRootFolderItemFactory extends
        DefaultSyncRootFolderItemFactory {

    protected static final String REQUIRED_PERMISSION_PARAM = "requiredPermission";

    // Required permission to include a folder as a synchronization root,
    // default is ReadWrite
    protected String requiredPermission = SecurityConstants.READ_WRITE;

    /**
     * Prevent from instantiating class as it should only be done by
     * {@link FileSystemItemFactoryDescriptor#getFactory()}.
     */
    protected PermissionSyncRootFolderItemFactory() {
    }

    @Override
    public void handleParameters(Map<String, String> parameters) {
        String requiredPermissionParam = parameters.get(REQUIRED_PERMISSION_PARAM);
        if (!StringUtils.isEmpty(requiredPermissionParam)) {
            requiredPermission = requiredPermissionParam;
        }
    }

    /**
     * The permission synchronization root factory considers that a
     * {@link DocumentModel} is adaptable as a {@link FileSystemItem} if:
     * <ul>
     * <li>It is Folderish</li>
     * <li>AND it is not a version nor a proxy</li>
     * <li>AND it is not HiddenInNavigation</li>
     * <li>AND it is not in the "deleted" life cycle state, unless
     * {@code includeDeleted} is true</li>
     * <li>AND it is a synchronization root registered for the current user</li>
     * <li>AND the current user has the {@link #getRequiredPermission()}
     * permission on the document</li>
     * </ul>
     */
    @Override
    public boolean isFileSystemItem(DocumentModel doc, boolean includeDeleted)
            throws ClientException {
        // Check required permission
        boolean hasRequiredPermission = doc.getCoreSession().hasPermission(
                doc.getRef(), requiredPermission);
        return super.isFileSystemItem(doc, includeDeleted)
                && hasRequiredPermission;
    }

    @Override
    protected FileSystemItem adaptDocument(DocumentModel doc,
            boolean forceParentId, String parentId) throws ClientException {
        return new DefaultSyncRootFolderItem(name, parentId, doc);
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

}
