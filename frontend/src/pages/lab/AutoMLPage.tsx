import { useState } from 'react'
interface Trial {
  key: number
  trial: number
  algorithm: string
  algoValue: string
  hyperparams: string
  accuracy: string
  auc: string
  f1: string
  time: string
  isBest: boolean
}
import { Card, Button, Select, InputNumber, Table, Tag, Space, Progress, message, Statistic, Row, Col, Divider, Descriptions } from 'antd'
import { ThunderboltOutlined, TrophyOutlined, RocketOutlined } from '@ant-design/icons'

export default function AutoMLPage() {
  const [running, setRunning] = useState(false)
  const [progress, setProgress] = useState(0)
  const [results, setResults] = useState<Trial[]>([])
  const [bestModel, setBestModel] = useState<Trial | null>(null)

  const algorithms = [
    { value: 'rf', label: '随机森林' },
    { value: 'xgb', label: 'XGBoost' },
    { value: 'lr', label: '逻辑回归' },
    { value: 'svm', label: 'SVM' },
    { value: 'nn', label: '神经网络' },
  ]

  function runAutoML() {
    setRunning(true)
    setProgress(0)
    setResults([])

    const trials: Trial[] = []
    const totalTrials = 15
    let best: Trial | null = null
    let bestAcc = 0

    let i = 0
    const interval = setInterval(() => {
      const algo = algorithms[Math.floor(Math.random() * algorithms.length)]
      const acc = 0.78 + Math.random() * 0.18
      const auc = acc + (Math.random() - 0.5) * 0.05
      const f1 = acc - (Math.random() * 0.05)
      const time = (Math.random() * 120 + 10).toFixed(1)

      const trial = {
        key: i,
        trial: i + 1,
        algorithm: algo.label,
        algoValue: algo.value,
        hyperparams: generateHyperparams(algo.value),
        accuracy: acc.toFixed(4),
        auc: auc.toFixed(4),
        f1: f1.toFixed(4),
        time: time + 's',
        isBest: false,
      }

      if (acc > bestAcc) {
        if (best) best.isBest = false
        trial.isBest = true
        bestAcc = acc
        best = { ...trial, accuracy: acc.toFixed(4), auc: auc.toFixed(4), f1: f1.toFixed(4) }
      }

      trials.push(trial)
      setResults([...trials])
      setProgress(Math.round(((i + 1) / totalTrials) * 100))
      setBestModel(best)

      i++
      if (i >= totalTrials) {
        clearInterval(interval)
        setRunning(false)
        message.success('AutoML 搜索完成')
      }
    }, 500)
  }

  function generateHyperparams(algo: string) {
    switch (algo) {
      case 'rf': return `n_estimators=${Math.floor(Math.random()*400+100)}, max_depth=${Math.floor(Math.random()*15+5)}`
      case 'xgb': return `learning_rate=${(Math.random()*0.3+0.01).toFixed(3)}, max_depth=${Math.floor(Math.random()*10+3)}`
      case 'lr': return `C=${(Math.random()*10+0.1).toFixed(2)}, penalty=l2`
      case 'svm': return `C=${(Math.random()*5+0.5).toFixed(2)}, kernel=rbf`
      case 'nn': return `layers=${Math.floor(Math.random()*3+2)}, units=${Math.floor(Math.random()*128+32)}`
      default: return '-'
    }
  }

  const columns = [
    { title: '#', dataIndex: 'trial', key: 'trial', width: 50 },
    { title: '算法', dataIndex: 'algorithm', key: 'algorithm', render: function (t: string, r: Trial) { return r.isBest ? <Tag color="gold">{t} ★</Tag> : <Tag>{t}</Tag> } },
    { title: '超参数', dataIndex: 'hyperparams', key: 'hyperparams', ellipsis: true },
    { title: '准确率', dataIndex: 'accuracy', key: 'accuracy', sorter: function (a: Trial, b: Trial) { return Number(a.accuracy) - Number(b.accuracy) } },
    { title: 'AUC', dataIndex: 'auc', key: 'auc' },
    { title: 'F1', dataIndex: 'f1', key: 'f1' },
    { title: '耗时', dataIndex: 'time', key: 'time' },
  ]

  return (
    <div>
      <Card title="AutoML 自动模型搜索">
        <Space wrap style={{ marginBottom: 16 }}>
          <Select placeholder="选择数据集" style={{ width: 200 }} options={[{ value: 1, label: '数据集 1' }, { value: 2, label: '数据集 2' }]} />
          <Select placeholder="目标列" style={{ width: 150 }} options={[{ value: 'target', label: 'target' }]} />
          <InputNumber addonBefore="最大搜索次数" min={5} max={100} defaultValue={15} />
          <InputNumber addonBefore="超时(分钟)" min={1} max={120} defaultValue={30} />
          <Button type="primary" icon={<ThunderboltOutlined />} onClick={runAutoML} loading={running} disabled={running}>
            {running ? '搜索中...' : '开始搜索'}
          </Button>
        </Space>
        {running || progress > 0 ? <Progress percent={progress} style={{ marginBottom: 16 }} /> : null}
      </Card>

      {bestModel && !running ? (
        <Card title={<Space><TrophyOutlined /> 最优模型</Space>} style={{ marginTop: 16 }}>
          <Row gutter={16}>
            <Col span={6}><Statistic title="算法" value={bestModel.algorithm} /></Col>
            <Col span={6}><Statistic title="准确率" value={bestModel.accuracy} /></Col>
            <Col span={6}><Statistic title="AUC" value={bestModel.auc} /></Col>
            <Col span={6}><Statistic title="F1" value={bestModel.f1} /></Col>
          </Row>
          <Divider />
          <Descriptions column={2} size="small">
            <Descriptions.Item label="超参数">{bestModel.hyperparams}</Descriptions.Item>
            <Descriptions.Item label="耗时">{bestModel.time}</Descriptions.Item>
          </Descriptions>
          <Button type="primary" icon={<RocketOutlined />} style={{ marginTop: 12 }} onClick={function () { message.success('模型已部署到推理服务') }}>部署最优模型</Button>
        </Card>
      ) : null}

      <Card title={`搜索结果 (${results.length} 次)`} style={{ marginTop: 16 }}>
        <Table columns={columns} dataSource={results} size="small" pagination={false} />
      </Card>
    </div>
  )
}
