/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.system.security;

import static com.exadel.frs.enums.OrganizationRole.ADMINISTRATOR;
import static com.exadel.frs.enums.OrganizationRole.OWNER;
import static com.exadel.frs.enums.OrganizationRole.USER;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.Model;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.enums.AppRole;
import com.exadel.frs.enums.OrganizationRole;
import com.exadel.frs.exception.AppDoesNotBelongToOrgException;
import com.exadel.frs.exception.InsufficientPrivilegesException;
import com.exadel.frs.exception.ModelDoesNotBelongToAppException;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationManager {

    public void verifyUserHasReadPrivileges(final Long userId, final Organization organization) {
        organization.getUserOrganizationRoleOrThrow(userId);
    }

    public void verifyUserHasWritePrivileges(final Long userId, final Organization organization) {
        if (!List.of(OWNER, ADMINISTRATOR).contains(organization.getUserOrganizationRoleOrThrow(userId).getRole())) {
            throw new InsufficientPrivilegesException();
        }
    }

    public void verifyUserHasReadPrivileges(final Long userId, final App app) {
        if (USER == getUserOrganizationRole(app.getOrganization(), userId)) {
            app.getUserAppRole(userId)
               .orElseThrow(InsufficientPrivilegesException::new);
        }
    }

    public void verifyUserHasWritePrivileges(final Long userId, final App app) {
        if (USER == getUserOrganizationRole(app.getOrganization(), userId)) {
            val role = app.getUserAppRole(userId)
                          .orElseThrow(InsufficientPrivilegesException::new)
                          .getRole();

            if (AppRole.USER == role) {
                throw new InsufficientPrivilegesException();
            }
        }
    }

    public void verifyOrganizationHasTheApp(final String orgGuid, final App app) {
        if (!app.getOrganization().getGuid().equals(orgGuid)) {
            throw new AppDoesNotBelongToOrgException(app.getGuid(), orgGuid);
        }
    }

    public void verifyAppHasTheModel(final String appGuid, final Model model) {
        if (!model.getApp().getGuid().equals(appGuid)) {
            throw new ModelDoesNotBelongToAppException(model.getGuid(), appGuid);
        }
    }

    private OrganizationRole getUserOrganizationRole(final Organization organization, final Long userId) {
        return organization.getUserOrganizationRoleOrThrow(userId).getRole();
    }

}
