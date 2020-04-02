package org.spideruci.line.extractor.parsers.components;

import java.util.List;
import java.util.Objects;

public class MethodSignature extends Component {

    public final String file_path;
    public final String className;
    public final String mname;
    public final String rtype;
    public final List<String> parameters;
    public final int line_start;
    public final int line_end;
    public final List<String> annotations;

    public MethodSignature(String file_path, String className, String mname, String rtype, List<String> parameters, int line_start, int line_end, List<String> annotations) {
        this.file_path = file_path;
        this.className = className;
        this.mname = mname;
        this.rtype = rtype;
        this.parameters = parameters;
        this.line_start = line_start;
        this.line_end = line_end;
        this.annotations = annotations;
    }

    @Override
    public String asString() {
        return String.format(
            "MethodSignature(%s %s %s %s - %d,%d)",
            this.file_path,
            this.rtype,
            this.className,
            this.mname,
            this.line_start,
            this.line_end
        );
    }

    public boolean containAnnotation(String annotation) {
        return this.annotations.contains(annotation);
    }

    @Override
    public boolean equals(Object other){
        if (this == other) {
            return true;
        }

        if (!(other instanceof MethodSignature)){
            return false;
        }

        MethodSignature otherMethod = (MethodSignature) other;

        return otherMethod.className.equals(this.className) &&
                otherMethod.file_path.equals(this.file_path) &&
                otherMethod.mname.equals(this.mname) &&
                otherMethod.rtype.equals(this.rtype) &&
                otherMethod.parameters.equals(this.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.className, this.file_path, this.mname, this.rtype, this.parameters);
    }
}