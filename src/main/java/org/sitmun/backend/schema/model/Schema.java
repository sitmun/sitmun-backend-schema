package org.sitmun.backend.schema.model;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Schema {
    private String name;
    private List<Table> tables;

    public boolean hasTable(String name) {
        return tables.stream().anyMatch((it) -> Objects.equals(it.getName(), name));
    }

    public Table getTable(String name) {
        return tables.stream().filter((it) -> Objects.equals(it.getName(), name)).findFirst().orElse(null);
    }
}
