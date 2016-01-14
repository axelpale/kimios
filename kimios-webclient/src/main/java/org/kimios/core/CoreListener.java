/*
 * Kimios - Document Management System Software
 * Copyright (C) 2008-2014  DevLib'
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * aong with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kimios.core;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;
import org.kimios.controller.Controller;
import org.kimios.core.configuration.Config;
import org.kimios.i18n.InternationalizationManager;
import org.kimios.utils.configuration.ConfigurationManager;
import org.kimios.utils.spring.SpringWebContextLauncher;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.Properties;

/**
 * @author Fabien Alin
 */
public class CoreListener implements ServletContextListener {
    final Logger log = LoggerFactory.getLogger(CoreListener.class);

    private ContextLoader contextLoader;

    private boolean osgiMode = false;

    public void contextInitialized(ServletContextEvent event) {

        //check if osgi


        try {
            this.contextLoader = new ContextLoader();

            /*
               Check config file exist
            */
            String kimiosHome = System.getProperty("kimios.home");

            if (StringUtils.isBlank(kimiosHome)) {
                throw new Exception("Kimios Home Directory not found. Please Check App servers parameters");
            }

            String settingsPath = kimiosHome
                    + "/client/conf";
            File f = new File(settingsPath);
            // if dir, or kimios.properties file in dir doesn't exist
            if (!f.exists() || !new File(f, "kimios.properties").exists()) {
                /*
                               Load Port From environment
                */
                try {
                    String serverPort = System.getProperty("kimios.service.port");
                    String serverWebAppName = System.getenv("kimios.service.appname");
                    serverWebAppName =
                            serverWebAppName != null && serverWebAppName.length() > 0 ? serverWebAppName : "kimios";
                    serverPort = serverPort != null && serverPort.length() > 0 ? serverPort : "8080";
                    log.info("Kimios Web Client is running on " + serverPort);
                    /*
                       Generate file and, include server port
                    */
                    Properties webProperties = new Properties();
                    webProperties.setProperty(Config.DM_CHUNK_SIZE, "102400");
                    webProperties
                            .setProperty(Config.DM_SERVER_URL,
                                    "http://127.0.0.1:" + serverPort + "/" + serverWebAppName);

                    webProperties.setProperty(Config.DM_SERVICE_CONTEXT, "/services");


                    webProperties.setProperty(Config.BONITA_ENABLED, "false");

                    webProperties.setProperty(Config.DEFAULT_DOCUMENT_TYPE, "");

                    webProperties.setProperty(Config.DEFAULT_DOMAIN, "kimios");

                    webProperties.setProperty(Config.TRASH_FEATURE_ENABLED, "true");

                    String temporaryPathName = "kimios-tmp";
                    File temporaryDirectory = new File(temporaryPathName);
                    temporaryDirectory.mkdirs();
                    webProperties.setProperty(Config.DM_TMP_FILES_PATH, temporaryPathName);

                    try {
                        f.mkdirs();
                    } catch (Exception e) {
                        log.error("Error while creating Web Client Home Dir");
                    }


                    webProperties.store(new FileWriterWithEncoding(new File(f, "kimios.properties"), "UTF-8"),
                            "Kimios Settings File generated by the Kimios Deployer\n\n" +
                                    "Copyright @ Devlib' 2012-2015" +
                                    "Authors: Jérôme LUDMANN, Fabien ALIN");
                } catch (Exception e) {
                    log.error("Error while generating automatic settings", e);
                    f = null;
                }
            }

            if (f != null && f.exists()) {
                SpringWebContextLauncher.launchApp(event.getServletContext(), this.contextLoader);
                InternationalizationManager.getInstance("EN");
                CoreInitializer.contextInitialized(event.getServletContext().getRealPath("/"));
                SpringAppContextProvider appContextProvider = new SpringAppContextProvider(event.getServletContext());
                event.getServletContext().setAttribute("kimiosAppCtxBeanProvider", appContextProvider);
                Controller.init(appContextProvider);
                new FileCleaner().cleanTemporaryFiles(new File(
                        ConfigurationManager.getValue(Config.DM_TMP_FILES_PATH).toString()));
            } else {
                log.error("Kimios Web Client Setting not found. Application unavailable");
            }
        } catch (Exception e) {
            log.error("kimios Client Listener", e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        log.info("kimios Web Client Closing ...");

        SpringWebContextLauncher.shutdownApp(event.getServletContext(), this.contextLoader);
        log.info("kimios Web Client Closed");
    }
}
