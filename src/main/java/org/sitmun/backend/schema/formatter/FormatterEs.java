package org.sitmun.backend.schema.formatter;

import org.sitmun.backend.schema.model.Configuration;
import org.sitmun.backend.schema.model.Entity;
import schemacrawler.schema.*;
import schemacrawler.utility.MetaDataUtility;

import java.util.*;
import java.util.stream.Collectors;

import static us.fatehi.utility.Utility.hasNoUpperCase;

public class FormatterEs {

    public String indexDetails(Index index) {
        final IndexType indexType = index.getIndexType();
        String indexTypeString = "";
        if (indexType != IndexType.unknown && indexType != IndexType.other) {
            indexTypeString = indexType.toString() + " ";
        }
        return "índice " + indexTypeString + (index.isUnique() ? "" : "no ") + "unique ";
    }

    public String sortSequence(IndexColumn index) {
        if (index.getSortSequence().name().equals("ascending")) {
            return "creciente";
        } else {
            return "decreciente";
        }
    }

    public String ruleDetails(ForeignKey foreignKey) {
        String updateRuleString = "";
        final ForeignKeyUpdateRule updateRule = foreignKey.getUpdateRule();
        if (updateRule != null && updateRule != ForeignKeyUpdateRule.unknown) {
            updateRuleString = ", al actualizar '" + updateRule +"'";
        }

        String deleteRuleString = "";
        final ForeignKeyUpdateRule deleteRule = foreignKey.getDeleteRule();
        if (deleteRule != null && deleteRule != ForeignKeyUpdateRule.unknown) {
            deleteRuleString = ", al borrar '" + deleteRule + "'";
        }

        final String ruleString;
        if (deleteRule != null
                && updateRule == deleteRule
                && updateRule != ForeignKeyUpdateRule.unknown) {
            ruleString = ", con '" + deleteRule + "'";
        } else {
            ruleString = updateRuleString + deleteRuleString;
        }
        return ruleString;
    }

    public String cardinality(ForeignKey foreignKey) {
        switch (MetaDataUtility.findForeignKeyCardinality(foreignKey)) {
            case zero_one: return "0..1";
            case zero_many: return "0..*";
            case one_one: return "1..1";
            default: return "";
        }
    }

    public String columnTypeName(Column column) {
        final String columnTypeName;
        columnTypeName = column.getColumnDataType().getDatabaseSpecificTypeName();
        final String columnType = columnTypeName + column.getWidth();
        final String nullable = columnNullable(columnTypeName, column.isNullable());
        return columnType + nullable;
    }

    protected String columnNullable(final String columnTypeName, final boolean isNullable) {
        final String columnNullable;
        if (isNullable) {
            columnNullable = "";
        } else if (hasNoUpperCase(columnTypeName)) {
            columnNullable = " not null";
        } else {
            columnNullable = " NOT NULL";
        }
        return columnNullable;
    }

    public String formatTitle(String tableName, Configuration config) {
        Optional<Entity> entity = config.getSchema().getEntities().stream()
                .filter((it) -> Objects.equals(it.getTable(), tableName))
                .findFirst();
        if (entity.isPresent()) {
            Entity configEntity = entity.get();
            StringBuilder sb = new StringBuilder();
            if (configEntity.getType() == null) {
                sb.append("Tabla ");
            } else {
                sb.append(configEntity.getType()).append(" ");
            }
            if (configEntity.getName() == null) {
                sb.append("`").append(tableName).append("`");
            } else {
                sb.append("\"").append(configEntity.getName()).append("\" (`").append(tableName).append("`)");
            }
            return sb.toString();
        } else {
            return "Tabla `"+tableName+"`";
        }
    }

    public Collection<Table> sortTables(Collection<Table> list, Configuration config) {
        return list.stream()
                .sorted(Comparator.comparing((it) -> {
                    Entity entity = config.getSchema().getEntitity(it.getName());
                    if (entity == null || entity.getType() ==  null) {
                        return "";
                    } else {
                        return entity.getType();
                    }
                }))
                .collect(Collectors.toList());
    }

}
