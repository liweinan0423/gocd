/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.util;

import java.io.File;
import java.util.Properties;

import com.googlecode.junit.ext.JunitExtRunner;
import com.googlecode.junit.ext.RunIf;
import com.rits.cloning.Cloner;
import com.thoughtworks.go.junitext.DatabaseChecker;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
@RunWith(JunitExtRunner.class)
public class SystemEnvironmentTest {
    static final Cloner CLONER = new Cloner();
    private Properties original;

    @Before
    public void before() {
        original = CLONER.deepClone(System.getProperties());
    }

    @After
    public void after() {
        System.setProperties(original);
        new SystemEnvironment().reset(SystemEnvironment.ENABLE_CONFIG_MERGE_FEATURE);
    }

    @Test
    public void shouldDisableNewFeaturesByDefault() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.isFeatureEnabled("cruise.experimental.feature.some-feature"), is(false));
    }

    @Test
    public void shouldBeAbletoEnableAllNewFeatures() {
        Properties properties = new Properties();
        properties.setProperty(SystemEnvironment.CRUISE_EXPERIMENTAL_ENABLE_ALL, "true");
        SystemEnvironment systemEnvironment = new SystemEnvironment(properties);
        assertThat(systemEnvironment.isFeatureEnabled("cruise.experimental.feature.some-feature"), is(true));
    }

    @Test
    public void shouldFindJettyConfigInTheConfigDir() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getJettyConfigFile(), is(new File(systemEnvironment.getConfigDir(), "jetty.xml")));
    }

    @Test
    public void shouldUnderstandOperatingSystem() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getOperatingSystemName(), is(System.getProperty("os.name")));
    }


    @Test
    public void shouldUnderstandWetherToUseCompressedJs() throws Exception {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.useCompressedJs(), is(true));
        systemEnvironment.setProperty(GoConstants.USE_COMPRESSED_JAVASCRIPT, Boolean.FALSE.toString());
        assertThat(systemEnvironment.useCompressedJs(), is(false));
        systemEnvironment.setProperty(GoConstants.USE_COMPRESSED_JAVASCRIPT, Boolean.TRUE.toString());
        assertThat(systemEnvironment.useCompressedJs(), is(true));
    }

    @Test
    public void shouldHaveBaseUrl() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getBaseUrlForShine(), is("http://localhost:8153/go"));
    }

    @Test
    public void shouldHaveBaseUrlSsl() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getBaseSslUrlForShine(), is("https://localhost:8154/go"));
    }

    @Test
    public void shouldCacheAgentConnectionSystemPropertyOnFirstAccess() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty(SystemEnvironment.AGENT_CONNECTION_TIMEOUT_IN_SECONDS, "1");
        assertThat(systemEnvironment.getAgentConnectionTimeout(), is(1));
        System.setProperty(SystemEnvironment.AGENT_CONNECTION_TIMEOUT_IN_SECONDS, "2");
        assertThat(systemEnvironment.getAgentConnectionTimeout(), is(1));
    }

    @Test
    public void shouldCacheSslPortSystemPropertyOnFirstAccess() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty(SystemEnvironment.CRUISE_SERVER_SSL_PORT, "8154");
        assertThat(systemEnvironment.getSslServerPort(), is(8154));
        System.setProperty(SystemEnvironment.CRUISE_SERVER_SSL_PORT, "20000");
        assertThat(systemEnvironment.getSslServerPort(), is(8154));
    }

    @Test
    public void shouldCacheConfigDirOnFirstAccess() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getConfigDir(), is("config"));
        System.setProperty(SystemEnvironment.CONFIG_DIR_PROPERTY, "raghu");
        assertThat(systemEnvironment.getConfigDir(), is("config"));
    }

    @Test
    public void shouldCacheConfigFilePathOnFirstAccess() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.configDir(), is(new File("config")));
        System.setProperty(SystemEnvironment.CONFIG_FILE_PROPERTY, "foo");
        assertThat(systemEnvironment.getConfigDir(), is("config"));
    }


    @Test
    public void shouldCacheDatabaseDiskFullOnFirstAccess() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty(SystemEnvironment.DATABASE_FULL_SIZE_LIMIT, "100");
        assertThat(systemEnvironment.getDatabaseDiskSpaceFullLimit(), is(100L));
        System.setProperty(SystemEnvironment.DATABASE_FULL_SIZE_LIMIT, "50M");
        assertThat(systemEnvironment.getDatabaseDiskSpaceFullLimit(), is(100L));
    }

    @Test
    public void shouldCacheArtifactDiskFullOnFirstAccess() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty(SystemEnvironment.ARTIFACT_FULL_SIZE_LIMIT, "100");
        assertThat(systemEnvironment.getArtifactReposiotryFullLimit(), is(100L));
        System.setProperty(SystemEnvironment.ARTIFACT_FULL_SIZE_LIMIT, "50M");
        assertThat(systemEnvironment.getArtifactReposiotryFullLimit(), is(100L));
    }

    @Test
    public void shouldClearCachedValuesOnSettingNewProperty() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty(SystemEnvironment.ARTIFACT_FULL_SIZE_LIMIT, "100");
        assertThat(systemEnvironment.getArtifactReposiotryFullLimit(), is(100L));
        systemEnvironment.setProperty(SystemEnvironment.ARTIFACT_FULL_SIZE_LIMIT, "50");
        assertThat(systemEnvironment.getArtifactReposiotryFullLimit(), is(50L));
    }

    @Test
    public void shouldPrefixApplicationPathWithContext() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.pathFor("foo/bar"), is("/go/foo/bar"));
        assertThat(systemEnvironment.pathFor("/baz/quux"), is("/go/baz/quux"));
    }

    @Test
    public void shouldUnderstandConfigRepoDir() {
        Properties properties = new Properties();
        SystemEnvironment systemEnvironment = new SystemEnvironment(properties);
        assertThat(systemEnvironment.getConfigRepoDir(), is(new File("db/config.git")));
        properties.setProperty(SystemEnvironment.CRUISE_CONFIG_REPO_DIR, "foo/bar.git");
        assertThat(systemEnvironment.getConfigRepoDir(), is(new File("foo/bar.git")));
    }

    @Test
    public void shouldUnderstandMaterialUpdateInterval() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getMaterialUpdateIdleInterval(), is(60000L));
        systemEnvironment.setProperty(SystemEnvironment.MATERIAL_UPDATE_IDLE_INTERVAL_PROPERTY, "20");
        assertThat(systemEnvironment.getMaterialUpdateIdleInterval(), is(20L));
    }

    @Test
    public void shouldUnderstandH2CacheSize() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getCruiseDbCacheSize(), is(String.valueOf(128 * 1024)));
        System.setProperty(SystemEnvironment.CRUISE_DB_CACHE_SIZE, String.valueOf(512 * 1024));
        assertThat(systemEnvironment.getCruiseDbCacheSize(), is(String.valueOf(512 * 1024)));
    }

    @Test
    public void shouldUnderstandLazyLoadXslTransformerRegistryCacheSize() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getShineXslTransformerRegistryCacheSize(), is(20));
        systemEnvironment.setProperty(SystemEnvironment.SHINE_XSL_TRANSFORMER_REGISTRY_CACHE_SIZE, "50");
        assertThat(systemEnvironment.getShineXslTransformerRegistryCacheSize(), is(50));
    }

    @Test
    public void shouldReturnTheJobWarningLimit() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getUnresponsiveJobWarningThreshold(), is(5 * 60 * 1000L));
        System.setProperty(SystemEnvironment.UNRESPONSIVE_JOB_WARNING_THRESHOLD, "30");
        assertThat(systemEnvironment.getUnresponsiveJobWarningThreshold(), is(30 * 60 * 1000L));
    }

    @Test
    public void shouldReturnTheDefaultValueForActiveMqUseJMX() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getActivemqUseJmx(), is(false));
        System.setProperty(SystemEnvironment.ACTIVEMQ_USE_JMX, "true");
        assertThat(systemEnvironment.getActivemqUseJmx(), is(true));
    }

    @Test
    public void shouldReturnTheNumberOfDaysBelowWhichWarningPopupShouldShowForLicenseExpiry() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getLicenseExpiryWarningTime(), is(30));
        System.setProperty(SystemEnvironment.NUMBER_OF_DAYS_TO_EXPIRY_PROPERTY, "2");
        assertThat(systemEnvironment.getLicenseExpiryWarningTime(), is(2));
    }

    @Test
    public void shouldGetPluginEnabledStatusAsFalseIfNoEnvironmentVariableSet() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.pluginStatus(), is(GoConstants.ENABLE_PLUGINS_RESPONSE_FALSE));
    }

    @Test
    public void shouldGetPluginEnabledStatusAsFalseIfPropertyIsSetToN() {
        System.setProperty(GoConstants.ENABLE_PLUGINS_PROPERTY, "N");
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.pluginStatus(), is(GoConstants.ENABLE_PLUGINS_RESPONSE_FALSE));
    }

    @Test
    public void shouldGetPluginEnabledStatusAsTrueIfPropertyIsSetToY() {
        System.setProperty(GoConstants.ENABLE_PLUGINS_PROPERTY, "Y");
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.pluginStatus(), is(GoConstants.ENABLE_PLUGINS_RESPONSE_TRUE));
    }

    @Test
    public void shouldReturnTrueWhenPluginsAreEnabled() {
        System.setProperty(GoConstants.ENABLE_PLUGINS_PROPERTY, "Y");
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.isPluginsEnabled(), is(true));
    }

    @Test
    public void shouldReturnFalseWhenPluginsAreNotEnabled() {
        System.setProperty(GoConstants.ENABLE_PLUGINS_PROPERTY, "N");
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.isPluginsEnabled(), is(false));
    }

    @Test
    public void shouldReadAgentBootstrapperVersion() {
        try {
            System.setProperty(GoConstants.AGENT_LAUNCHER_VERSION, "12.2");
            SystemEnvironment systemEnvironment = new SystemEnvironment();
            assertThat(systemEnvironment.getAgentLauncherVersion(), is("12.2"));
        } finally {
            System.setProperty(GoConstants.AGENT_LAUNCHER_VERSION, "");
        }
    }

    @Test
    public void shouldDefaultAgentBootstrapperVersionToEmptyString() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getAgentLauncherVersion(), is(""));
    }

    @Test
    public void shouldResolveRevisionsForDependencyGraph_byDefault() {
        assertThat(System.getProperty(SystemEnvironment.RESOLVE_FANIN_REVISIONS), nullValue());
        assertThat(new SystemEnvironment().enforceRevisionCompatibilityWithUpstream(), is(true));
    }

    @Test
    public void should_NOT_resolveRevisionsForDependencyGraph_whenExplicitlyDisabled() {
        System.setProperty(SystemEnvironment.RESOLVE_FANIN_REVISIONS, SystemEnvironment.CONFIGURATION_NO);
        assertThat(new SystemEnvironment().enforceRevisionCompatibilityWithUpstream(), is(false));
    }

    @Test
    public void shouldResolveRevisionsForDependencyGraph_whenEnabledExplicitly() {
        System.setProperty(SystemEnvironment.RESOLVE_FANIN_REVISIONS, SystemEnvironment.CONFIGURATION_YES);
        assertThat(new SystemEnvironment().enforceRevisionCompatibilityWithUpstream(), is(true));
    }

    @Test
    public void should_cache_whetherToResolveRevisionsForDependencyGraph() {//because access to properties is synchronized
        assertThat(System.getProperty(SystemEnvironment.RESOLVE_FANIN_REVISIONS), nullValue());
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.enforceRevisionCompatibilityWithUpstream(), is(true));
        System.setProperty(SystemEnvironment.RESOLVE_FANIN_REVISIONS, SystemEnvironment.CONFIGURATION_NO);
        assertThat(systemEnvironment.enforceRevisionCompatibilityWithUpstream(), is(true));
    }

    @Test
    public void shouldTurnOnConfigMergeFeature_byDefault() {
        assertThat(System.getProperty(SystemEnvironment.ENABLE_CONFIG_MERGE_PROPERTY), nullValue());
        assertThat(new SystemEnvironment().get(SystemEnvironment.ENABLE_CONFIG_MERGE_FEATURE), is(true));
    }

    @Test
    public void should_NOT_TurnOnConfigMergeFeature_whenExplicitlyDisabled() {
        System.setProperty(SystemEnvironment.ENABLE_CONFIG_MERGE_PROPERTY, SystemEnvironment.CONFIGURATION_NO);
        assertThat(new SystemEnvironment().get(SystemEnvironment.ENABLE_CONFIG_MERGE_FEATURE), is(false));
    }

    @Test
    public void shouldTurnOnConfigMergeFeature_whenEnabledExplicitly() {
        System.setProperty(SystemEnvironment.ENABLE_CONFIG_MERGE_PROPERTY, SystemEnvironment.CONFIGURATION_YES);
        assertThat(new SystemEnvironment().get(SystemEnvironment.ENABLE_CONFIG_MERGE_FEATURE), is(true));
    }

    @Test
    public void should_cache_whetherToTurnOnConfigMergeFeature() {//because access to properties is synchronized
        assertThat(System.getProperty(SystemEnvironment.ENABLE_CONFIG_MERGE_PROPERTY), nullValue());
        assertThat(new SystemEnvironment().get(SystemEnvironment.ENABLE_CONFIG_MERGE_FEATURE), is(true));
        System.setProperty(SystemEnvironment.ENABLE_CONFIG_MERGE_PROPERTY, SystemEnvironment.CONFIGURATION_NO);
        assertThat(new SystemEnvironment().get(SystemEnvironment.ENABLE_CONFIG_MERGE_FEATURE), is(true));
    }

    @Test
    public void shouldGetTfsSocketTimeOut() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.getTfsSocketTimeout(), is(SystemEnvironment.TFS_SOCKET_TIMEOUT_IN_MILLISECONDS));
        System.setProperty(SystemEnvironment.TFS_SOCKET_TIMEOUT_PROPERTY, "100000000");
        assertThat(systemEnvironment.getTfsSocketTimeout(), is(100000000));
    }

    @Test
    public void shouldGiveINFOAsTheDefaultLevelOfAPluginWithoutALoggingLevelSet() throws Exception {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat(systemEnvironment.pluginLoggingLevel("some-plugin-1"), is(Level.INFO));
    }

    @Test
    public void shouldGiveINFOAsTheDefaultLevelOfAPluginWithAnInvalidLoggingLevelSet() throws Exception {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty("plugin.some-plugin-2.log.level", "SOME-INVALID-LOG-LEVEL");

        assertThat(systemEnvironment.pluginLoggingLevel("some-plugin-2"), is(Level.INFO));
    }

    @Test
    public void shouldGiveTheLevelOfAPluginWithALoggingLevelSet() throws Exception {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        System.setProperty("plugin.some-plugin-3.log.level", "DEBUG");
        System.setProperty("plugin.some-plugin-4.log.level", "INFO");
        System.setProperty("plugin.some-plugin-5.log.level", "WARN");
        System.setProperty("plugin.some-plugin-6.log.level", "ERROR");

        assertThat(systemEnvironment.pluginLoggingLevel("some-plugin-3"), is(Level.DEBUG));
        assertThat(systemEnvironment.pluginLoggingLevel("some-plugin-4"), is(Level.INFO));
        assertThat(systemEnvironment.pluginLoggingLevel("some-plugin-5"), is(Level.WARN));
        assertThat(systemEnvironment.pluginLoggingLevel("some-plugin-6"), is(Level.ERROR));
    }

    @Test
    @RunIf(value = DatabaseChecker.class, arguments = {DatabaseChecker.H2})
    public void shouldGetGoDatabaseProvider() {
        SystemEnvironment systemEnvironment = new SystemEnvironment();
        assertThat("default provider should be h2db", systemEnvironment.getDatabaseProvider(), is("com.thoughtworks.go.server.database.H2Database"));
        System.setProperty("go.database.provider", "foo");
        assertThat(systemEnvironment.getDatabaseProvider(), is("foo"));
    }
}
