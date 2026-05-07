# API Reference

Complete REST API documentation for AI Platform.

## Base URL

| Environment | Base URL |
|-------------|----------|
| Production | `https://<your-domain>/api/v1` |
| Local dev | `http://localhost:8080` |

## Authentication

All endpoints (except login) require a JWT Bearer token:

```
Authorization: Bearer <access-token>
```

### Response Format

All APIs return a standard wrapper:

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

Paginated responses:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [ ... ],
    "total": 100,
    "page": 0,
    "size": 20
  }
}
```

---

## Auth

### POST /auth/login

Authenticate and receive JWT tokens.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 7200000
  }
}
```

### POST /auth/refresh

Refresh an expired access token.

**Request:**
```json
{
  "refreshToken": "eyJhbGci..."
}
```

**Response:** Same as login.

### POST /auth/logout

Invalidate the current session.

**Headers:** `X-User-Id: <userId>`

**Response:**
```json
{ "code": 200, "message": "success" }
```

---

## Console APIs

### Users

#### GET /users

List users with pagination.

**Query Parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 20 | Page size |
| `tenantId` | long | — | Filter by tenant |
| `username` | string | — | Filter by username |

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      { "id": 1, "username": "admin", "email": "admin@example.com", "tenantId": 1, "roleId": 1, "status": "ACTIVE" }
    ],
    "total": 50,
    "page": 0,
    "size": 20
  }
}
```

#### POST /users

Create a new user.

**Request:**
```json
{
  "username": "john",
  "password": "Pass123!",
  "email": "john@example.com",
  "tenantId": 1,
  "roleId": 2,
  "status": "ACTIVE"
}
```

**Response:** `{"code": 200, "data": { "id": 2, "username": "john", ... }}`

#### PUT /users/{id}

Update a user.

#### DELETE /users/{id}

Delete a user.

### Roles

#### GET /roles

List roles.

**Query Parameters:** `page`, `size`, `tenantId`

#### POST /roles

Create a role.

**Request:**
```json
{
  "name": "DataScientist",
  "code": "DATA_SCIENTIST",
  "tenantId": 1,
  "description": "Can run experiments and manage models"
}
```

#### PUT /roles/{id}

Update a role.

#### DELETE /roles/{id}

Delete a role.

#### PUT /roles/{id}/permissions

Update role permissions.

**Request:**
```json
[
  { "resource": "lab:experiments", "action": "READ" },
  { "resource": "lab:experiments", "action": "WRITE" },
  { "resource": "inference:models", "action": "READ" }
]
```

### Tenants

#### GET /tenants

List tenants.

**Query Parameters:** `page`, `size`, `name`, `status`

#### GET /tenants/{id}

Get tenant details.

#### POST /tenants

Create a tenant.

**Request:**
```json
{
  "name": "Acme Corp",
  "code": "ACME",
  "status": "ACTIVE",
  "description": "Enterprise tenant"
}
```

#### PUT /tenants/{id}

Update a tenant.

#### PUT /tenants/{id}/status

Toggle tenant active/suspended status.

#### PUT /tenants/{id}/quota

Update tenant resource quota.

**Request:** JSON string of quota configuration.

#### DELETE /tenants/{id}

Delete a tenant.

### Projects

#### GET /projects

List projects.

**Query Parameters:** `page`, `size`, `tenantId`, `name`

#### GET /projects/{id}

Get project details.

#### POST /projects

Create a project.

**Request:**
```json
{
  "name": "Fraud Detection",
  "tenantId": 1,
  "description": "Credit card fraud detection model"
}
```

#### PUT /projects/{id}

Update a project.

#### DELETE /projects/{id}

Delete a project.

#### POST /projects/{id}/members

Add a member to a project.

**Query Parameters:** `userId`, `roleId`

#### DELETE /projects/{id}/members/{userId}

Remove a member from a project.

### Audit Logs

#### GET /audit-logs

List audit logs.

**Query Parameters:** `page`, `size`, `tenantId`, `userId`, `action`

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "id": 1,
        "userId": 1,
        "action": "CREATE_USER",
        "resource": "users",
        "details": "Created user john",
        "ipAddress": "192.168.1.100",
        "createdAt": "2026-01-15 10:30:00"
      }
    ],
    "total": 500,
    "page": 0,
    "size": 20
  }
}
```

