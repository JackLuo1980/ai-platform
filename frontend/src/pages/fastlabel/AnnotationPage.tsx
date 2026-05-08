import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Form, Input, Select, message, Space, Card, Slider, Popconfirm, Tag } from 'antd';
import { LeftOutlined, RightOutlined, CheckOutlined, CloseOutlined, PlusOutlined, DeleteOutlined, PlayCircleOutlined, PauseCircleOutlined } from '@ant-design/icons';
import { getItem, submitAnnotation, reviewItem } from '@/services/fastlabel';

function formatTime(seconds: number) {
  var m = Math.floor(seconds / 60);
  var s = Math.floor(seconds % 60);
  var ms = Math.floor((seconds % 1) * 10);
  return m + ':' + String(s).padStart(2, '0') + '.' + ms;
}

function AnnotationPage() {
  const { taskId } = useParams<{ taskId: string }>();
  const navigate = useNavigate();
  const [item, setItem] = useState<Record<string, unknown> | null>(null);
  const [loading, setLoading] = useState(false);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [form] = Form.useForm();
  const audioRef = useRef<HTMLAudioElement>(null);
  const [segments, setSegments] = useState<Array<{ start: number; end: number; label: string; text?: string }>>([]);
  const [audioDuration, setAudioDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [segStart, setSegStart] = useState(0);
  const [segEnd, setSegEnd] = useState(0);
  const [segLabel, setSegLabel] = useState('');
  const [segText, setSegText] = useState('');
  const [playing, setPlaying] = useState(false);

  function fetchItem(index: number) {
    if (!taskId) return;
    setLoading(true);
    getItem(taskId, { index })
      .then(function (res: any) {
        const data = res?.data || res || {};
        setItem(data);
        setTotalItems(data.total || 0);
        setCurrentIndex(index);
        form.resetFields();
        setSegments([]);
        setAudioDuration(0);
        setCurrentTime(0);
        setSegStart(0);
        setSegEnd(0);
        if (data.annotationJson) {
          try {
            var ann = typeof data.annotationJson === 'string' ? JSON.parse(data.annotationJson) : data.annotationJson;
            if (ann.segments) { setSegments(ann.segments); }
          } catch (e) { /* ignore */ }
        }
      })
      .catch(function () { message.error('加载失败'); })
      .finally(function () { setLoading(false); });
  }

  useEffect(function () { fetchItem(0); }, [taskId]);

  function handleSubmit() {
    if (!item) return;
    var annotationJson: Record<string, unknown> = {};
    var type = item.type as string;
    if (type === 'audio') {
      annotationJson = { segments: segments };
    } else {
      annotationJson = { labels: form.getFieldsValue() };
    }
    submitAnnotation(item.id as string, annotationJson)
      .then(function () {
        message.success('标注已保存');
        if (currentIndex < totalItems - 1) { fetchItem(currentIndex + 1); }
        else { message.info('所有数据已完成'); }
      })
      .catch(function () { message.error('保存失败'); });
  }

  function handleReview(action: 'approved' | 'rejected') {
    if (!item) return;
    reviewItem(item.id as string, { action })
      .then(function () {
        message.success(action === 'approved' ? '已通过' : '已拒绝');
        if (currentIndex < totalItems - 1) { fetchItem(currentIndex + 1); }
      })
      .catch(function () { message.error('操作失败'); });
  }

  function addSegment() {
    if (!segLabel) { message.warning('请输入标签'); return; }
    if (segEnd <= segStart) { message.warning('结束时间必须大于开始时间'); return; }
    setSegments(function (prev) { return prev.concat([{ start: segStart, end: segEnd, label: segLabel, text: segText }]); });
    setSegStart(segEnd);
    setSegEnd(Math.min(segEnd + 5, audioDuration));
    setSegLabel('');
    setSegText('');
  }

  function removeSegment(idx: number) {
    setSegments(function (prev) { return prev.filter(function (_, i) { return i !== idx; }); });
  }

  function playSegment(seg: { start: number; end: number }) {
    if (audioRef.current) {
      audioRef.current.currentTime = seg.start;
      audioRef.current.play();
      setPlaying(true);
    }
  }

  var onTimeUpdate = useCallback(function () {
    if (!audioRef.current) return;
    setCurrentTime(audioRef.current.currentTime);
    if (audioRef.current.currentTime >= audioDuration && audioDuration > 0) {
      setPlaying(false);
    }
  }, [audioDuration]);

  var onLoadedMetadata = useCallback(function () {
    if (audioRef.current) { setAudioDuration(audioRef.current.duration); }
  }, []);

  function togglePlay() {
    if (!audioRef.current) return;
    if (playing) { audioRef.current.pause(); setPlaying(false); }
    else { audioRef.current.play(); setPlaying(true); }
  }

  function renderContent() {
    if (!item) return null;
    var type = item.type as string;
    if (type === 'image') {
      return (
        <div style={{ textAlign: 'center', padding: 24, background: '#fafafa', borderRadius: 8, minHeight: 400, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <img src={item.url as string} alt="" style={{ maxWidth: '100%', maxHeight: 500, objectFit: 'contain' }} />
        </div>
      );
    }
    if (type === 'text') {
      return (
        <div style={{ padding: 24, background: '#fafafa', borderRadius: 8, minHeight: 400, fontSize: 14, lineHeight: 1.8, whiteSpace: 'pre-wrap' }}>
          {(item.content as string) || ''}
        </div>
      );
    }
    if (type === 'audio') {
      var url = (item.dataUrl || item.url) as string;
      return (
        <div style={{ padding: 24, background: '#fafafa', borderRadius: 8 }}>
          <audio ref={audioRef} src={url} onTimeUpdate={onTimeUpdate} onLoadedMetadata={onLoadedMetadata} onEnded={function () { setPlaying(false); }} style={{ width: '100%' }} controls />
          <div style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 8 }}>
              <Button icon={playing ? <PauseCircleOutlined /> : <PlayCircleOutlined />} onClick={togglePlay} />
              <Slider min={0} max={audioDuration || 100} step={0.1} value={currentTime} onChange={function (v) { if (audioRef.current) { audioRef.current.currentTime = v; setCurrentTime(v); } }} style={{ flex: 1 }} tooltipFormatter={function (v) { return formatTime(v || 0); }} />
              <span style={{ fontSize: 12, color: '#666', minWidth: 100 }}>{formatTime(currentTime)} / {formatTime(audioDuration)}</span>
            </div>
            <div style={{ position: 'relative', height: 32, background: '#e8e8e8', borderRadius: 4, marginBottom: 12, overflow: 'hidden' }}>
              {segments.map(function (seg, idx) {
                var left = audioDuration > 0 ? (seg.start / audioDuration) * 100 : 0;
                var width = audioDuration > 0 ? ((seg.end - seg.start) / audioDuration) * 100 : 0;
                return (
                  <div key={idx} onClick={function () { playSegment(seg); }} style={{ position: 'absolute', left: left + '%', width: width + '%', top: 0, bottom: 0, background: '#1890ff', opacity: 0.6, cursor: 'pointer', borderRadius: 2, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 10, color: '#fff', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', padding: '0 2px' }}>
                    {seg.label}
                  </div>
                );
              })}
              {audioDuration > 0 ? (
                <div style={{ position: 'absolute', left: (currentTime / audioDuration) * 100 + '%', top: 0, bottom: 0, width: 2, background: '#ff4d4f', zIndex: 1 }} />
              ) : null}
            </div>
            <div style={{ borderTop: '1px solid #f0f0f0', paddingTop: 12 }}>
              <div style={{ marginBottom: 8, fontWeight: 500 }}>已标注片段 ({segments.length})</div>
              {segments.length === 0 ? <div style={{ color: '#999', fontSize: 12 }}>暂无标注片段，请使用下方表单添加</div> : null}
              {segments.map(function (seg, idx) {
                return (
                  <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 0', borderBottom: '1px solid #f5f5f5' }}>
                    <span style={{ fontSize: 12, color: '#666', minWidth: 120 }}>{formatTime(seg.start)} - {formatTime(seg.end)}</span>
                    <Tag color="blue">{seg.label}</Tag>
                    {seg.text ? <span style={{ fontSize: 12, color: '#333' }}>{seg.text}</span> : null}
                    <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={function () { playSegment(seg); }} />
                    <Popconfirm title="确定删除?" onConfirm={function () { removeSegment(idx); }}>
                      <Button type="link" size="small" danger icon={<DeleteOutlined />} />
                    </Popconfirm>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      );
    }
    return <div style={{ padding: 24, background: '#fafafa', borderRadius: 8 }}>不支持的数据类型</div>;
  }

  function renderAnnotationPanel() {
    if (!item) return null;
    var type = item.type as string;
    if (type === 'audio') {
      return (
        <Card title="音频标注" loading={loading}>
          <div style={{ marginBottom: 12 }}>
            <div style={{ marginBottom: 4, fontSize: 13 }}>开始时间: {formatTime(segStart)}</div>
            <Slider min={0} max={audioDuration || 100} step={0.1} value={segStart} onChange={setSegStart} tooltipFormatter={function (v) { return formatTime(v || 0); }} />
          </div>
          <div style={{ marginBottom: 12 }}>
            <div style={{ marginBottom: 4, fontSize: 13 }}>结束时间: {formatTime(segEnd)}</div>
            <Slider min={0} max={audioDuration || 100} step={0.1} value={segEnd} onChange={setSegEnd} tooltipFormatter={function (v) { return formatTime(v || 0); }} />
          </div>
          <Input placeholder="标签 (必填)" value={segLabel} onChange={function (e) { setSegLabel(e.target.value); }} style={{ marginBottom: 8 }} />
          <Input.TextArea placeholder="转写文本 (可选)" value={segText} onChange={function (e) { setSegText(e.target.value); }} rows={2} style={{ marginBottom: 8 }} />
          <Button type="dashed" icon={<PlusOutlined />} onClick={addSegment} style={{ width: '100%', marginBottom: 16 }}>添加片段</Button>
          <Space style={{ width: '100%', justifyContent: 'center' }}>
            <Button type="primary" onClick={handleSubmit}>提交并下一条</Button>
            <Button icon={<CheckOutlined />} style={{ background: '#52c41a', color: '#fff' }} onClick={function () { handleReview('approved'); }}>通过</Button>
            <Button danger icon={<CloseOutlined />} onClick={function () { handleReview('rejected'); }}>拒绝</Button>
          </Space>
        </Card>
      );
    }
    return (
      <Card title="标注" loading={loading}>
        <Form form={form} layout="vertical">
          <Form.Item name="category" label="分类">
            <Select placeholder="选择分类" mode="multiple">
              <Select.Option value="positive">正面</Select.Option>
              <Select.Option value="negative">负面</Select.Option>
              <Select.Option value="neutral">中性</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="note" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
        <Space style={{ width: '100%', justifyContent: 'center', marginTop: 16 }}>
          <Button type="primary" onClick={handleSubmit}>提交并下一条</Button>
          <Button icon={<CheckOutlined />} style={{ background: '#52c41a', color: '#fff' }} onClick={function () { handleReview('approved'); }}>通过</Button>
          <Button danger icon={<CloseOutlined />} onClick={function () { handleReview('rejected'); }}>拒绝</Button>
        </Space>
      </Card>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Space>
          <Button onClick={function () { navigate(-1); }}>返回</Button>
          <span>第 {currentIndex + 1} / {totalItems} 条</span>
        </Space>
        <Space>
          <Button icon={<LeftOutlined />} disabled={currentIndex <= 0} onClick={function () { fetchItem(currentIndex - 1); }}>上一条</Button>
          <Button icon={<RightOutlined />} disabled={currentIndex >= totalItems - 1} onClick={function () { fetchItem(currentIndex + 1); }}>下一条</Button>
        </Space>
      </div>
      <div style={{ display: 'flex', gap: 16 }}>
        <div style={{ flex: 1 }}>{renderContent()}</div>
        <div style={{ width: 360 }}>{renderAnnotationPanel()}</div>
      </div>
    </div>
  );
}

export default AnnotationPage;
