package net.wuillemin.fxprotest;

import org.jetbrains.annotations.NotNull;

/**
 * FxPro is the main class of the application
 */
public class FxPro {

    public static void main(@NotNull String[] args) {

        int[] data = {5, 2, 3, 4, 5, 4, 0, 3, 1};

        System.out.println(
                String.format(
                        "The volume of water for the test is %d",
                        DivideAndConquerKt.calculateWaterAmount(data)));
    }
}
