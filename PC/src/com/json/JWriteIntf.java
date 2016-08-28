package com.json;

import java.io.IOException;

public interface JWriteIntf {

    public boolean beforeWrite(Appendable writer, JElement element) throws IOException;

    public void afterWrite(Appendable writer, JElement element) throws IOException;

    public boolean beforeWriteName(Appendable writer, JElement element, String name) throws IOException;
}
