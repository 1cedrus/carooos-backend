package org.one_cedrus.carobackend.chat.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pagination<Item> {

    private List<Item> items;
    private Integer from;
    private Integer perPage;
    private Boolean hasNextPage;
    private Integer total;
}
