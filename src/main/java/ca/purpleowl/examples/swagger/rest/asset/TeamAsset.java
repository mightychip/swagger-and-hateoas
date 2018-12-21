package ca.purpleowl.examples.swagger.rest.asset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
public class TeamAsset {
    private Long teamId;
    private String name;
    private String teamFocus;
    @JsonInclude(Include.NON_EMPTY)
    private String lastStandUp;

    public TeamAsset(){}
}
