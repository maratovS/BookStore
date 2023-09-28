package com.ssau.bookStory.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("Convert2Diamond")
@Configuration
public class MapperConfiguration {

    @SuppressWarnings("Convert2Lambda")
    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }
}
