package org.sitmun.backend.schema.model;

import com.typesafe.config.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entity {
    @Optional
    private String name;
    private String type;
    private String table;
    private String description;
    @Optional
    private List<Column> columns;
}