### Messages

#### GET /messages

List user messages/notifications.

**Query Parameters:** `page`, `size`, `userId`, `isRead`

#### PUT /messages/{id}/read

Mark a message as read.

**Headers:** `X-User-Id: <userId>`

---

## Operation APIs

### Clusters

#### GET /clusters

List registered compute clusters.

**Query Parameters:** `page`, `size`, `status`

#### POST /clusters

Register a new cluster.

**Request:**
```json
{
  "name": "gpu-cluster-01",
  "type": "KUBERNETES",
  "endpoint": "https://k8s.example.com:6443",
  "config": "{ \"kubeconfig\": \"...\" }",
  "status": "ACTIVE"
}
```

#### GET /clusters/{id}/status

Get cluster status and health.

#### GET /clusters/{id}/nodes

Get cluster node information.

### Images

#### GET /images

List available container images.

**Query Parameters:** `page`, `size`, `type`, `name`

#### POST /images

Register a new image.

**Request:**
```json
{
  "name": "pytorch-2.1-cuda12",
  "type": "TRAINING",
  "url": "registry.example.com/pytorch:2.1-cuda12",
  "description": "PyTorch 2.1 with CUDA 12"
}
```

#### PUT /images/{id}

Update an image.

#### DELETE /images/{id}

Delete an image.

### Environments

#### GET /environments

List runtime environments.

**Query Parameters:** `page`, `size`, `imageId`

#### POST /environments

Create an environment.

**Request:**
```json
{
  "name": "ML Training Env",
  "imageId": 1,
  "envVars": "{\"PYTHONPATH\": \"/workspace\", \"CUDA_HOME\": \"/usr/local/cuda\"}",
  "description": "Standard ML training environment"
}
```

#### PUT /environments/{id}

Update an environment.

#### DELETE /environments/{id}

Delete an environment.

### Resource Pools

#### GET /resource-pools

List resource pools.

**Query Parameters:** `page`, `size`, `clusterId`

#### POST /resource-pools

Create a resource pool.

**Request:**
```json
{
  "name": "GPU Pool A",
  "clusterId": 1,
  "cpuTotal": 64,
  "memoryTotal": 256,
  "gpuTotal": 8,
  "cpuUsed": 0,
  "memoryUsed": 0,
  "gpuUsed": 0
}
```

#### PUT /resource-pools/{id}

Update a resource pool.

### Resource Quotas

#### GET /quotas

List resource quotas.

**Query Parameters:** `page`, `size`, `tenantId`, `projectId`

#### PUT /quotas/{id}

Update a resource quota.

**Request:**
```json
{
  "cpuLimit": 16,
  "memoryLimit": 64,
  "gpuLimit": 2,
  "storageLimit": 500
}
```

---

## Lab APIs

### Data Sources

#### GET /lab/datasources

List data sources.

**Query Parameters:** `page`, `size`, `type`, `tenantId`

#### GET /lab/datasources/{id}

Get data source details.

#### POST /lab/datasources

Create a data source.

**Request:**
```json
{
  "name": "Production DB",
  "type": "POSTGRESQL",
  "config": "{\"host\":\"db.example.com\",\"port\":5432,\"database\":\"prod\",\"username\":\"reader\",\"password\":\"***\"}",
  "tenantId": 1,
  "description": "Production database read replica"
}
```

#### PUT /lab/datasources/{id}

Update a data source.

#### DELETE /lab/datasources/{id}

Delete a data source.

#### POST /lab/datasources/{id}/test

Test data source connectivity.

**Response:** `{"code": 200, "data": { "success": true, "message": "Connection OK", "latencyMs": 23 }}`

### Datasets

#### GET /lab/datasets

List datasets.

**Query Parameters:** `page`, `size`, `tenantId`, `projectId`

#### GET /lab/datasets/{id}

Get dataset details.

#### POST /lab/datasets

Create a dataset (metadata only).

**Request:**
```json
{
  "name": "Training Set v2",
  "description": "Cleaned training data",
  "tenantId": 1,
  "projectId": 1,
  "format": "CSV",
  "rowCount": 50000,
  "columnCount": 25
}
```

