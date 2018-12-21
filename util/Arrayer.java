package util;

import java.util.ArrayList;

public class Arrayer {
    public static <T> ArrayList<T> createEmptyArray(int size) {
        ArrayList<T> array = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            array.add(null);
        }
        return array;
    }
}
