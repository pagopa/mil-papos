package it.pagopa.swclient.mil.papos.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Terminal {
    private String pspId;
    private String terminalId;
    private Boolean enabled;
    private String payeeCode;
    private List<String> workstations;
}
