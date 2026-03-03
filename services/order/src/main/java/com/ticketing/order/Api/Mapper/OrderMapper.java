package com.ticketing.order.Api.Mapper;

import com.ticketing.order.Api.dto.OrderResponse;
import com.ticketing.order.Api.model.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    OrderResponse toResponse(OrderEntity order);
}
