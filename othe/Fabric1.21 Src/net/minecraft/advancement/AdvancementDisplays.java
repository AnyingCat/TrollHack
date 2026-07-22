/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlacedAdvancement;

public class AdvancementDisplays {
    private static final int DISPLAY_DEPTH = 2;

    private static Status getStatus(Advancement advancement, boolean force) {
        Optional<AdvancementDisplay> optional = advancement.display();
        if (optional.isEmpty()) {
            return Status.HIDE;
        }
        if (force) {
            return Status.SHOW;
        }
        if (optional.get().isHidden()) {
            return Status.HIDE;
        }
        return Status.NO_CHANGE;
    }

    private static boolean shouldDisplay(Stack<Status> statuses) {
        for (int i = 0; i <= 2; ++i) {
            Status status = statuses.peek(i);
            if (status == Status.SHOW) {
                return true;
            }
            if (status != Status.HIDE) continue;
            return false;
        }
        return false;
    }

    private static boolean shouldDisplay(PlacedAdvancement advancement, Stack<Status> statuses, Predicate<PlacedAdvancement> donePredicate, ResultConsumer consumer) {
        boolean bl = donePredicate.test(advancement);
        Status status = AdvancementDisplays.getStatus(advancement.getAdvancement(), bl);
        boolean bl2 = bl;
        statuses.push(status);
        for (PlacedAdvancement placedAdvancement : advancement.getChildren()) {
            bl2 |= AdvancementDisplays.shouldDisplay(placedAdvancement, statuses, donePredicate, consumer);
        }
        boolean bl3 = bl2 || AdvancementDisplays.shouldDisplay(statuses);
        statuses.pop();
        consumer.accept(advancement, bl3);
        return bl2;
    }

    public static void calculateDisplay(PlacedAdvancement advancement, Predicate<PlacedAdvancement> donePredicate, ResultConsumer consumer) {
        PlacedAdvancement placedAdvancement = advancement.getRoot();
        ObjectArrayList<Status> stack = new ObjectArrayList<Status>();
        for (int i = 0; i <= 2; ++i) {
            stack.push(Status.NO_CHANGE);
        }
        AdvancementDisplays.shouldDisplay(placedAdvancement, stack, donePredicate, consumer);
    }

    static enum Status {
        SHOW,
        HIDE,
        NO_CHANGE;

    }

    @FunctionalInterface
    public static interface ResultConsumer {
        public void accept(PlacedAdvancement var1, boolean var2);
    }
}

