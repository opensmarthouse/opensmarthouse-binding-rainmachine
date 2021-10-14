package org.opensmarthouse.binding.rainmachine.internal.discovery;

/**
 * RainMachine discovery data processor
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class RainMachineDiscoveryData {

    private final String fingerprint = "SPRINKLER";

    private final String id;
    private final String mac;
    private final String name;
    private final String address;

    public RainMachineDiscoveryData(String message) {
        String[] discoveryData = message.split("\\|\\|");

        id = discoveryData[0];
        mac = discoveryData[1];
        name = discoveryData[2];
        address = discoveryData[3];
    }

    public boolean isValid() {
        return fingerprint.equals(id);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getMac() {
        return mac;
    }

    @Override
    public String toString() {
        return "RainMachineDiscoveryData [id=" + id + ", mac=" + mac + ", name=" + name + ", address=" + address + "]";
    }
}