#### POST /lab/datasets/upload

Upload a dataset file.

**Request:** `multipart/form-data` with fields: `tenantId`, `projectId`, `name`, `description`, `file`

#### GET /lab/datasets/{id}/preview

Preview dataset rows.

**Query Parameters:** `page`, `size`

#### GET /lab/datasets/{id}/stats

Get dataset column statistics.

**Query Parameters:** `version`

#### POST /lab/datasets/{id}/stats/compute

Compute dataset statistics.

**Query Parameters:** `version` (default: 1)

#### GET /lab/datasets/{id}/versions

List dataset versions.

#### PUT /lab/datasets/{id}

Update a dataset.

#### DELETE /lab/datasets/{id}

Delete a dataset.

### Experiments

#### GET /lab/experiments

List experiments.

**Query Parameters:** `page`, `size`, `tenantId`, `projectId`

#### GET /lab/experiments/{id}

Get experiment details.

#### GET /lab/experiments/{id}/metrics

Get experiment metrics.

#### POST /lab/experiments

Create an experiment.

**Request:**
```json
{
  "name": "XGBoost Tuning Run 3",
  "tenantId": 1,
  "projectId": 1,
  "description": "Learning rate 0.01, max_depth 8"
}
```

#### POST /lab/experiments/{id}/runs

Log a run under an experiment.

**Request:**
```json
{
  "runName": "xgb-lr001-md8",
  "params": {
    "learning_rate": "0.01",
    "max_depth": "8",
    "n_estimators": "500"
  },
  "metrics": {
    "accuracy": 0.9234,
    "f1_score": 0.8912,
    "auc": 0.9567
  }
}
```

### Feature Groups

#### GET /lab/feature-groups

List feature groups.

**Query Parameters:** `page`, `size`, `tenantId`

#### GET /lab/feature-groups/{id}

Get feature group details.

#### POST /lab/feature-groups

Create a feature group.

**Request:**
```json
{
  "name": "User Transaction Features",
  "tenantId": 1,
  "description": "Aggregated transaction features per user",
  "features": [
    { "name": "txn_count_7d", "dtype": "INTEGER", "description": "Transaction count in last 7 days" },
    { "name": "avg_amount_30d", "dtype": "DECIMAL", "description": "Average transaction amount in 30 days" }
  ]
}
```

#### PUT /lab/feature-groups/{id}

Update a feature group.

#### DELETE /lab/feature-groups/{id}

Delete a feature group.

### Operators

#### GET /lab/operators

List operators (pipeline components).

**Query Parameters:** `page`, `size`, `tenantId`, `category`, `type`

#### GET /lab/operators/{id}

Get operator details.

#### GET /lab/operators/presets

List built-in operator presets.

#### GET /lab/operators/{id}/versions

List operator versions.

#### POST /lab/operators

Create a custom operator.

**Request:**
```json
{
  "name": "Data Cleaner",
  "category": "PREPROCESSING",
  "type": "PYTHON",
  "tenantId": 1,
  "description": "Remove nulls and outliers",
  "config": "{\"script\": \"clean.py\", \"runtime\": \"python3.10\"}"
}
```

#### PUT /lab/operators/{id}

Update an operator.

#### DELETE /lab/operators/{id}

Delete an operator.

#### POST /lab/operators/{id}/test

Test an operator with sample parameters.

**Request:**
```json
{
  "inputData": "{\"col1\": [1, 2, null], \"col2\": [4, null, 6]}",
  "params": { "threshold": 3.0 }
}
```

### Workflows

#### GET /lab/workflows

List workflows (pipeline DAGs).

**Query Parameters:** `page`, `size`, `tenantId`, `projectId`

#### GET /lab/workflows/{id}

Get workflow definition.

#### POST /lab/workflows

Create a workflow.

**Request:**
```json
{
  "name": "ETL + Training Pipeline",
  "tenantId": 1,
  "projectId": 1,
  "description": "Extract, transform, and train model",
  "dagJson": "{\"nodes\":[...],\"edges\":[...]}"
}
```

#### PUT /lab/workflows/{id}

Update a workflow.

