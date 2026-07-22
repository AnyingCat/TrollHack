package github.trollhack.events;

import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;

import java.lang.invoke.MethodHandles;

public class EventBusHolder {
    public static final IEventBus INSTANCE = new EventBus();

    public static void init() {
        INSTANCE.registerLambdaFactory("github.trollhack",
                (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }
}
