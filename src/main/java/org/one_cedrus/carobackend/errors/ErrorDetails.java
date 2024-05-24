package org.one_cedrus.carobackend.errors;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorDetails {

    private LocalDateTime timestamp;
    private Integer status;
    private String detail;
}
