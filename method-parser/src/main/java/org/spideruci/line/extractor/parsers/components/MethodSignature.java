package org.spideruci.line.extractor.parsers.components;

public class MethodSignature extends Component {

    public final String file_path;
    public final String className;
    public final String mname;
    public final String rtype;
    public final int line_start;
    public final int line_end;

    // TODO add parameters

    public MethodSignature(String file_path, String className, String mname, String rtype, int line_start, int line_end) {
        this.file_path = file_path;
        this.className = className;
        this.mname = mname;
        this.rtype = rtype;
        this.line_start = line_start;
        this.line_end = line_end;
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
}