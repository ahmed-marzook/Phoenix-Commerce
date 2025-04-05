package com.kaizenflow.commerce.order.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.kaizenflow.commerce.order.domain.model.Order;
import com.kaizenflow.commerce.order.model.OrderDTO;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public interface OrderMapper {

    OrderDTO toDTO(Order order);
}
