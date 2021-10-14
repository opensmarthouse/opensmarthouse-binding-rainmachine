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
package org.opensmarthouse.binding.rainmachine.internal.discovery;

import java.net.Inet4Address;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineAddressCache;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineBindingConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RainMachineDiscoveryParticipant} class discovers RainMachine Device(s) and places them in the inbox.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
@Component(service = { MDNSDiscoveryParticipant.class,
        RainMachineAddressCache.class }, configurationPid = "discovery.rainmachine")
public class RainMachineDiscoveryParticipant implements MDNSDiscoveryParticipant, RainMachineAddressCache {
    private static final String RAINMACHINE_MDNS_ID = "rainmachine";

    private static final String DISCOVERY_ID = "id";

    private static final String CONFIG_ID = "id";
    private static final String CONFIG_ADDRESS = "id";

    private final Logger logger = LoggerFactory.getLogger(RainMachineDiscoveryParticipant.class);

    private final Map<String, @Nullable String> lastKnownHostAddresses = new ConcurrentHashMap<>();

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(RainMachineBindingConstants.RAINMACHINE_BRIDGE);
    }

    @Override
    public String getServiceType() {
        logger.debug("RainMachine mDNS getServiceType was called");

        return "_hap._tcp.local";
    }

    @Override
    public @Nullable DiscoveryResult createResult(final ServiceInfo info) {
        final String id = info.getName();

        logger.debug("RainMachine mDNS id found '{}' with type '{}'", id, info.getType());

        if (!id.contains(RAINMACHINE_MDNS_ID)) {
            return null;
        }

        if (info.getInet4Addresses().length == 0 || info.getInet4Addresses()[0] == null) {
            return null;
        }

        final ThingUID uid = getThingUID(info);

        if (uid == null) {
            return null;
        }

        final Inet4Address hostname = info.getInet4Addresses()[0];
        final String serialNumber = info.getPropertyString(DISCOVERY_ID);

        if (serialNumber == null) {
            logger.debug("No serial number found in data for discovered RainMachine {}: {}", id, info);
            return null;
        }
        final String hostAddress = hostname == null ? "" : hostname.getHostAddress();

        lastKnownHostAddresses.put(serialNumber, hostAddress);
        final Map<String, Object> properties = new HashMap<>(3);

        properties.put(CONFIG_ID, serialNumber);
        properties.put(CONFIG_ADDRESS, hostAddress);
        return DiscoveryResultBuilder.create(uid).withProperties(properties).withRepresentationProperty(CONFIG_ID)
                .withLabel("RainMachine " + serialNumber).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(final ServiceInfo info) {
        final String name = info.getName();

        if (!name.contains(RAINMACHINE_MDNS_ID)) {
            logger.trace("Found other type of device that is not recognized as a RainMachine: {}", name);
            return null;
        }
        if (info.getInet4Addresses().length == 0 || info.getInet4Addresses()[0] == null) {
            logger.debug("Found a RainMachine, but no ip address is given: {}", info);
            return null;
        }
        logger.debug("ServiceInfo addr: {}", info.getInet4Addresses()[0]);
        if (getServiceType().equals(info.getType())) {
            final String serialNumber = info.getPropertyString(DISCOVERY_ID);

            logger.debug("Discovered an Envoy with serial number '{}'", serialNumber);
            return new ThingUID(RainMachineBindingConstants.RAINMACHINE_BRIDGE, serialNumber);
        }
        return null;
    }

    @Override
    public String getLastKnownHostAddress(final String serialNumber) {
        final String hostAddress = lastKnownHostAddresses.get(serialNumber);

        return hostAddress == null ? "" : hostAddress;
    }

}
