import React, { useState, useEffect } from 'react';
import { Tabs, TabPane } from 'antd';
import { Form, Input, InputNumber, Button, Select, Table, message, Card, Descriptions, Progress, Space } from 'antd';
import { onlineScore, batchScore, deployToInference, getScoringRules } from '@/services/scorecard';

const { Option } = Select;

export default function ScoringPage() {
  const [rules, setRules] = useState<any[]>([]);
  const [scoreResult, setScoreResult] = useState<any>(null);
  const [scoringLoading, setScoringLoading] = useState(false);
  const [onlineForm] = Form.useForm();

  const [batchModelId, setBatchModelId] = useState<string>('');
  const [batchDatasetId, setBatchDatasetId] = useState<string>('');
  const [batchProgress, setBatchProgress] = useState(0);
  const [batchRunning, setBatchRunning] = useState(false);

  useEffect(function () {
    getScoringRules()
      .then(function (res) { setRules(res.data.data?.variables || []); })
      .catch(function () {});
  }, []);

  function handleOnlineScore(values: any) {
    setScoringLoading(true);
    setScoreResult(null);
    onlineScore(values)
      .then(function (res) {
        setScoreResult(res.data.data);
        message.success('Score computed');
      })
      .catch(function () { message.error('Scoring failed'); })
      .finally(function () { setScoringLoading(false); });
  }

  function handleBatchScore() {
    if (!batchModelId || !batchDatasetId) { message.warning('Select model and dataset'); return; }
    setBatchRunning(true);
    setBatchProgress(0);
    batchScore({ modelId: batchModelId, datasetId: batchDatasetId })
      .then(function () {
        setBatchProgress(100);
        message.success('Batch scoring completed');
      })
      .catch(function () { message.error('Batch scoring failed'); })
      .finally(function () { setBatchRunning(false); });
  }

  function handleDeploy() {
    if (!batchModelId) { message.warning('Select a model first'); return; }
    deployToInference(batchModelId)
      .then(function () { message.success('Deployed to inference'); })
      .catch(function () { message.error('Deploy failed'); });
  }

  return (
    <div>
      <h2>Scoring</h2>
      <Tabs defaultActiveKey="online">
        <TabPane tab="Online Scoring" key="online">
          <Card style={{ maxWidth: 800 }}>
            <Form form={onlineForm} layout="vertical" onFinish={handleOnlineScore}>
              {rules.map(function (rule) {
                return (
                  <Form.Item key={rule.name} name={rule.name} label={rule.label || rule.name}>
                    <InputNumber style={{ width: '100%' }} placeholder={rule.label || rule.name} />
                  </Form.Item>
                );
              })}
              {rules.length === 0 ? <p>No variables loaded. Select a model first.</p> : null}
              <Form.Item>
                <Space>
                  <Button type="primary" htmlType="submit" loading={scoringLoading}>Score</Button>
                  <Button onClick={handleDeploy}>Deploy to Inference</Button>
                </Space>
              </Form.Item>
            </Form>
            {scoreResult ? (
              <Card title="Scoring Result" style={{ marginTop: 16 }}>
                <Descriptions bordered column={1}>
                  <Descriptions.Item label="Score">{scoreResult.score}</Descriptions.Item>
                  <Descriptions.Item label="Risk Level">{scoreResult.riskLevel}</Descriptions.Item>
                </Descriptions>
                {scoreResult.points ? (
                  <Table
                    style={{ marginTop: 16 }}
                    rowKey="variable"
                    columns={[
                      { title: 'Variable', dataIndex: 'variable', key: 'variable' },
                      { title: 'Bin', dataIndex: 'bin', key: 'bin' },
                      { title: 'Points', dataIndex: 'points', key: 'points' },
                    ]}
                    dataSource={scoreResult.points}
                    pagination={false}
                    size="small"
                  />
                ) : null}
              </Card>
            ) : null}
          </Card>
        </TabPane>
        <TabPane tab="Batch Scoring" key="batch">
          <Card style={{ maxWidth: 600 }}>
            <div style={{ marginBottom: 16 }}>
              <label>Model:</label>
              <Select value={batchModelId} onChange={setBatchModelId} style={{ width: '100%' }} placeholder="Select model">
                <Option value="">Select</Option>
              </Select>
            </div>
            <div style={{ marginBottom: 16 }}>
              <label>Dataset:</label>
              <Select value={batchDatasetId} onChange={setBatchDatasetId} style={{ width: '100%' }} placeholder="Select dataset">
                <Option value="">Select</Option>
              </Select>
            </div>
            {batchRunning ? <Progress percent={batchProgress} style={{ marginBottom: 16 }} /> : null}
            <Space>
              <Button type="primary" onClick={handleBatchScore} loading={batchRunning}>Run Batch</Button>
              <Button onClick={handleDeploy}>Deploy to Inference</Button>
            </Space>
          </Card>
        </TabPane>
      </Tabs>
    </div>
  );
}
