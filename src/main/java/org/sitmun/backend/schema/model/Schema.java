package org.sitmun.backend.schema.model;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Schema {
    private String name;
    private List<Entity> entities;

    public boolean hasEntity(String table) {
        return entities.stream().anyMatch((it) -> Objects.equals(it.getTable(), table));
    }

    public Entity getEntitity(String table) {
        return entities.stream().filter((it) -> Objects.equals(it.getTable(), table)).findFirst().orElse(null);
    }

    public Column getColumn(String tableName, String columnName) {
        Entity table = getEntitity(tableName);
        if (table == null || table.getColumns() == null)
            return null;
        return table.getColumns().stream().filter((it) -> Objects.equals(it.getName(), columnName)).findFirst().orElse(null);
    }
}