#### DELETE /lab/workflows/{id}

Delete a workflow.

#### POST /lab/workflows/{id}/run

Execute a workflow.

**Request (optional):**
```json
{
  "params": { "dataset_version": 2, "model_type": "xgboost" }
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "id": 42,
    "workflowId": 7,
    "status": "RUNNING",
    "startedAt": "2026-05-07 14:30:00"
  }
}
```

#### GET /lab/workflows/{id}/runs

List workflow run history.

**Query Parameters:** `page`, `size`

#### GET /lab/workflows/{id}/runs/{runId}/tasks

Get individual task statuses within a run.

### Model Archive

#### GET /lab/model-archives

List archived models.

**Query Parameters:** `page`, `size`, `tenantId`, `status`

#### GET /lab/model-archives/{id}

Get model archive details.

#### POST /lab/model-archives

Create a model archive entry.

**Request:**
```json
{
  "name": "Fraud Detection v2.1",
  "tenantId": 1,
  "projectId": 1,
  "framework": "PYTORCH",
  "version": "2.1.0",
  "description": "XGBoost model with improved recall"
}
```

#### PUT /lab/model-archives/{id}

Update a model archive.

#### DELETE /lab/model-archives/{id}

Delete a model archive.

#### POST /lab/model-archives/{id}/submit

Submit model for approval.

#### POST /lab/model-archives/{id}/approve

Approve or reject a model.

**Request:**
```json
{
  "approved": true,
  "comment": "Metrics look good, approved for production"
}
```

#### GET /lab/model-archives/{id}/files

List model files.

---

## Inference APIs

### Models

#### GET /api/v1/inference/models

List inference models.

**Query Parameters:** `page`, `size`, `keyword`

#### GET /api/v1/inference/models/{id}

Get model details.

#### GET /api/v1/inference/models/{id}/versions

List model versions.

#### POST /api/v1/inference/models/import

Import a model from Lab.

**Request:**
```json
{
  "labApiUrl": "http://10.0.0.3:8083",
  "modelId": 15,
  "modelName": "fraud-v2",
  "framework": "pytorch"
}
```

#### POST /api/v1/inference/models/upload

Upload a model file directly.

**Request:** `multipart/form-data` with fields: `file`, `modelName`, `framework`, `version`

#### POST /api/v1/inference/models/{id}/audit/approve

Approve a model for deployment.

**Request:**
```json
{ "remark": "Ready for canary deployment" }
```

#### POST /api/v1/inference/models/{id}/audit/reject

Reject a model.

**Request:**
```json
{ "remark": "AUC below threshold" }
```

#### POST /api/v1/inference/models/{id}/rollback

Rollback an online service to a previous model version.

**Request:**
```json
{
  "onlineServiceId": 3
}
```

### Online Services

#### POST /api/v1/inference/online/deploy

Deploy a model as an online prediction service.

**Request:**
```json
{
  "name": "fraud-detector-v2",
  "modelId": 15,
  "replicas": 2,
  "cpuCores": 2.0,
  "memoryMb": 4096,
  "releaseType": "canary"
}
```

#### GET /api/v1/inference/online/{id}

Get online service details.

#### PUT /api/v1/inference/online/{id}/config

Update service configuration (replicas, CPU, memory).

#### POST /api/v1/inference/online/{id}/stop

Stop an online service.

#### POST /api/v1/inference/online/{id}/predict

Send a prediction request.

**Request:**
```json
{
  "features": {
    "amount": 1500.00,
    "merchant_category": "travel",
    "hour_of_day": 14,
    "txn_count_7d": 3
  }
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "prediction": "FRAUD",
    "probability": 0.87,
    "modelId": 15,
    "latencyMs": 12
  }
}
```

#### PUT /api/v1/inference/online/{id}/release-type

Switch release type (canary/bluegreen).

**Request:**
```json
{ "releaseType": "bluegreen" }
```

### Batch Services

#### POST /api/v1/inference/batch

Create a batch prediction job.

**Request:**
```json
{
  "name": "Daily Scoring Batch",
  "modelId": 15,
  "inputPath": "s3://datasets/batch-input/2026-05-07.csv",
  "config": "{\"output_format\":\"CSV\",\"chunk_size\":1000}"
}
```

