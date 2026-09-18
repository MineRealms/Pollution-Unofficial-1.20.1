package meowmel.pollution.api.capability;

/** Marks a vis source whose vis counts as "clean" for magic recipes. */
public interface ICleanVis {

    default boolean isCleanVis() {
        return false;
    }
}
