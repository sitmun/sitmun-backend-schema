package org.sitmun.backend.schema.model;

import lombok.Data;

import java.util.List;

@Data
public class Output {
    private String folder;
    private String language;
    private List<String> includePre;
    private List<String> includePost;
    private List<String> copyFiles;
}
