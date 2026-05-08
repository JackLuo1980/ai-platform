import { Routes, Route, Navigate } from 'react-router-dom';
import BasicLayout from './layouts/BasicLayout';
import LoginPage from './pages/login/LoginPage';
import TenantList from './pages/console/TenantList';
import UserList from './pages/console/UserList';
import RoleList from './pages/console/RoleList';
import ProjectList from './pages/console/ProjectList';
import AuditLogList from './pages/console/AuditLogList';
import MessageList from './pages/console/MessageList';
import ClusterList from './pages/operation/ClusterList';
import ResourcePoolList from './pages/operation/ResourcePoolList';
import QuotaList from './pages/operation/QuotaList';
import ImageList from './pages/operation/ImageList';
import EnvironmentList from './pages/operation/EnvironmentList';
import DataSourceList from './pages/lab/DataSourceList';
import DatasetList from './pages/lab/DatasetList';
import DataPreview from './pages/lab/DataPreview';
import OperatorList from './pages/lab/OperatorList';
import WorkflowList from './pages/lab/WorkflowList';
import WorkflowEditor from './pages/lab/WorkflowEditor';
import ExperimentList from './pages/lab/ExperimentList';
import ModelArchiveList from './pages/lab/ModelArchiveList';
import FeatureGroupList from './pages/lab/FeatureGroupList';
import FileManager from './pages/lab/FileManager';
import ModelList from './pages/inference/ModelList';
import OnlineServiceList from './pages/inference/OnlineServiceList';
import BatchServiceList from './pages/inference/BatchServiceList';
import MonitoringDashboard from './pages/inference/MonitoringDashboard';
import AutoMLPage from './pages/lab/AutoMLPage';
import OperationMonitoring from './pages/operation/MonitoringDashboard';
import EvaluationList from './pages/inference/EvaluationList';
import DriftDashboard from './pages/inference/DriftDashboard';
import DeploymentPage from './pages/inference/DeploymentPage';
import DataBackflowPage from './pages/inference/DataBackflowPage';
import LabelDatasetList from './pages/fastlabel/LabelDatasetList';
import TaskList from './pages/fastlabel/TaskList';
import AnnotationPage from './pages/fastlabel/AnnotationPage';
import ExportList from './pages/fastlabel/ExportList';
import VariableList from './pages/scorecard/VariableList';
import BinningDetail from './pages/scorecard/BinningDetail';
import ScorecardModelList from './pages/scorecard/ModelList';
import ScoringPage from './pages/scorecard/ScoringPage';
import LicensePage from './pages/console/LicensePage';

function AuthGuard({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/login" replace />;
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/*"
        element={
          <AuthGuard>
            <BasicLayout />
          </AuthGuard>
        }
      >
        <Route index element={<Navigate to="/console/tenants" replace />} />
        <Route path="console/tenants" element={<TenantList />} />
        <Route path="console/users" element={<UserList />} />
        <Route path="console/roles" element={<RoleList />} />
        <Route path="console/projects" element={<ProjectList />} />
        <Route path="console/audit-logs" element={<AuditLogList />} />
        <Route path="console/messages" element={<MessageList />} />
        <Route path="operation/clusters" element={<ClusterList />} />
        <Route path="operation/pools" element={<ResourcePoolList />} />
        <Route path="operation/quotas" element={<QuotaList />} />
        <Route path="operation/images" element={<ImageList />} />
        <Route path="operation/environments" element={<EnvironmentList />} />
        <Route path="operation/monitoring" element={<OperationMonitoring />} />
        <Route path="lab/datasources" element={<DataSourceList />} />
        <Route path="lab/datasets" element={<DatasetList />} />
        <Route path="lab/datasets/:id" element={<DataPreview />} />
        <Route path="lab/operators" element={<OperatorList />} />
        <Route path="lab/workflows" element={<WorkflowList />} />
        <Route path="lab/workflows/:id" element={<WorkflowEditor />} />
        <Route path="lab/experiments" element={<ExperimentList />} />
        <Route path="lab/archives" element={<ModelArchiveList />} />
        <Route path="lab/features" element={<FeatureGroupList />} />
        <Route path="lab/files" element={<FileManager />} />
        <Route path="lab/automl" element={<AutoMLPage />} />
        <Route path="inference/models" element={<ModelList />} />
        <Route path="inference/online-services" element={<OnlineServiceList />} />
        <Route path="inference/batch-services" element={<BatchServiceList />} />
        <Route path="inference/monitoring" element={<MonitoringDashboard />} />
        <Route path="inference/evaluations" element={<EvaluationList />} />
        <Route path="inference/drift" element={<DriftDashboard />} />
        <Route path="inference/backflow" element={<DataBackflowPage />} />
        <Route path="fastlabel/datasets" element={<LabelDatasetList />} />
        <Route path="fastlabel/tasks" element={<TaskList />} />
        <Route path="fastlabel/tasks/:id/annotate" element={<AnnotationPage />} />
        <Route path="fastlabel/exports" element={<ExportList />} />
        <Route path="scorecard/variables" element={<VariableList />} />
        <Route path="scorecard/binning/:id" element={<BinningDetail />} />
        <Route path="scorecard/models" element={<ScorecardModelList />} />
        <Route path="scorecard/scoring" element={<ScoringPage />} />
      </Route>
    </Routes>
  );
}

export default App;
