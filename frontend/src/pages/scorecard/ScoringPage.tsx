import { useState, useEffect } from 'react';
import { Tabs, Form, Input, InputNumber, Button, Select, Table, message, Card, Descriptions, Space } from 'antd';
import { onlineScore, batchScore, deployToInference, getScoringRules } from '@/services/scorecard';

function ScoringPage() {
  const [rules, setRules] = useState<Record<string, unknown>[]>([]);
  const [scoreResult, setScoreResult] = useState<Record<string, unknown> | null>(null);
  const [scoringLoading, setScoringLoading] = useState(false);
  const [onlineForm] = Form.useForm();
  const [batchModelId, setBatchModelId] = useState<string>('');
  const [batchDatasetId, setBatchDatasetId] = useState<string>('');
  const [batchRunning, setBatchRunning] = useState(false);

  useEffect(function () {
    getScoringRules()
      .then(function (res: any) {
        const d = res?.data || res || {};
        setRules(d.variables || d.items || []);
      })
      .catch(function () {});
  }, []);

  function handleOnlineScore(values: Record<string, unknown>) {
    setScoringLoading(true);
    setScoreResult(null);
    onlineScore(values)
      .then(function (res: any) {
        setScoreResult(res?.data || res || null);
        message.success('评分完成');
      })
      .catch(function () { message.error('评分失败'); })
      .finally(function () { setScoringLoading(false); });
  }

  function handleBatchScore() {
    if (!batchModelId || !batchDatasetId) { message.warning('请选择模型和数据集'); return; }
    setBatchRunning(true);
    batchScore({ modelId: batchModelId, datasetId: batchDatasetId })
      .then(function () {
        message.success('批量评分已完成');
      })
      .catch(function () { message.error('批量评分失败'); })
      .finally(function () { setBatchRunning(false); });
  }

  function handleDeploy() {
    if (!batchModelId) { message.warning('请先选择模型'); return; }
    deployToInference(batchModelId)
      .then(function () { message.success('已部署到推理'); })
      .catch(function () { message.error('部署失败'); });
  }

  return (
    <div>
      <h2>评分</h2>
      <Tabs defaultActiveKey="online" items={[
        {
          key: 'online',
          label: '在线评分',
          children: (
            <Card style={{ maxWidth: 800 }}>
              <Form form={onlineForm} layout="vertical" onFinish={handleOnlineScore}>
                {rules.map(function (rule: any) {
                  return (
                    <Form.Item key={rule.name} name={rule.name} label={rule.label || rule.name}>
                      <InputNumber style={{ width: '100%' }} placeholder={rule.label || rule.name} />
                    </Form.Item>
                  );
                })}
                {rules.length === 0 ? <p>未加载变量，请先选择模型。</p> : null}
                <Form.Item>
                  <Space>
                    <Button type="primary" htmlType="submit" loading={scoringLoading}>评分</Button>
                    <Button onClick={handleDeploy}>部署到推理</Button>
                  </Space>
                </Form.Item>
              </Form>
              {scoreResult ? (
                <Card title="评分结果" style={{ marginTop: 16 }}>
                  <Descriptions bordered column={1}>
                    <Descriptions.Item label="分数">{String(scoreResult.score)}</Descriptions.Item>
                    <Descriptions.Item label="风险等级">{String(scoreResult.riskLevel)}</Descriptions.Item>
                  </Descriptions>
                  {scoreResult.points ? (
                    <Table
                      style={{ marginTop: 16 }}
                      rowKey="variable"
                      columns={[
                        { title: '变量', dataIndex: 'variable' },
                        { title: '分箱', dataIndex: 'bin' },
                        { title: '得分', dataIndex: 'points' },
                      ]}
                      dataSource={scoreResult.points as Record<string, unknown>[]}
                      pagination={false}
                      size="small"
                    />
                  ) : null}
                </Card>
              ) : null}
            </Card>
          ),
        },
        {
          key: 'batch',
          label: '批量评分',
          children: (
            <Card style={{ maxWidth: 600 }}>
              <div style={{ marginBottom: 16 }}>
                <label>模型:</label>
                <Select value={batchModelId || undefined} onChange={setBatchModelId} style={{ width: '100%' }} placeholder="选择模型" />
              </div>
              <div style={{ marginBottom: 16 }}>
                <label>数据集:</label>
                <Select value={batchDatasetId || undefined} onChange={setBatchDatasetId} style={{ width: '100%' }} placeholder="选择数据集" />
              </div>
              <Space>
                <Button type="primary" onClick={handleBatchScore} loading={batchRunning}>执行批量评分</Button>
                <Button onClick={handleDeploy}>部署到推理</Button>
              </Space>
            </Card>
          ),
        },
      ]} />
    </div>
  );
}

export default ScoringPage;
