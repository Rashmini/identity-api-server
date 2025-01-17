/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.api.server.application.management.v1.core.functions.application;

import com.google.common.collect.Lists;
import org.wso2.carbon.identity.api.server.application.management.v1.AppRoleConfig;
import org.wso2.carbon.identity.api.server.application.management.v1.core.functions.UpdateFunction;
import org.wso2.carbon.identity.application.common.model.AppRoleMappingConfig;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Update the Identity Provider application role configurations in Service Provider model from the API model.
 */
public class UpdateAppRoleConfigurations implements UpdateFunction<ServiceProvider, List<AppRoleConfig>> {

    @Override
    public void apply(ServiceProvider serviceProvider, List<AppRoleConfig> appRoleConfigurations) {

        if (appRoleConfigurations != null) {
            updateAppRoleConfigurations(serviceProvider, appRoleConfigurations);
        }
    }

    /**
     * Update the Identity Provider application role configurations in Service Provider model from the API model.
     *
     * @param serviceProvider       Service Provider to be updated.
     * @param appRoleConfigurations Identity Provider application role configurations from API model.
     */
    private void updateAppRoleConfigurations(ServiceProvider serviceProvider,
                                             List<AppRoleConfig> appRoleConfigurations) {

        List<String> attributeStepFIdPs = getAttributeStepFIdPs(serviceProvider);
        AppRoleMappingConfig[] appRoleMappingConfigs = getApplicationRoleMappingConfig(
                attributeStepFIdPs, appRoleConfigurations);
        serviceProvider.setApplicationRoleMappingConfig(appRoleMappingConfigs);
    }

    /**
     * Get the federated identity provider names in the attribute step.
     *
     * @param serviceProvider Service Provider to be updated.
     * @return List of federated identity provider names in the attribute step.
     */
    private List<String> getAttributeStepFIdPs(ServiceProvider serviceProvider) {

        AuthenticationStep attributeAuthStep = serviceProvider.getLocalAndOutBoundAuthenticationConfig().
                getAuthenticationStepForAttributes();
        IdentityProvider[] authStepFederatedIdentityProviders = null;
        if (attributeAuthStep == null) {
            attributeAuthStep = Lists.newArrayList(serviceProvider.getLocalAndOutBoundAuthenticationConfig().
                            getAuthenticationSteps()).stream().filter(AuthenticationStep::isAttributeStep).findFirst()
                    .orElse(null);
        }
        if (attributeAuthStep != null) {
            authStepFederatedIdentityProviders = attributeAuthStep.getFederatedIdentityProviders();
        }
        if (authStepFederatedIdentityProviders != null) {
            return Arrays.stream(authStepFederatedIdentityProviders).
                    map(IdentityProvider::getIdentityProviderName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Get the application role mapping configurations for the service provider to be updated.
     *
     * @param attributeStepFIdPs List of federated identity provider names in the attribute step.
     * @param appRoleConfigs     List of AppRoleConfig.
     * @return AppRoleMappingConfig[] array of application role mapping configurations.
     */
    private AppRoleMappingConfig[] getApplicationRoleMappingConfig(List<String> attributeStepFIdPs,
                                                                   List<AppRoleConfig> appRoleConfigs) {

        return attributeStepFIdPs.stream()
                .map(FIdPName -> {
                    AppRoleMappingConfig appRoleMappingConfig = new AppRoleMappingConfig();
                    appRoleMappingConfig.setIdPName(FIdPName);
                    appRoleConfigs.stream()
                            .filter(appRoleConfig -> FIdPName.equals(appRoleConfig.getIdp()))
                            .findFirst()
                            .ifPresent(appRoleConfig -> appRoleMappingConfig.setUseAppRoleMappings(
                                    appRoleConfig.getUseAppRoleMappings()));
                    return appRoleMappingConfig;
                }).toArray(AppRoleMappingConfig[]::new);
    }
}
