package com.modelvault.modelvault.model;

import java.util.Date;
import java.util.Map;

public class Models {

	// Model class
	public static class Model {
		private Long id;
		private String name;
		private String description;
		private String owner;
		private String tags; // JSON string
		private Date createdAt;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getTags() {
			return tags;
		}

		public void setTags(String tags) {
			this.tags = tags;
		}

		public Date getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
		}
	}

	public static class LoginRequest {
		private String username;
		private String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	// ModelVersion class
	public static class ModelVersion {
		private Long id;
		private Long modelId;
		private String version;
		private String framework;
		private String metrics; // JSON string
		private String schema; // JSON string
		private String artifactUri;
		private Date createdAt;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getModelId() {
			return modelId;
		}

		public void setModelId(Long modelId) {
			this.modelId = modelId;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getFramework() {
			return framework;
		}

		public void setFramework(String framework) {
			this.framework = framework;
		}

		public String getMetrics() {
			return metrics;
		}

		public void setMetrics(String metrics) {
			this.metrics = metrics;
		}

		public String getSchema() {
			return schema;
		}

		public void setSchema(String schema) {
			this.schema = schema;
		}

		public String getArtifactUri() {
			return artifactUri;
		}

		public void setArtifactUri(String artifactUri) {
			this.artifactUri = artifactUri;
		}

		public Date getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
		}
	}

	// ModelStage class
	public static class ModelStage {
		private Long id;
		private Long modelVersionId;
		private String stage; // ENUM as string
		private boolean isActive;
		private String changedBy;
		private String comment;
		private Date changedAt;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getModelVersionId() {
			return modelVersionId;
		}

		public void setModelVersionId(Long modelVersionId) {
			this.modelVersionId = modelVersionId;
		}

		public String getStage() {
			return stage;
		}

		public void setStage(String stage) {
			this.stage = stage;
		}

		public boolean getIsActive() {
			return isActive;
		}

		public void setIsActive(boolean isActive) {
			this.isActive = isActive;
		}

		public String getChangedBy() {
			return changedBy;
		}

		public void setChangedBy(String changedBy) {
			this.changedBy = changedBy;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public Date getChangedAt() {
			return changedAt;
		}

		public void setChangedAt(Date changedAt) {
			this.changedAt = changedAt;
		}
	}

	// AuditLog class
	public static class AuditLog {
		private Long id;
		private String entityType;
		private Long entityId;
		private String action;
		private String oldState; // JSON string
		private String newState; // JSON string
		private String performedBy;
		private Date performedAt;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getEntityType() {
			return entityType;
		}

		public void setEntityType(String entityType) {
			this.entityType = entityType;
		}

		public Long getEntityId() {
			return entityId;
		}

		public void setEntityId(Long entityId) {
			this.entityId = entityId;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getOldState() {
			return oldState;
		}

		public void setOldState(String oldState) {
			this.oldState = oldState;
		}

		public String getNewState() {
			return newState;
		}

		public void setNewState(String newState) {
			this.newState = newState;
		}

		public String getPerformedBy() {
			return performedBy;
		}

		public void setPerformedBy(String performedBy) {
			this.performedBy = performedBy;
		}

		public Date getPerformedAt() {
			return performedAt;
		}

		public void setPerformedAt(Date performedAt) {
			this.performedAt = performedAt;
		}
	}

	// ModelStageTransition (for request)
	public static class ModelStageTransition {
		private String fromStage;
		private String toStage;
		private String comment;

		public String getFromStage() {
			return fromStage;
		}

		public void setFromStage(String fromStage) {
			this.fromStage = fromStage;
		}

		public String getToStage() {
			return toStage;
		}

		public void setToStage(String toStage) {
			this.toStage = toStage;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}
}