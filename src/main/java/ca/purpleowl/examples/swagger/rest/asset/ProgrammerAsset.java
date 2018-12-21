package ca.purpleowl.examples.swagger.rest.asset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
public class ProgrammerAsset {
    private Long programmerId;
    private String name;
    private String dateHired;
    @JsonInclude(Include.NON_EMPTY)
    private Long teamId;
    @JsonInclude(Include.NON_EMPTY)
    private String teamName;
}
