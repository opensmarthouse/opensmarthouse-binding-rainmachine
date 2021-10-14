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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineBindingConstants;
import org.opensmarthouse.binding.rainmachine.internal.RainMachineException;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineApiVersion;
import org.opensmarthouse.binding.rainmachine.internal.api.RainMachineCommunicator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RainMachineDiscoveryService} class discovers RainMachine Device(s) and places them in the inbox.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.rainmachine")
public class RainMachineDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(RainMachineDiscoveryService.class);

    private static final int TIMEOUT = 15;
    private static final int BROADCAST_TIMEOUT = 80;

    private static final int BROADCAST_DISCOVERY_PORT_TX = 15800;
    private static final int BROADCAST_DISCOVERY_PORT_RX = 15900;

    private static final String BROADCAST_DISCOVERY_MESSAGE = "OpenSmartHouse Discovery";

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    public RainMachineDiscoveryService() {
        super(RainMachineBindingConstants.SUPPORTED_THING_TYPES_UIDS, TIMEOUT, true);
        startScan();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return RainMachineBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startScan() {
        logger.debug("RainMachine discovery starting scan");

        for (RainMachineDiscoveryData discoveryData : updBroadcast()) {
            if (discoveryData.isValid()) {
                int zones = 16;
                String model = "";

                try {
                    RainMachineCommunicator communicator = new RainMachineCommunicator(discoveryData.getAddress());

                    RainMachineApiVersion version = communicator.getVersions();

                    switch (Integer.parseInt(version.hwVer)) {
                        case 1:
                            model = "Touch";
                            zones = 8;
                            break;
                        case 2:
                            model = "Mini-8";
                            zones = 8;
                            break;
                        case 3:
                            model = "HD-12/16";
                            break;
                        case 5:
                            model = "Pro-8/16";
                            break;
                        default:
                            break;
                    }
                } catch (RainMachineException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                ThingUID bridgeUid = new ThingUID(RainMachineBindingConstants.RAINMACHINE_BRIDGE,
                        discoveryData.getMac().replaceAll("[^A-Za-z0-9\\-_]", "").toLowerCase());
                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(bridgeUid)
                        .withLabel("RainMachine " + model + ": " + discoveryData.getName())
                        .withProperty("host", discoveryData.getAddress()).withProperty("mac", discoveryData.getMac())
                        .withProperty("name", discoveryData.getName()).withRepresentationProperty("mac").build();
                thingDiscovered(discoveryResult);

                for (int zone = 1; zone <= zones; zone++) {
                    ThingUID zoneUid = new ThingUID(RainMachineBindingConstants.RAINMACHINE_ZONE, bridgeUid,
                            "zone" + zone);
                    discoveryResult = DiscoveryResultBuilder.create(zoneUid).withBridge(bridgeUid)
                            .withLabel("RainMachine " + model + ": " + discoveryData.getName() + " (Zone " + zone + ")")
                            .withProperty("uid", zone + 1).withRepresentationProperty("uid").build();
                    thingDiscovered(discoveryResult);
                }
            } else {
                logger.debug("RainMachine scan received no responses");
            }
        }
    }

    private List<RainMachineDiscoveryData> updBroadcast() {
        List<RainMachineDiscoveryData> rList = new LinkedList<>();

        // Find the server using UDP broadcast

        try (DatagramSocket c = new DatagramSocket(BROADCAST_DISCOVERY_PORT_RX)) {
            c.setSoTimeout(BROADCAST_TIMEOUT);
            c.setBroadcast(true);

            byte[] sendData = BROADCAST_DISCOVERY_MESSAGE.getBytes("UTF-8");

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName("255.255.255.255"), BROADCAST_DISCOVERY_PORT_TX);
            c.send(sendPacket);

            while (true) {
                // Wait for a response
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket;
                try {
                    receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    c.receive(receivePacket);
                    logger.debug("RainMachine discovery received from {}:{}", receivePacket.getAddress(),
                            receivePacket.getPort());
                } catch (SocketTimeoutException e) {
                    return rList;
                }

                // Check if the message is correct
                RainMachineDiscoveryData message = new RainMachineDiscoveryData(
                        new String(receivePacket.getData(), "UTF-8").trim());
                logger.debug("RainMachine discovery received data {}", message);

                if (message.isValid()) {
                    rList.add(message);
                }
            }
        } catch (IOException ex) {
            logger.debug("RainMachine discovery exception", ex);
            return rList;
        }
    }
}
