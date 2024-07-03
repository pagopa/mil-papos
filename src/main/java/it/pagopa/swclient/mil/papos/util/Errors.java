package it.pagopa.swclient.mil.papos.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@RegisterForReflection
@JsonInclude(Include.NON_NULL)
@Getter
@Setter
@ToString
public class Errors {
    private List<String> codes;
    private List<String> descriptions;

    public Errors(String error, String description) {
        this.codes = List.of(error);
        this.descriptions = List.of(description);
    }
}
