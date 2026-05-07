import { useState, useCallback, useMemo, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ReactFlow, {
  Node, Edge, addEdge, Connection, useNodesState, useEdgesState, Controls, Background, MiniMap,
  Handle, Position, NodeProps, BackgroundVariant
} from "reactflow";
import "reactflow/dist/style.css";
import { Button, Input, Select, Space, message, Drawer, Form } from "antd";
import { PlayCircleOutlined, SaveOutlined, DeleteOutlined, ArrowLeftOutlined } from "@ant-design/icons";
import * as workflowService from "@/services/lab";

function WorkflowNode({ data }: NodeProps) {
  const colors: Record<string, string> = {
    data_prep: "#1890ff",
    training: "#52c41a",
    evaluation: "#fa8c16",
    custom: "#8c8c8c",
  };
  const bgColor = colors[String(data.nodeType)] || "#8c8c8c";
  const labels: Record<string, string> = {
    data_prep: "\u6570\u636e\u51c6\u5907",
    training: "\u6a21\u578b\u8bad\u7ec3",
    evaluation: "\u6a21\u578b\u8bc4\u4f30",
    custom: "\u81ea\u5b9a\u4e49",
  };
  return (
    <div style={{ padding: "8px 16px", borderRadius: 6, background: bgColor, color: "#fff", minWidth: 140, textAlign: "center", fontSize: 13, fontWeight: 500, boxShadow: "0 2px 8px rgba(0,0,0,0.15)" }}>
      <Handle type="target" position={Position.Top} style={{ background: "#555", width: 8, height: 8 }} />
      <div style={{ fontSize: 14 }}>{String(data.label)}</div>
      <div style={{ fontSize: 11, opacity: 0.75 }}>{labels[String(data.nodeType)] || String(data.nodeType)}</div>
      <Handle type="source" position={Position.Bottom} style={{ background: "#555", width: 8, height: 8 }} />
    </div>
  );
}

const nodeTypes = { workflowNode: WorkflowNode };

const NODE_CATALOG = [
  { type: "data_prep", label: "\u6570\u636e\u51c6\u5907", color: "#1890ff" },
  { type: "training", label: "\u6a21\u578b\u8bad\u7ec3", color: "#52c41a" },
  { type: "evaluation", label: "\u6a21\u578b\u8bc4\u4f30", color: "#fa8c16" },
  { type: "custom", label: "\u81ea\u5b9a\u4e49", color: "#8c8c8c" },
];

function extractData(res: unknown): Record<string, unknown> {
  if (res && typeof res === "object") {
    var obj = res as Record<string, unknown>;
    if (obj.data && typeof obj.data === "object") return obj.data as Record<string, unknown>;
    return obj;
  }
  return {};
}

export default function WorkflowEditor() {
  var params = useParams();
  var id = params.id;
  var navigate = useNavigate();
  var [nodes, setNodes, onNodesChange] = useNodesState([]);
  var [edges, setEdges, onEdgesChange] = useEdgesState([]);
  var [selectedNode, setSelectedNode] = useState<Node | null>(null);
  var [workflowName, setWorkflowName] = useState("\u65b0\u5de5\u4f5c\u6d41");
  var [workflowId, setWorkflowId] = useState<string | null>(id || null);
  var [propDrawerOpen, setPropDrawerOpen] = useState(false);
  var [running, setRunning] = useState(false);
  var [saving, setSaving] = useState(false);
  var [form] = Form.useForm();
  var nodeIdCounter = useMemo(function () { return { current: 1 }; }, []);

  var onConnect = useCallback(function (connection: Connection) {
    setEdges(function (eds) { return addEdge(connection, eds); });
  }, [setEdges]);

  function addNode(nodeType: string) {
    var newId = "node_" + Date.now() + "_" + nodeIdCounter.current++;
    var labels: Record<string, string> = {
      data_prep: "\u6570\u636e\u51c6\u5907",
      training: "\u6a21\u578b\u8bad\u7ec3",
      evaluation: "\u6a21\u578b\u8bc4\u4f30",
      custom: "\u81ea\u5b9a\u4e49",
    };
    var newNode: Node = {
      id: newId,
      type: "workflowNode",
      position: { x: 100 + Math.random() * 400, y: 100 + Math.random() * 300 },
      data: { label: labels[nodeType] || nodeType, nodeType: nodeType, image: "", params: "{}" },
    };
    setNodes(function (nds) { return [...nds, newNode]; });
  }

  function onNodeClick(_: React.MouseEvent, node: Node) {
    setSelectedNode(node);
    setPropDrawerOpen(true);
    form.setFieldsValue({
      label: node.data.label,
      nodeType: node.data.nodeType,
      image: node.data.image || "",
      params: node.data.params || "{}",
    });
  }

  function onPaneClick() {
    setPropDrawerOpen(false);
    setSelectedNode(null);
  }

  function updateSelectedNode() {
    if (!selectedNode) return;
    var sid = selectedNode.id;
    var values = form.getFieldsValue();
    setNodes(function (nds) {
      return nds.map(function (n) {
        return n.id === sid ? { ...n, data: { ...n.data, ...values } } : n;
      });
    });
    setPropDrawerOpen(false);
  }

  function deleteSelectedNode() {
    if (!selectedNode) return;
    var sid = selectedNode.id;
    setNodes(function (nds) { return nds.filter(function (n) { return n.id !== sid; }); });
    setEdges(function (eds) { return eds.filter(function (e) { return e.source !== sid && e.target !== sid; }); });
    setPropDrawerOpen(false);
    setSelectedNode(null);
  }

  async function saveWorkflow() {
    setSaving(true);
    var nodesJson = nodes.map(function (n) {
      var cfg = n.data.params ? JSON.parse(String(n.data.params)) : {};
      if (n.data.image) cfg.image = n.data.image;
      return { id: n.id, type: n.data.nodeType, name: n.data.label, config: cfg };
    });
    var edgesJson = edges.map(function (e) { return { source: e.source, target: e.target }; });
    try {
      var payload: Record<string, unknown> = {
        name: workflowName,
        type: "DAG",
        nodesJson: JSON.stringify(nodesJson),
        edgesJson: JSON.stringify(edgesJson),
      };
      if (workflowId) {
        await workflowService.updateWorkflow(workflowId, payload);
        message.success("\u5de5\u4f5c\u6d41\u5df2\u66f4\u65b0");
      } else {
        var res = await workflowService.createWorkflow(payload);
        var data = extractData(res);
        var newId = String(data.id || "");
        if (newId) {
          setWorkflowId(newId);
          navigate("/lab/workflows/" + newId, { replace: true });
        }
        message.success("\u5de5\u4f5c\u6d41\u5df2\u4fdd\u5b58");
      }
    } catch (e) {
      message.error("\u4fdd\u5b58\u5931\u8d25");
    } finally {
      setSaving(false);
    }
  }

  async function runWorkflowAction() {
    if (!workflowId) {
      message.warning("\u8bf7\u5148\u4fdd\u5b58\u5de5\u4f5c\u6d41");
      return;
    }
    setRunning(true);
    try {
      await workflowService.runWorkflow(workflowId);
      message.success("\u5de5\u4f5c\u6d41\u5df2\u542f\u52a8");
    } catch (e) {
      message.error("\u542f\u52a8\u5931\u8d25");
    } finally {
      setRunning(false);
    }
  }

  function loadWorkflow(wid: string) {
    workflowService.getWorkflow(wid).then(function (res) {
      var data = extractData(res);
      setWorkflowName(String(data.name || "\u65b0\u5de5\u4f5c\u6d41"));
      var parsedNodes: Node[] = [];
      var rawNodes: unknown[] = data.nodesJson ? JSON.parse(String(data.nodesJson)) : [];
      rawNodes.forEach(function (n, i) {
        var nd = n as Record<string, unknown>;
        var cfg = (nd.config || {}) as Record<string, unknown>;
        var paramsCopy = Object.assign({}, cfg);
        delete paramsCopy.image;
        parsedNodes.push({
          id: String(nd.id),
          type: "workflowNode",
          position: { x: 100 + i * 180, y: 100 + (i % 3) * 120 },
          data: {
            label: String(nd.name || nd.type),
            nodeType: String(nd.type),
            image: String(cfg.image || ""),
            params: JSON.stringify(paramsCopy),
          },
        });
      });
      var parsedEdges: Edge[] = [];
      var rawEdges: unknown[] = data.edgesJson ? JSON.parse(String(data.edgesJson)) : [];
      rawEdges.forEach(function (e) {
        var ed = e as Record<string, unknown>;
        parsedEdges.push({ id: String(ed.source) + "-" + String(ed.target), source: String(ed.source), target: String(ed.target) });
      });
      setNodes(parsedNodes);
      setEdges(parsedEdges);
    }).catch(function () {
      message.error("\u52a0\u8f7d\u5de5\u4f5c\u6d41\u5931\u8d25");
    });
  }

  useEffect(function () {
    if (id) loadWorkflow(id);
  }, []);

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "calc(100vh - 120px)" }}>
      <div style={{ display: "flex", marginBottom: 0, flex: 1, minHeight: 0 }}>
        <div style={{ width: 180, borderRight: "1px solid #e8e8e8", padding: 12, display: "flex", flexDirection: "column", gap: 8, flexShrink: 0 }}>
          <div style={{ fontWeight: 600, marginBottom: 4, fontSize: 14 }}>\u8282\u70b9\u7c7b\u578b</div>
          {NODE_CATALOG.map(function (item) {
            return (
              <Button key={item.type} block onClick={function () { addNode(item.type); }} style={{ textAlign: "left", borderColor: item.color, color: item.color }}>
                <span style={{ display: "inline-block", width: 8, height: 8, borderRadius: "50%", background: item.color, marginRight: 8 }} />
                {item.label}
              </Button>
            );
          })}
          <div style={{ flex: 1 }} />
          <Input value={workflowName} onChange={function (e) { setWorkflowName(e.target.value); }} placeholder="\u5de5\u4f5c\u6d41\u540d\u79f0" style={{ marginBottom: 8 }} />
          <Space direction="vertical" style={{ width: "100%" }}>
            <Button type="primary" icon={<SaveOutlined />} block onClick={saveWorkflow} loading={saving}>\u4fdd\u5b58</Button>
            <Button icon={<PlayCircleOutlined />} block onClick={runWorkflowAction} loading={running} disabled={!workflowId}>\u8fd0\u884c</Button>
            <Button icon={<ArrowLeftOutlined />} block onClick={function () { navigate("/lab/workflows"); }}>\u8fd4\u56de\u5217\u8868</Button>
          </Space>
        </div>

        <div style={{ flex: 1, minHeight: 0 }}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onNodeClick={onNodeClick}
            onPaneClick={onPaneClick}
            nodeTypes={nodeTypes}
            fitView
            deleteKeyCode="Delete"
            snapToGrid
            snapGrid={[15, 15]}
          >
            <Controls />
            <Background variant={BackgroundVariant.Dots} gap={15} size={1} />
            <MiniMap nodeStrokeWidth={3} zoomable pannable style={{ width: 120, height: 80 }} />
          </ReactFlow>
        </div>
      </div>

      <Drawer title="\u8282\u70b9\u5c5e\u6027" open={propDrawerOpen} onClose={function () { setPropDrawerOpen(false); }} width={320} extra={<Space><Button type="primary" onClick={updateSelectedNode}>\u786e\u5b9a</Button><Button danger icon={<DeleteOutlined />} onClick={deleteSelectedNode}>\u5220\u9664</Button></Space>}>
        <Form form={form} layout="vertical">
          <Form.Item label="\u540d\u79f0" name="label"><Input /></Form.Item>
          <Form.Item label="\u7c7b\u578b" name="nodeType">
            <Select options={[
              { value: "data_prep", label: "\u6570\u636e\u51c6\u5907" },
              { value: "training", label: "\u6a21\u578b\u8bad\u7ec3" },
              { value: "evaluation", label: "\u6a21\u578b\u8bc4\u4f30" },
              { value: "custom", label: "\u81ea\u5b9a\u4e49" },
            ]} />
          </Form.Item>
          <Form.Item label="Docker \u955c\u50cf" name="image"><Input placeholder="python:3.11" /></Form.Item>
          <Form.Item label="\u53c2\u6570 (JSON)" name="params"><Input.TextArea rows={6} /></Form.Item>
        </Form>
      </Drawer>
    </div>
  );
}
