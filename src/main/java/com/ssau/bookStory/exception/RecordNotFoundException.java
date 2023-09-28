package com.ssau.bookStory.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordNotFoundException extends RuntimeException{
    private String entity;

    private String filter;
}
