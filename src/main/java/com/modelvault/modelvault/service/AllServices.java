package com.modelvault.modelvault.service;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.modelvault.modelvault.dao.AllDaos;
import com.modelvault.modelvault.model.Models.AuditLog;
import com.modelvault.modelvault.model.Models.Model;
import com.modelvault.modelvault.model.Models.ModelStage;
import com.modelvault.modelvault.model.Models.ModelStageTransition;
import com.modelvault.modelvault.model.Models.ModelVersion;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AllServices {

	@Autowired
	private AllDaos allDaos;

	@Autowired
	private MinioClient minioClient;

	@Autowired
	private DataSourceTransactionManager transactionManager;

	// Placeholder for JWT generation (implement fully with secret key)
	public String generateJwtToken(String username, String password) {
		// Validate credentials and generate JWT. Placeholder return.
		return "jwt-token-placeholder";
	}

	public Model createModel(Model model) {
		return allDaos.insertModel(model);
	}

	public List<Model> listModels(String name, String tag) {
		return allDaos.getModels(name, tag);
	}

	public Model getModel(Long modelId) {
		return allDaos.getModelById(modelId);
	}

	public ModelVersion registerVersion(ModelVersion version) {
		// Validate artifact exists in MinIO (optional check)
		return allDaos.insertModelVersion(version);
	}

	public List<ModelVersion> listVersions(Long modelId) {
		return allDaos.getVersionsByModelId(modelId);
	}

	public ModelVersion getVersion(Long versionId) {
		return allDaos.getVersionById(versionId);
	}

	public Map<String, String> generateUploadUrl(String artifactUri) throws InvalidKeyException, ErrorResponseException,
			InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
			XmlParserException, ServerException, IllegalArgumentException, IOException {
		String bucket = artifactUri.split("/")[2]; // Parse from s3://bucket/path
		String object = artifactUri.substring(artifactUri.indexOf("/", 5) + 1);
		String uploadUrl = minioClient.getPresignedObjectUrl(io.minio.GetPresignedObjectUrlArgs.builder()
				.method(Method.PUT).bucket(bucket).object(object).expiry(1, TimeUnit.HOURS).build());
		Map<String, String> response = new HashMap<>();
		response.put("uploadUrl", uploadUrl);
		response.put("artifactUri", artifactUri);
		return response;
	}

	public String generateDownloadUrl(String artifactUri) throws InvalidKeyException, ErrorResponseException,
			InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
			XmlParserException, ServerException, IllegalArgumentException, IOException {
		String bucket = artifactUri.split("/")[2];
		String object = artifactUri.substring(artifactUri.indexOf("/", 5) + 1);
		return minioClient.getPresignedObjectUrl(io.minio.GetPresignedObjectUrlArgs.builder().method(Method.GET)
				.bucket(bucket).object(object).expiry(1, TimeUnit.HOURS).build());
	}

	public ModelStage promoteVersion(Long versionId, ModelStageTransition transition) {
		TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			// Lock and validate
			ModelVersion version = allDaos.getVersionById(versionId);
			// Assume validation logic for schema/metrics
			// Archive existing PROD if needed
			allDaos.archiveExistingProd(version.getModelId());
			ModelStage stage = new ModelStage();
			stage.setModelVersionId(versionId);
			stage.setStage(transition.getToStage());
			stage.setChangedBy("user"); // From security context
			stage.setComment(transition.getComment());
			allDaos.insertModelStage(stage);
			// Insert audit
			AuditLog log = new AuditLog();
			log.setEntityType("MODEL_VERSION");
			log.setEntityId(versionId);
			log.setAction("PROMOTE");
			log.setOldState(Map.of("stage", transition.getFromStage()).toString());
			log.setNewState(Map.of("stage", transition.getToStage()).toString());
			log.setPerformedBy("user");
			allDaos.insertAuditLog(log);
			transactionManager.commit(status);
			return stage;
		} catch (Exception e) {
			transactionManager.rollback(status);
			throw e;
		}
	}

	public List<ModelStage> getStageHistory(Long versionId) {
		return allDaos.getStagesByVersionId(versionId);
	}

	public ModelVersion getProductionModel(String modelName) {
		return allDaos.getProductionVersionByModelName(modelName);
	}

	public List<AuditLog> getAuditLogs(String entityType, Long entityId) {
		return allDaos.getAuditLogs(entityType, entityId);
	}
}