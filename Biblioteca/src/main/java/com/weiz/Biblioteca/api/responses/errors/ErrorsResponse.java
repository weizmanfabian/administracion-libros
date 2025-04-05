package com.weiz.Biblioteca.api.responses.errors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ErrorsResponse extends BaseErrorResponse {
    private Map<String, String> errors;
}