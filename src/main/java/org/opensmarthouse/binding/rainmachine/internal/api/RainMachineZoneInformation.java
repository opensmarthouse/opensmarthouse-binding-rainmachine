package org.opensmarthouse.binding.rainmachine.internal.api;

public class RainMachineZoneInformation {
    public Integer uid;
    public String name;
    public Integer state;
    public Boolean active;
    public Integer userDuration;
    public Integer machineDuration;
    public Integer remaining;
    public Integer cycle;
    public Integer noOfCycles;
    public Boolean restriction;
    public Integer type;
    public Boolean master;

    @Override
    public String toString() {
        return "RainMachineZoneInformation [uid=" + uid + ", name=" + name + ", state=" + state + ", active=" + active
                + ", userDuration=" + userDuration + ", machineDuration=" + machineDuration + ", remaining=" + remaining
                + ", cycle=" + cycle + ", noOfCycles=" + noOfCycles + ", restriction=" + restriction + ", type=" + type
                + ", master=" + master + "]";
    }
}