#### GET /api/v1/inference/batch/{id}

Get batch job details.

#### POST /api/v1/inference/batch/{id}/start

Start a batch job.

#### POST /api/v1/inference/batch/{id}/stop

Stop a running batch job.

#### GET /api/v1/inference/batch/{id}/results

Get download URL for batch results.

**Response:**
```json
{ "code": 200, "data": { "downloadUrl": "s3://batch-results/42/output.csv" } }
```

### Evaluation

#### GET /api/v1/inference/evaluation

List model evaluations.

**Query Parameters:** `modelId`, `page`, `size`

#### GET /api/v1/inference/evaluation/templates

Get available evaluation templates.

**Response:**
```json
{
  "code": 200,
  "data": [
    { "name": "binary_classification", "description": "Binary classification metrics (AUC, KS, Gini)" },
    { "name": "multi_class", "description": "Multi-class classification metrics" },
    { "name": "regression", "description": "Regression metrics (RMSE, MAE, R2)" },
    { "name": "detection", "description": "Object detection metrics (mAP, IoU)" },
    { "name": "custom", "description": "Custom evaluation script" }
  ]
}
```

#### POST /api/v1/inference/evaluation

Run a model evaluation.

**Request (binary classification):**
```json
{
  "modelId": 15,
  "modelName": "fraud-v2",
  "modelVersion": "2.1.0",
  "template": "binary_classification",
  "actualLabels": [1, 0, 1, 1, 0],
  "predictedScores": [0.91, 0.12, 0.85, 0.67, 0.34],
  "threshold": 0.5
}
```

#### GET /api/v1/inference/evaluation/{id}/report

Get evaluation report details.

### Drift Detection

#### GET /api/v1/inference/drift/reports

List drift detection reports.

**Query Parameters:** `modelId`, `driftType`, `page`, `size`

#### POST /api/v1/inference/drift/check

Run a drift detection check.

**Request (data drift — default):**
```json
{
  "modelId": 15,
  "modelName": "fraud-v2",
  "modelVersion": "2.1.0",
  "driftType": "DATA_DRIFT",
  "baseline": [1.2, 3.4, 5.6, 7.8, 9.0],
  "current": [1.5, 3.8, 6.1, 8.2, 10.5]
}
```

**Request (prediction drift):**
```json
{
  "modelId": 15,
  "modelName": "fraud-v2",
  "driftType": "PREDICTION_DRIFT",
  "baselineCounts": [500, 300, 200],
  "currentCounts": [450, 350, 200]
}
```

**Request (concept drift):**
```json
{
  "modelId": 15,
  "modelName": "fraud-v2",
  "driftType": "CONCEPT_DRIFT",
  "recentAccuracies": [0.95, 0.93, 0.91, 0.88, 0.82],
  "thresholdAccuracy": 0.85
}
```

#### GET /api/v1/inference/drift/trend/{modelId}

Get drift trend over time.

**Query Parameters:** `limit` (default: 10)

### Version Management

#### POST /api/v1/inference/versions/compare

Compare two model versions.

**Request:**
```json
{
  "modelIdA": 15,
  "modelIdB": 16
}
```

#### POST /api/v1/inference/versions/rollback

Rollback an online service to a target model version.

**Request:**
```json
{
  "onlineServiceId": 3,
  "targetModelId": 14
}
```

### Monitoring

#### GET /api/v1/inference/monitor/metrics

Get service metrics.

**Query Parameters:** `serviceId`, `page`, `size`

#### GET /api/v1/inference/monitor/logs

Get service invocation logs.

**Query Parameters:** `serviceId`, `page`, `size`

---

## FastLabel APIs

### Label Datasets

#### GET /api/v1/fastlabel/datasets

List labeling datasets.

**Query Parameters:** `page`, `size`, `type`, `keyword`

#### GET /api/v1/fastlabel/datasets/{id}

Get dataset details.

#### POST /api/v1/fastlabel/datasets

Create a labeling dataset.

**Request:**
```json
{
  "name": "Product Images v1",
  "type": "IMAGE",
  "description": "E-commerce product images for classification"
}
```

#### POST /api/v1/fastlabel/datasets/upload

