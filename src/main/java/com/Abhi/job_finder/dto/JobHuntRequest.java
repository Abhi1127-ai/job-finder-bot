package com.Abhi.job_finder.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
@Data
public class JobHuntRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String resume;

}
