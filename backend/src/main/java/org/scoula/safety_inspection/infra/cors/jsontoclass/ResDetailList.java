// ResDetailList.java

package org.scoula.safety_inspection.infra.cors.jsontoclass;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResDetailList {
    private String resNumber;
    private String resContents;

    @JsonProperty("resNumber")
    public String getResNumber() { return resNumber; }
    @JsonProperty("resNumber")
    public void setResNumber(String value) { this.resNumber = value; }

    @JsonProperty("resContents")
    public String getResContents() { return resContents; }
    @JsonProperty("resContents")
    public void setResContents(String value) { this.resContents = value; }
}
