package com.weiz.Biblioteca.api.responses.errors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseErrorResponse implements Serializable {
    private String status;
    private Integer code;
    private String timestamp;
    private String path;
}