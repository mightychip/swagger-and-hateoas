package ca.purpleowl.examples.swagger.rest.asset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

//TODO Something to think about... maybe instead of extending resourcesupport, we just wrap our assets in Resource?
@Data
public class TeamAsset {
    private Long teamId;
    private String name;
    private String teamFocus;
    @JsonInclude(Include.NON_EMPTY)
    private String lastStandUp;

    public TeamAsset(){}
}
