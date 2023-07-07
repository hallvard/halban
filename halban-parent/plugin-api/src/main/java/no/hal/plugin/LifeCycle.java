package no.hal.plugin;

public interface LifeCycle {
    
    void activate(Context context);

    public static boolean activate(LifeCycle activatable, Context context) {
        try {
            activatable.activate(context);
            return true;
        } catch (RuntimeException e) {
            System.err.println("Couldn't activate " + activatable + ": " + e.getMessage());
            return false;
        }
    }
}
