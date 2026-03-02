package com.ticketing.order.Api.Mapper;

import com.ticketing.order.Api.model.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.servlet.tags.BindTag;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderResponse toResponse(OrderEntity order);
}