Upload files to create a dataset.

**Request:** `multipart/form-data` with fields: `name`, `type`, `file`, `createdBy`

#### POST /api/v1/fastlabel/datasets/import

Import a dataset from Lab.

**Request:**
```json
{
  "name": "Lab Dataset Copy",
  "type": "IMAGE",
  "sourceDatasetId": 5,
  "createdBy": "admin"
}
```

#### DELETE /api/v1/fastlabel/datasets/{id}

Delete a labeling dataset.

### Label Tasks

#### GET /api/v1/fastlabel/tasks

List labeling tasks.

**Query Parameters:** `page`, `size`, `status`, `datasetId`

#### GET /api/v1/fastlabel/tasks/{id}

Get task details.

#### POST /api/v1/fastlabel/tasks

Create a labeling task.

**Request:**
```json
{
  "name": "Classify Product Images",
  "datasetId": 1,
  "type": "CLASSIFICATION",
  "labels": "[\"electronics\",\"clothing\",\"food\",\"toys\"]",
  "assignedTo": "labeler1"
}
```

#### PUT /api/v1/fastlabel/tasks/{id}/assign

Assign a task to a user.

**Request:**
```json
{ "assignedTo": "labeler2" }
```

#### PUT /api/v1/fastlabel/tasks/{id}/complete

Mark a task as completed.

#### GET /api/v1/fastlabel/tasks/{id}/items

Get items for a task.

**Query Parameters:** `page`, `size`

### Label Items

#### GET /api/v1/fastlabel/items

List label items.

**Query Parameters:** `taskId`, `status`

#### GET /api/v1/fastlabel/items/{id}

Get item detail (including data and existing annotation).

#### POST /api/v1/fastlabel/items/{id}/annotate

Submit an annotation for an item.

**Request:**
```json
{ "annotation": "{\"label\": \"electronics\", \"confidence\": 0.95}" }
```

#### POST /api/v1/fastlabel/items/{id}/review

Review an annotation (approve/reject).

**Request:**
```json
{
  "action": "APPROVE",
  "comment": "Correct label",
  "reviewedBy": "reviewer1"
}
```

#### GET /api/v1/fastlabel/items/task/{taskId}

Get all annotations for a task.

### Label Exports

#### GET /api/v1/fastlabel/exports

List exports.

**Query Parameters:** `taskId`, `status`

#### GET /api/v1/fastlabel/exports/{id}

Get export details.

#### POST /api/v1/fastlabel/exports

Create an export.

**Request:**
```json
{
  "taskId": 3,
  "format": "COCO_JSON",
  "exportedBy": "admin"
}
```

#### POST /api/v1/fastlabel/exports/{id}/push

Push exported labels to Lab.

**Response:**
```json
{ "code": 200, "data": { "labDatasetId": 12, "itemCount": 500 } }
```

---

## Scorecard APIs

### Variables

#### GET /api/v1/scorecard/variables

List scorecard variables.

**Query Parameters:** `projectId`

#### GET /api/v1/scorecard/variables/{id}

Get variable details with binning results.

#### POST /api/v1/scorecard/variables

Create a variable.

**Request:**
```json
{
  "name": "age",
  "displayName": "Applicant Age",
  "dtype": "INTEGER",
  "projectId": 1,
  "source": "APPLICATION",
  "description": "Applicant age in years"
}
```

#### GET /api/v1/scorecard/variables/{id}/analyze

Get variable analysis (IV, missing rate, predictiveness).

**Response:**
```json
{
  "code": 200,
  "data": {
    "variableId": 1,
    "variableName": "age",
    "dtype": "INTEGER",
    "ivValue": 0.2345,
    "missingRate": 0.02,
    "suggestion": "IV < 0.02: not predictive; 0.02-0.1: weak; 0.1-0.3: medium; > 0.3: strong"
  }
}
```

#### POST /api/v1/scorecard/variables/{id}/analyze

Run variable analysis (same response as GET).

#### DELETE /api/v1/scorecard/variables/{id}

Delete a variable.

### Binning

#### GET /api/v1/scorecard/binning/variable/{variableId}

List binning results for a variable.

#### POST /api/v1/scorecard/binning/variable/{variableId}

