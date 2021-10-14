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
package org.opensmarthouse.binding.rainmachine.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RainMachineBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public class RainMachineBindingConstants {

    private static final String BINDING_ID = "rainmachine";

    // List of all Thing ids
    private static final String RAINMACHINE = "rainmachine";

    // List of all Thing Type UIDs
    public static final ThingTypeUID RAINMACHINE_BRIDGE = new ThingTypeUID(BINDING_ID, RAINMACHINE + "_bridge");
    public static final ThingTypeUID RAINMACHINE_ZONE = new ThingTypeUID(BINDING_ID, RAINMACHINE + "_zone");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(new ThingTypeUID[] { RAINMACHINE_BRIDGE, RAINMACHINE_ZONE }));

    // List of internal default values
    public static final int DEFAULT_WAIT_BEFORE_INITIAL_REFRESH = 30;
    public static final int DEFAULT_REFRESH_RATE = 60;
    public static final short DISCOVERY_SUBNET_MASK = 24;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final int DISCOVERY_THREAD_POOL_SHUTDOWN_WAIT_TIME_SECONDS = 300;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;

    // List of all Channel ids
    public static final String CHANNEL_ID_ZONE_STATE = "zone_state";
    public static final String CHANNEL_ID_LASTRAIN = "lastrain";

}
