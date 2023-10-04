package no.hal.plugin;

public interface LifeCycle {
    
    void activate(InstanceRegistry instanceRegistry);

    public static boolean activate(LifeCycle activatable, InstanceRegistry instanceRegistry) {
        try {
            activatable.activate(instanceRegistry);
            return true;
        } catch (RuntimeException e) {
            System.err.println("Couldn't activate " + activatable + ": " + e.getMessage());
            return false;
        }
    }
}
