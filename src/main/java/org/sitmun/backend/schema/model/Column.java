package org.sitmun.backend.schema.model;

import com.typesafe.config.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
    private String name;
    private String description;
    @Optional
    private List<String> tags;
}
