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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Service that keeps track of host names/ip addresses of discovered RainMachine devices.
 *
 * @author Chris Jackson - Initial contribution
 */
@NonNullByDefault
public interface RainMachineAddressCache {
    /**
     * Returns the known host name/ip address for the device with the given ID.
     * If not known an empty string is returned.
     *
     * @param id the ID of the RainMachine to get host address for
     * @return the known host address or an empty string if not known
     */
    String getLastKnownHostAddress(String id);
}
