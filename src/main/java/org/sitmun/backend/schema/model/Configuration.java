package org.sitmun.backend.schema.model;

import lombok.Data;

@Data
public class Configuration {
    private Output output;
    private Database database;
    private Schema schema;
}

