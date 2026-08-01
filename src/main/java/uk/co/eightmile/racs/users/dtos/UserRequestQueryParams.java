package uk.co.eightmile.racs.users.dtos;

import uk.co.eightmile.racs.common.validation.Lowercase;
import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import uk.co.eightmile.racs.common.validation.SortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserRequestQueryParams extends RequestQueryParams {

    @SortBy(allowed = {"createdAt", "updatedAt", "firstName", "lastName", "email"})
    private String sortBy = "createdAt:desc";

    private String email = "";

    private String firstName = "";

    private String lastName = "";

    private String campaignId;

    private Boolean inactive;
}
