package org.sitmun.backend.schema;

import schemacrawler.schema.*;
import schemacrawler.utility.MetaDataUtility;

import static us.fatehi.utility.Utility.hasNoUpperCase;

public class Formatter {

    public String indexDetails(Index index) {
        final IndexType indexType = index.getIndexType();
        String indexTypeString = "";
        if (indexType != IndexType.unknown && indexType != IndexType.other) {
            indexTypeString = indexType.toString() + " ";
        }
        return (index.isUnique() ? "" : "non-") + "unique " + indexTypeString + "index";
    }

    public String sortSequence(IndexColumn index) {
        return index.getSortSequence().name();
    }

    public String ruleDetails(ForeignKey foreignKey) {
        String updateRuleString = "";
        final ForeignKeyUpdateRule updateRule = foreignKey.getUpdateRule();
        if (updateRule != null && updateRule != ForeignKeyUpdateRule.unknown) {
            updateRuleString = ", on update " + updateRule;
        }

        String deleteRuleString = "";
        final ForeignKeyUpdateRule deleteRule = foreignKey.getDeleteRule();
        if (deleteRule != null && deleteRule != ForeignKeyUpdateRule.unknown) {
            deleteRuleString = ", on delete " + deleteRule;
        }

        final String ruleString;
        if (deleteRule != null
                && updateRule == deleteRule
                && updateRule != ForeignKeyUpdateRule.unknown) {
            ruleString = ", with " + deleteRule;
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
}
