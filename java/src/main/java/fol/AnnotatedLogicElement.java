package fol;

public class AnnotatedLogicElement {
    private final LogicElement _first;
    private final String _second;
    private java.lang.String _roleFirst = "Axiom";
    private java.lang.String _roleSecond = "Origin";

    public AnnotatedLogicElement(LogicElement first, String second) {
        _first = first;
        _second = second;
    }

    public AnnotatedLogicElement(LogicElement first, String second, java.lang.String roleFirst, java.lang.String roleSecond) {
        this(first, second);
        _roleFirst = roleFirst;
        _roleSecond = roleSecond;
    }

    public LogicElement getFirst() {
        return _first;
    }

    public String getSecond() {
        return _second;
    }

    @Override
    public java.lang.String toString() {
        return (_roleFirst + ": " + _first + ", " + _roleSecond + ":" + _second);
    }
}
