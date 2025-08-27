package com.modelvault.modelvault.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.modelvault.modelvault.model.Models.AuditLog;
import com.modelvault.modelvault.model.Models.Model;
import com.modelvault.modelvault.model.Models.ModelStage;
import com.modelvault.modelvault.model.Models.ModelVersion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AllDaos {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Model insertModel(Model model) {
        String sql = "INSERT INTO models (name, description, owner, tags) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, model.getName(), model.getDescription(), model.getOwner(), model.getTags().toString());
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        model.setId(id);
        model.setCreatedAt(new java.util.Date());
        return model;
    }

    public List<Model> getModels(String name, String tag) {
        String sql = "SELECT * FROM models WHERE 1=1";
        if (name != null) sql += " AND name LIKE '%" + name + "%'";
        if (tag != null) sql += " AND tags LIKE '%" + tag + "%'";
        return jdbcTemplate.query(sql, new ModelRowMapper());
    }

    public Model getModelById(Long modelId) {
        String sql = "SELECT * FROM models WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new ModelRowMapper(), modelId);
    }

    public ModelVersion insertModelVersion(ModelVersion version) {
        String sql = "INSERT INTO model_versions (model_id, version, framework, metrics, schema_def, artifact_uri) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, version.getModelId(), version.getVersion(), version.getFramework(), version.getMetrics().toString(), version.getSchema().toString(), version.getArtifactUri());
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        version.setId(id);
        version.setCreatedAt(new java.util.Date());
        return version;
    }

    public List<ModelVersion> getVersionsByModelId(Long modelId) {
        String sql = "SELECT * FROM model_versions WHERE model_id = ?";
        return jdbcTemplate.query(sql, new ModelVersionRowMapper(), modelId);
    }

    public ModelVersion getVersionById(Long versionId) {
        String sql = "SELECT * FROM model_versions WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new ModelVersionRowMapper(), versionId);
    }

    public void archiveExistingProd(Long modelId) {
        String sql = "UPDATE model_stages SET is_active = FALSE WHERE stage = 'PRODUCTION' AND model_version_id IN (SELECT id FROM model_versions WHERE model_id = ?)";
        jdbcTemplate.update(sql, modelId);
    }

    public void insertModelStage(ModelStage stage) {
        String sql = "INSERT INTO model_stages (model_version_id, stage, is_active, changed_by, comment) VALUES (?, ?, TRUE, ?, ?)";
        jdbcTemplate.update(sql, stage.getModelVersionId(), stage.getStage(), stage.getChangedBy(), stage.getComment());
    }

    public List<ModelStage> getStagesByVersionId(Long versionId) {
        String sql = "SELECT * FROM model_stages WHERE model_version_id = ?";
        return jdbcTemplate.query(sql, new ModelStageRowMapper(), versionId);
    }

    public ModelVersion getProductionVersionByModelName(String modelName) {
        String sql = "SELECT mv.* FROM model_versions mv JOIN models m ON mv.model_id = m.id JOIN model_stages ms ON mv.id = ms.model_version_id WHERE m.name = ? AND ms.stage = 'PRODUCTION' AND ms.is_active = TRUE";
        return jdbcTemplate.queryForObject(sql, new ModelVersionRowMapper(), modelName);
    }

    public void insertAuditLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (entity_type, entity_id, action, old_state, new_state, performed_by) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, log.getEntityType(), log.getEntityId(), log.getAction(), log.getOldState().toString(), log.getNewState().toString(), log.getPerformedBy());
    }

    public List<AuditLog> getAuditLogs(String entityType, Long entityId) {
        String sql = "SELECT * FROM audit_logs WHERE entity_type = ? AND entity_id = ?";
        return jdbcTemplate.query(sql, new AuditLogRowMapper(), entityType, entityId);
    }

    // RowMappers
    private static class ModelRowMapper implements RowMapper<Model> {
        @Override
        public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
            Model model = new Model();
            model.setId(rs.getLong("id"));
            model.setName(rs.getString("name"));
            model.setDescription(rs.getString("description"));
            model.setOwner(rs.getString("owner"));
            model.setTags(rs.getString("tags")); // Parse JSON if needed
            model.setCreatedAt(rs.getDate("created_at"));
            return model;
        }
    }

    private static class ModelVersionRowMapper implements RowMapper<ModelVersion> {
        @Override
        public ModelVersion mapRow(ResultSet rs, int rowNum) throws SQLException {
            ModelVersion version = new ModelVersion();
            version.setId(rs.getLong("id"));
            version.setModelId(rs.getLong("model_id"));
            version.setVersion(rs.getString("version"));
            version.setFramework(rs.getString("framework"));
            version.setMetrics(rs.getString("metrics")); // Parse JSON
            version.setSchema(rs.getString("schema_def")); // Parse JSON
            version.setArtifactUri(rs.getString("artifact_uri"));
            version.setCreatedAt(rs.getDate("created_at"));
            return version;
        }
    }

    private static class ModelStageRowMapper implements RowMapper<ModelStage> {
        @Override
        public ModelStage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ModelStage stage = new ModelStage();
            stage.setId(rs.getLong("id"));
            stage.setModelVersionId(rs.getLong("model_version_id"));
            stage.setStage(rs.getString("stage"));
            stage.setIsActive(rs.getBoolean("is_active"));
            stage.setChangedBy(rs.getString("changed_by"));
            stage.setComment(rs.getString("comment"));
            stage.setChangedAt(rs.getDate("changed_at"));
            return stage;
        }
    }

    private static class AuditLogRowMapper implements RowMapper<AuditLog> {
        @Override
        public AuditLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditLog log = new AuditLog();
            log.setId(rs.getLong("id"));
            log.setEntityType(rs.getString("entity_type"));
            log.setEntityId(rs.getLong("entity_id"));
            log.setAction(rs.getString("action"));
            log.setOldState(rs.getString("old_state")); // Parse JSON
            log.setNewState(rs.getString("new_state")); // Parse JSON
            log.setPerformedBy(rs.getString("performed_by"));
            log.setPerformedAt(rs.getDate("performed_at"));
            return log;
        }
    }
}