Create a manual binning entry.

**Request:**
```json
{
  "binLabel": "18-25",
  "lowerBound": 18.0,
  "upperBound": 25.0,
  "woe": -0.4125,
  "ivContribution": 0.0356
}
```

#### POST /api/v1/scorecard/binning/variable/{variableId}/auto

Run automatic binning (equal-width with WOE/IV calculation).

**Response:**
```json
{
  "code": 200,
  "data": {
    "variableId": 1,
    "method": "equal_width",
    "bins": [
      { "label": "bin_1", "woe": -0.5413, "ivContribution": 0.0412 },
      { "label": "bin_2", "woe": -0.2134, "ivContribution": 0.0198 },
      { "label": "bin_3", "woe": 0.0842, "ivContribution": 0.0067 },
      { "label": "bin_4", "woe": 0.3287, "ivContribution": 0.0301 },
      { "label": "bin_5", "woe": 0.6124, "ivContribution": 0.0523 }
    ],
    "totalIv": 0.1501
  }
}
```

#### POST /api/v1/scorecard/binning/auto

Auto-bin by variable ID (request body).

**Request:**
```json
{ "variableId": 1 }
```

### Scorecard Models

#### GET /api/v1/scorecard/models

List scorecard models.

**Query Parameters:** `projectId`

#### GET /api/v1/scorecard/models/{id}

Get model details with associated scoring rules.

#### POST /api/v1/scorecard/models

Create a scorecard model.

**Request:**
```json
{
  "name": "Credit Scoring Model v1",
  "projectId": 1,
  "description": "Logistic regression scorecard for credit risk",
  "baseScore": 600,
  "pdo": 20
}
```

#### POST /api/v1/scorecard/models/{id}/train

Train a scorecard model (computes KS, AUC, Gini, PSI).

**Response:**
```json
{
  "code": 200,
  "data": {
    "modelId": 1,
    "modelName": "Credit Scoring Model v1",
    "status": "TRAINED",
    "metrics": {
      "ks": 0.4213,
      "auc": 0.7856,
      "gini": 0.5712,
      "psi": 0.0834
    },
    "coefficients": {
      "variable_1": 0.8523,
      "variable_2": -0.3412,
      "variable_3": 0.1978
    },
    "intercept": -1.2345
  }
}
```

#### GET /api/v1/scorecard/models/{id}/report

Get model assessment report.

**Response:**
```json
{
  "code": 200,
  "data": {
    "modelId": 1,
    "modelName": "Credit Scoring Model v1",
    "status": "TRAINED",
    "metrics": { "ks": 0.4213, "auc": 0.7856, "gini": 0.5712, "psi": 0.0834 },
    "assessment": {
      "discrimination": "GOOD",
      "accuracy": "GOOD"
    }
  }
}
```

#### DELETE /api/v1/scorecard/models/{id}

Delete a scorecard model.

### Scoring Rules

#### GET /api/v1/scorecard/rules/model/{modelId}

List scoring rules for a model.

#### POST /api/v1/scorecard/rules

Create a standalone scoring rule.

**Request:**
```json
{
  "modelId": 1,
  "variableId": 1,
  "binLabel": "18-25",
  "points": 15.0,
  "baseScore": 600,
  "pdo": 20
}
```

#### POST /api/v1/scorecard/rules/model/{modelId}

Create a scoring rule for a specific model.

### Scoring

#### POST /api/v1/scorecard/scoring/{modelId}

Execute scoring for a given model.

**Request:**
```json
{
  "age": 30,
  "income": 75000,
  "credit_history_months": 48,
  "debt_ratio": 0.35
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "score": 660,
    "modelId": 1,
    "details": {
      "baseScore": 600,
      "pdo": 20,
      "variableScores": [
        { "variable": "age", "inputValue": 30, "points": 15 },
        { "variable": "income", "inputValue": 75000, "points": 15 },
        { "variable": "credit_history_months", "inputValue": 48, "points": 15 },
        { "variable": "debt_ratio", "inputValue": 0.35, "points": 15 }
      ],
      "totalScore": 660
    }
  }
}
```

#### GET /api/v1/scorecard/scoring/{modelId}/results

Get scoring results history.
