package com.modelvault.modelvault.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.modelvault.modelvault.config.JwtUtil;
import com.modelvault.modelvault.model.Models.AuditLog;
import com.modelvault.modelvault.model.Models.LoginRequest;
import com.modelvault.modelvault.model.Models.Model;
import com.modelvault.modelvault.model.Models.ModelStage;
import com.modelvault.modelvault.model.Models.ModelStageTransition;
import com.modelvault.modelvault.model.Models.ModelVersion;
import com.modelvault.modelvault.service.AllServices;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1")
public class AllControllers {

	@Autowired
	private AllServices allServices;
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/auth/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
		String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

		String token = jwtUtil.generateToken(userDetails.getUsername(), role);

		Map<String, String> response = new HashMap<>();
		response.put("token", token);
		response.put("username", userDetails.getUsername());
		response.put("role", role);

		return ResponseEntity.ok(response);
	}

	// Model Management APIs
	@PostMapping("/models")
	@PreAuthorize("hasRole('ML_ENGINEER') or hasRole('ADMIN')")
	public ResponseEntity<Model> createModel(@RequestBody Model model) {
		Model created = allServices.createModel(model);
		return ResponseEntity.ok(created);
	}

	@GetMapping("/models")
	public ResponseEntity<List<Model>> listModels(@RequestParam(required = false) String name,
			@RequestParam(required = false) String tag) {
		return ResponseEntity.ok(allServices.listModels(name, tag));
	}

	@GetMapping("/models/{modelId}")
	public ResponseEntity<Model> getModel(@PathVariable Long modelId) {
		return ResponseEntity.ok(allServices.getModel(modelId));
	}

	// Model Version APIs
	@PostMapping("/models/{modelId}/versions")
	@PreAuthorize("hasRole('ML_ENGINEER') or hasRole('ADMIN')")
	public ResponseEntity<ModelVersion> registerVersion(@PathVariable Long modelId, @RequestBody ModelVersion version) {
		version.setModelId(modelId);
		ModelVersion created = allServices.registerVersion(version);
		return ResponseEntity.ok(created);
	}

	@GetMapping("/models/{modelId}/versions")
	public ResponseEntity<List<ModelVersion>> listVersions(@PathVariable Long modelId) {
		return ResponseEntity.ok(allServices.listVersions(modelId));
	}

	@GetMapping("/models/{modelId}/versions/{versionId}")
	public ResponseEntity<ModelVersion> getVersion(@PathVariable Long modelId, @PathVariable Long versionId) {
		return ResponseEntity.ok(allServices.getVersion(versionId));
	}

	// Artifact APIs
	@PostMapping("/artifacts/upload-url")
	@PreAuthorize("hasRole('ML_ENGINEER') or hasRole('ADMIN')")
	public ResponseEntity<Map<String, String>> getUploadUrl(@RequestBody Map<String, String> request)
			throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException,
			InvalidResponseException, NoSuchAlgorithmException, XmlParserException, ServerException,
			IllegalArgumentException, IOException {
		String artifactUri = request.get("artifactUri");
		Map<String, String> response = allServices.generateUploadUrl(artifactUri);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/artifacts/download-url")
	public ResponseEntity<String> getDownloadUrl(@RequestParam String artifactUri) throws InvalidKeyException,
			ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
			NoSuchAlgorithmException, XmlParserException, ServerException, IllegalArgumentException, IOException {
		String url = allServices.generateDownloadUrl(artifactUri);
		return ResponseEntity.ok(url);
	}

	// Lifecycle APIs
	@PostMapping("/models/{modelId}/versions/{versionId}/transition")
	@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
	public ResponseEntity<ModelStage> promoteVersion(@PathVariable Long modelId, @PathVariable Long versionId,
			@RequestBody ModelStageTransition transition) {
		ModelStage stage = allServices.promoteVersion(versionId, transition);
		return ResponseEntity.ok(stage);
	}

	@GetMapping("/models/{modelId}/versions/{versionId}/stages")
	public ResponseEntity<List<ModelStage>> getStageHistory(@PathVariable Long modelId, @PathVariable Long versionId) {
		return ResponseEntity.ok(allServices.getStageHistory(versionId));
	}

	// Discovery APIs
	@GetMapping("/models/{modelName}/production")
	public ResponseEntity<ModelVersion> getProductionModel(@PathVariable String modelName) {
		return ResponseEntity.ok(allServices.getProductionModel(modelName));
	}

	// Audit APIs
	@GetMapping("/audit")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<AuditLog>> getAuditLogs(@RequestParam String entityType, @RequestParam Long entityId) {
		return ResponseEntity.ok(allServices.getAuditLogs(entityType, entityId));
	}
}