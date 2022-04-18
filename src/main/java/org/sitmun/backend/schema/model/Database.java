package org.sitmun.backend.schema.model;

import lombok.Data;

@Data
public class Database {
    private String url;
    private String user;
    private String password;
}
