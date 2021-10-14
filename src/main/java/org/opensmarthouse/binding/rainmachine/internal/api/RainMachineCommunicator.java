/**
 * Copyright (c) 2010-2021 Contributors to the OpenSmartHouse project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.opensmarthouse.binding.rainmachine.internal.api;

import static org.eclipse.jetty.http.HttpMethod.GET;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory.Client;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * The {@link RainMachineCommunicator} handles communication with RainMachine
 * controllers so that the API is all in one place
 *
 * @author Chris Jackson - Initial contribution
 */

@NonNullByDefault
public class RainMachineCommunicator {

    private static final String API_REFERENCE = "/api/4/";

    private static final String CMD_LOGIN = "auth/login";
    private static final String CMD_APIVER = "apiVer";
    private static final String CMD_APIZONE = "zone";
    private static final String CMD_PROVISION = "provision";
    private static final String CMD_DIAGNOSTICS = "diag";

    private static final String REF_ACCESSTOKEN = "access_token";

    private static final int HTTP_TIMEOUT = 3;

    private final Logger logger = LoggerFactory.getLogger(RainMachineCommunicator.class);
    private final HttpClient httpClient;

    private final String address;
    private final String password;

    private final Gson gson = new Gson();
    private ExecutorService executor = Executors.newCachedThreadPool();

    private String accessToken = "";

    public RainMachineCommunicator(String address) throws RainMachineException {
        this(address, "");
    }

    public RainMachineCommunicator(String address, String password) throws RainMachineException {
        String localAddress;
        if (address.endsWith("/")) {
            localAddress = address.substring(0, address.length() - 1);
        } else {
            localAddress = address;
        }
        if (localAddress.contains("//")) {
            localAddress = localAddress.substring(localAddress.indexOf("//") + 2, address.length() - 1);
        }
        this.address = localAddress;
        this.password = "";// password;

        Client sslContext = new SslContextFactory.Client();
        this.httpClient = new HttpClient(sslContext);
        this.httpClient.getSslContextFactory().setTrustAll(true);
        this.httpClient.getSslContextFactory().setValidateCerts(false);
        this.httpClient.getSslContextFactory().setValidatePeerCerts(false);
        this.httpClient.getSslContextFactory().setEndpointIdentificationAlgorithm(null);
        this.httpClient.setExecutor(executor);

        try {
            // httpClient.setFollowRedirects(false);
            this.httpClient.start();
        } catch (Exception e) {
            throw new RainMachineException("Cannot start HttpClient!");
        }

        logger.debug("RainMachine communicator created for {}", this.address);
    }

    @Override
    public void finalize() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            // swallow this
        }
    }

    private synchronized boolean commandLogin() throws RainMachineException {
        if (!accessToken.isEmpty()) {
            return true;
        }

        if (password.isEmpty()) {
            return false;
        }

        try {
            Request request = httpClient.POST(URI.create("https://" + address + API_REFERENCE + CMD_LOGIN));
            logger.debug("RainMachine login request {}", request);
            request.header(HttpHeader.CONTENT_TYPE, "text/html");
            String contentString = "{\"pwd\": \"" + password + "\", \"remember\": 1}";

            request.content(new StringContentProvider(contentString, "utf-8"));
            // request.content(new StringContentProvider("{\"pwd\":\"" + password + "\", remember: 1}", "utf-8"));
            logger.debug("RainMachine login request content {}", contentString);
            logger.debug("RainMachine login request content {}", request.getContent().toString());
            ContentResponse response;
            response = request.send();
            logger.debug("RainMachine login response code {}", response.getStatus());
            if (response.getStatus() != HttpStatus.OK_200) {
                return false;
            }

            String responseString = response.getContentAsString();
            logger.debug("RainMachine login response {}", responseString);

            TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
            };
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> data = mapper.readValue(responseString, typeRef);
            accessToken = data.get(REF_ACCESSTOKEN);
            return true;
        } catch (InterruptedException | TimeoutException | ExecutionException | NullPointerException
                | JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public RainMachineApiVersion getVersions() throws RainMachineException {
        return (RainMachineApiVersion) sendGet(CMD_APIVER, RainMachineApiVersion.class);
    }

    public RainMachineZonesInformation getZones() throws RainMachineException {
        if (!commandLogin()) {
            return new RainMachineZonesInformation();
        }
        return (RainMachineZonesInformation) sendGet(CMD_APIZONE, RainMachineZonesInformation.class);
    }

    public RainMachineDeviceInformation getDeviceInfo() throws RainMachineException {
        if (!commandLogin()) {
            return new RainMachineDeviceInformation();
        }
        return (RainMachineDeviceInformation) sendGet(CMD_PROVISION, RainMachineDeviceInformation.class);
    }

    public RainMachineDiagnostics getDiagnostics() throws RainMachineException {
        if (!commandLogin()) {
            return new RainMachineDiagnostics();
        }
        return (RainMachineDiagnostics) sendGet(CMD_DIAGNOSTICS, RainMachineDiagnostics.class);
    }

    private synchronized RainMachineResponse sendGet(String command, Class<? extends RainMachineResponse> typeRef)
            throws RainMachineException {
        String url = "https://" + address + API_REFERENCE + command;
        if (!accessToken.isEmpty()) {
            url.concat("?access_token=" + accessToken);
        }
        logger.debug("RainMachine request: {}", url);
        ContentResponse response;

        try {
            response = httpClient.newRequest(url).method(GET).timeout(HTTP_TIMEOUT, TimeUnit.SECONDS).send();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                logger.warn("RainMachine return status other than HTTP_OK : {}", response.getStatus());
                throw new RainMachineException("RainMachine return status other than HTTP_OK: " + response.getStatus());
            }
        } catch (TimeoutException | ExecutionException | NullPointerException e) {
            logger.warn("Could not connect to RainMachine with exception: ", e);
            throw new RainMachineException("Could not connect to RainMachine with exception: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.warn("Connect to RainMachine interrupted: ", e);
            Thread.currentThread().interrupt();
            throw new RainMachineException("Connect to RainMachine interrupted: " + e.getMessage());
        }

        String responseString = response.getContentAsString();
        logger.debug("RainMachine response: {}", responseString);

        return gson.fromJson(responseString, typeRef);
    }
}
