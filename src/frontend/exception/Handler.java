package frontend.exception;

import util.Pair;

import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;

public class Handler {
    private final SortedSet<FrontEndException> exceptions = new TreeSet<>();

    public void save(FrontEndException e, ExceptionType type, Pair<Integer, Integer> position) {
        e.set(type, position.getLeft(), position.getRight());
        exceptions.add(e);
    }

    public String getExceptionCode() {
        StringJoiner joiner = new StringJoiner("\n");
        exceptions.forEach(o -> joiner.add(o.getExceptionCode()));
        return joiner.toString();
    }

    public boolean hasException() {
        return !exceptions.isEmpty();
    }
}
