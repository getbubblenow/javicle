package jvc.service;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum JIndexType {

    single, from, to;

    @JsonCreator public static JIndexType fromString (String v) { return valueOf(v.toLowerCase()); }

}
