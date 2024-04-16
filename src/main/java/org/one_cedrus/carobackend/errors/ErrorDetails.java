package org.one_cedrus.carobackend.errors;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorDetails {
    private LocalDateTime timestamp;
    private Integer status;
    private String detail;
}
