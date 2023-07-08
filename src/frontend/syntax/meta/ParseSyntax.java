package frontend.syntax.meta;

import frontend.syntax.factories.SyntaxType;
import frontend.token.meta.MetaToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class ParseSyntax extends MetaSyntax {
    private final List<MetaSyntax> derivatives;

    public ParseSyntax(List<MetaSyntax> derivatives, SyntaxType type) {
        super(type);
        this.derivatives = Collections.unmodifiableList(new ArrayList<>(derivatives));
    }

    public MetaSyntax get(int index) {
        int offset;
        if (index < 0) {
            offset = index % derivatives.size() + derivatives.size();
        } else {
            offset = index % derivatives.size();
        }
        return derivatives.get(offset);
    }

    public int size() {
        return derivatives.size();
    }

    public MetaToken getToken(int index) {
        return TokenSyntax.getToken(get(index));
    }
    
    @Override
    public String getTokenCode() {
        StringJoiner joiner = new StringJoiner("\n");
        for (MetaSyntax s : derivatives) {
            joiner.add(s.getTokenCode());
        }
        if (type.getTokenCode() != null) {
            joiner.add("<" + type.getTokenCode() + ">");
        }
        return joiner.toString();
    }
}



















