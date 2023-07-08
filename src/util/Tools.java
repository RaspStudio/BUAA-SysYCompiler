package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Tools {

    public static <T> List<T> merge(List<T> list, T[] append) {
        List<T> ret = new ArrayList<>(list);
        ret.addAll(Arrays.asList(append));
        return ret;
    }

    public static <T> List<T> merge(List<T> list, List<T> append) {
        List<T> ret = new ArrayList<>(list);
        ret.addAll(append);
        return ret;
    }

    public static List<Integer> increase(List<Integer> src, List<Integer> limit) {
        List<Integer> ret = new ArrayList<>(src);
        for (int i = ret.size() - 1; i >= 0; i--) {
            if (ret.get(i) + 1 < limit.get(i)) {
                ret.set(i, ret.get(i) + 1);
                return ret;
            } else {
                ret.set(i, 0);
            }
        }
        return null;
    }

}
