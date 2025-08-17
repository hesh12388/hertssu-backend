package com.hertssu.Proposals.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProposalStatusRequest {
    @NotNull(message = "Status is required")
    private ProposalStatus status;
}
