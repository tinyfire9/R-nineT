package com.RnineT.Transfer.Drives.AmazonDrive.api;

import java.util.ArrayList;
import java.util.Date;

public class Resource {
    private final String KIND_FILE = "FILE";
    private final String KIND_FOLDER = "FOLDER";

    private String id;
    private String name;
    private String kind;
    private String description;
    private String status;
    private String contentType;
    private String extension;
    private Long size;
    private Long version;
    private Date modifiedDate;
    private Date createdDate;
    private ArrayList<String> labels;
    private String createdBy;
    private ArrayList<String> parents;
    private Resource(
            String id,
            String name,
            String kind,
            String description,
            String status,
            String contentType,
            String extension,
            Long size,
            Long version,
            Date modifiedDate,
            Date createdDate,
            ArrayList<String> labels,
            String createdBy,
            ArrayList<String> parents
    ){
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.description = description;
        this.status = status;
        this.contentType = contentType;
        this.extension = extension;
        this.size = size;
        this.version = version;
        this.modifiedDate = modifiedDate;
        this.createdDate = createdDate;
        this.labels = labels;
        this.createdBy = createdBy;
        this.parents = parents;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public ArrayList<String> getParents() {
        return parents;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public Long getVersion() {
        return version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getKind() {
        return kind;
    }

    public String getStatus() {
        return status;
    }

    public Long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public String getDescription() {
        return description;
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean isFile(){
        return this.kind.equals(KIND_FILE);
    }

    public boolean isFolder(){
        return this.kind.equals(KIND_FOLDER);
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    public static class Builder{
        private String id;
        private String name;
        private String kind;
        private String description;
        private String status;
        private String contentType;
        private String extension;
        private Long size;
        private Long version;
        private Date modifiedDate;
        private Date createdDate;
        private ArrayList<String> labels;
        private String createdBy;
        private ArrayList<String> parents;

        public Resource build(){
            return new Resource(
                id,
                name,
                kind,
                description,
                status,
                contentType,
                extension,
                size,
                version,
                modifiedDate,
                createdDate,
                labels,
                createdBy,
                parents
            );
        }

        public Builder setCreatedDate(Date createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder setKind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder setModifiedDate(Date modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setLabels(ArrayList<String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder setParents(ArrayList<String> parents) {
            this.parents = parents;
            return this;
        }

        public Builder setVersion(Long version) {
            this.version = version;
            return this;
        }

        public Builder setSize(Long size) {
            this.size = size;
            return this;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setExtension(String extension) {
            this.extension = extension;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }
    }
}