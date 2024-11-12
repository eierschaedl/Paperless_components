package org.example.paperless_components.RestAPI.service.mapper;

public interface Mapper<S, T> {

    T mapToDto(S source);

}