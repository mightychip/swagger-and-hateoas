package ca.purpleowl.examples.swagger.rest.asset;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

@Getter
@Setter
public class ProgrammerAsset extends ResourceSupport {
    private Long programmerId;
    private String name;
    private String dateHired;
    @JsonInclude(Include.NON_EMPTY)
    private Long teamId;
    @JsonInclude(Include.NON_EMPTY)
    private String teamName;
}
